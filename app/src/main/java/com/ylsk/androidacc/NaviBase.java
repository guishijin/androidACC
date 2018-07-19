package com.ylsk.androidacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;

import com.ylsk.datafilter.RCFilter;
import com.ylsk.inertialnavigation.representation.Matrix;
import com.ylsk.inertialnavigation.tools.MathExt;

import java.text.DecimalFormat;

/**
 * 惯性导航测试
 */
public class NaviBase extends Activity implements SensorEventListener {

    private SensorManager sensorManager;

    // 当前线性加速度采集的时刻 -- 单位 纳秒
    private long mTime;

    // 两次采集的时间差 -- 单位 秒
    private double sTime;

    // 起始时间初始化为0，用来表示还没有开始 -- 单位 纳秒
    private long startTime = 0;

    // 地磁
    private float[] geomagnetic = null;
    // 旋转矩阵
    private float[] r = new float[9];
    // 倾斜矩阵
    private float[] I = new float[9];

    // 旋转矩阵4*4
    private float[] r44 = new float[16];
    private float[] speedOL4 = new float[4];

    // 重力加速度
    private float[] gravity = null;

    // 线性加速度
    private float[] speedOL = null;

    // 累计速度，初始为0
    private double[] speedREll = new double[]{0.0, 0.0, 0.0};
    // 累计距离，初始为0
    private double[] xyzDistance = new double[]{0.0, 0.0, 0.0};

    // 显示控件
    TextView textView;

    private RCFilter aXFilter;
    private RCFilter aYFilter;
    private RCFilter aZFilter;

    private RCFilter aXYFilter;
    private RCFilter aXYZFilter;

    private RCFilter timeFilter;

    // 速度漂移归零阈值
    private final float speedThresold = 5.0f;
    // 加速度阈值
    private final float atThresold = 0.01f;
    // 速度漂移归零计次统计
    private final int speedthresoldCount = 100;
    private int countspeed = 0;


    private PowerManager.WakeLock mWakeLock;

    /**
     *  候选值：
     *
     *  SENSOR_DELAY_FASTEST = 0;   // 金立M6=200Hz   间隔  5毫秒
     *  SENSOR_DELAY_GAME = 1;      // 金立M6= 50Hz   间隔 20毫秒
     *  SENSOR_DELAY_UI = 2;        // 金立M6= 15Hz   间隔 66毫秒
     *  SENSOR_DELAY_NORMAL = 3;    // 金立M6=  5Hz   间隔200毫秒
     *
     *  或者直接指定 毫秒值。
     *  通过测试表明，
     *  填写的延时时间值时的有效范围能超过 SENSOR_DELAY_FASTEST 和 SENSOR_DELAY_NORMAL的边界。
     */
    private int delaytimeUS = SensorManager.SENSOR_DELAY_GAME;
    //---------------------------------------------------------------------------------------------
    // private int delaytimeUS =   <1 * 1000; // 自定义周期：<1毫秒间隔           期望：1000+  实测  200Hz
    // private int delaytimeUS =    1 * 1000; // 自定义周期： 1毫秒间隔           期望：1000hz 实测  200Hz
    // private int delaytimeUS =    2 * 1000; // 自定义周期： 2毫秒间隔           期望： 500hz 实测  200Hz
    // private int delaytimeUS =    5 * 1000; // 和 SENSOR_DELAY_FASTEST 等效     期望： 200Hz 实测  200Hz
    // private int delaytimeUS =   10 * 1000; // 自定义周期： 10毫秒间隔          期望： 100Hz 实测  100Hz
    // private int delaytimeUS =   20 * 1000; // 和 SENSOR_DELAY_GAME 等效        期望：  50Hz 实测   50Hz
    // private int delaytimeUS =   40 * 1000; // 自定义周期： 40毫秒间隔          期望：  25Hz 实测   25Hz
    // private int delaytimeUS =   66 * 1000; // 和 SENSOR_DELAY_UI 等效          期望：  15Hz 实测   15Hz
    // private int delaytimeUS =  100 * 1000; // 自定义周期： 100毫秒间隔         期望:   10Hz 实测   10Hz
    // private int delaytimeUS =  200 * 1000; // 和 SENSOR_DELAY_NORMAL 等效      期望：   5Hz 实测    5Hz
    // private int delaytimeUS =  500 * 1000; // 自定义周期：500毫秒间隔          期望：   2Hz 实测    5Hz
    // private int delaytimeUS = 1000 * 1000; // 自定义周期：1000毫秒间隔         期望：   1Hz 实测    5Hz
    // private int delaytimeUS =>1000 * 1000; // 自定义周期：大于1000毫秒间隔     期望：  <1Hz 实测    5Hz
    //---------------------------------------------------------------------------------------------

    // 消息处理器
    private Handler handler = new Handler() {

        /**
         * 重写消息处理方法
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {

            // 判断数据是否采集完整
            if (geomagnetic != null && gravity != null && speedOL != null) {

                // 如果startTime == 0 说明没有采集到至少2次的信息
                if (startTime == 0) {
                    // 记录上一次开始时间
                    startTime = mTime;

                    // 直接返回，本次不计算
                    return;
                }

                // 积分时间片段计算：
                sTime = (mTime - startTime) / 1000.0;

                // 根据重力加速度和磁场，获取旋转矩阵和倾斜矩阵
                if (SensorManager.getRotationMatrix(r, I, gravity, geomagnetic)) {

                    // 更新起始时间戳
                    startTime = mTime;

                    // 计算各个方向的加速度 （映射到大地坐标系中）
                    float aX = speedOL[0] * r[0] + speedOL[1] * r[1] + speedOL[2] * r[2];
                    float aY = speedOL[0] * r[3] + speedOL[1] * r[4] + speedOL[2] * r[5];
                    float aZ = speedOL[0] * r[6] + speedOL[1] * r[7] + speedOL[2] * r[8];

                    // 对时间片段、加速度进行低通滤波
                    sTime = timeFilter.doFilter((float) sTime);
                    aX = MathExt.FRount(aXFilter.doFilter(aX),2);
                    aY = MathExt.FRount(aYFilter.doFilter(aY),2);
                    aZ = MathExt.FRount(aZFilter.doFilter(aZ),2);

                    // 累计计算当前的速度 速度积分
                    speedREll[0] = MathExt.FRount((float)(speedREll[0] + sTime * aX),2);
                    speedREll[1] = MathExt.FRount((float)(speedREll[1] + sTime * aY),2);
                    speedREll[2] = MathExt.FRount((float)(speedREll[2] + sTime * aZ),2);

                    if(  Math.abs(aX) < 0.01f && Math.abs(aY) < 0.01f && Math.abs(speedREll[0]) < speedThresold && Math.abs(speedREll[1]) < speedThresold )
                    {
                        countspeed++;
                    }
                    // 速度归零
                    if(countspeed >= speedthresoldCount)
                    {
                        speedREll[0] = 0;
                        speedREll[1] = 0;
                        speedREll[2] = 0;
                        countspeed = 0;
                    }

                    // 计算时间片内的移动距离    S = V。* t  + 1/2 * a * t * t
                    xyzDistance[0] = xyzDistance[0] + (speedREll[0] * sTime + sTime * sTime * aX / 2);
                    xyzDistance[1] = xyzDistance[1] + (speedREll[1] * sTime + sTime * sTime * aY / 2);
                    xyzDistance[2] = xyzDistance[2] + (speedREll[2] * sTime + sTime * sTime * aZ / 2);



                    // 计算磁场强度
                    float h =
                            (I[3] * r[0] + I[4] * r[3] + I[5] * r[6]) * geomagnetic[0] +
                            (I[3] * r[1] + I[4] * r[4] + I[5] * r[7]) * geomagnetic[1] +
                            (I[3] * r[2] + I[4] * r[5] + I[5] * r[8]) * geomagnetic[2] ;

                    // 输出信息到界面
                    DecimalFormat df = new DecimalFormat();
                    String style = "0.00"; //定义要显示的数字的格式
                    df.applyPattern(style);      // 将格式应用于格式化器

                    textView.setText(
                                    "====================================\n" +
                                    "时  间  差(秒)：" + MathExt.FRount((float)sTime,3) + "\n" +
                                    "====================================\n" +
                                    "x  加速度(米/秒方)：" + df.format(aX) + "\n" +
                                    "y  加速度(米/秒方)：" + df.format(aY) + "\n" +
                                    "z  加速度(米/秒方)：" + df.format(aZ) + "\n" +
                                    "|x,y| ：" + df.format(Math.sqrt( aX*aX + aY*aY )) + "\n" +
                                    "|x,y,z| ：" + df.format(Math.sqrt( aX*aX + aY*aY + aZ*aZ)) + "\n" +
                                    "====================================\n" +
                                    "x（东向）速    度(米/秒)：" + df.format(speedREll[0]) + "\n" +
                                    "y（北向）速    度(米/秒)：" + df.format(speedREll[1]) + "\n" +
                                    "z（高程）速    度(米/秒)：" + df.format(speedREll[2]) + "\n" +
                                    "====================================\n" +
                                    "x（东向）距    离(米)：" + df.format(xyzDistance[0]) + "\n" +
                                    "y（东向）距    离(米)：" + df.format(xyzDistance[1]) + "\n" +
                                    "z（高程）距    离(米)：" + df.format(xyzDistance[2]) + "\n" +
                                    "====================================\n"
                                    );
                }
            }
        }
    };

    /**
     * 重写Activiey的创建事件
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi_base);

        // 初始化控件
        textView = (TextView) findViewById(R.id.textView);

        // 初始化传感器管理器对象
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        float factor = 0.01f;
        aXFilter = new RCFilter(factor);
        aYFilter = new RCFilter(factor);
        aZFilter = new RCFilter(factor);
        aXYFilter  = new RCFilter(factor);
        aXYZFilter = new RCFilter(factor);
        timeFilter = new RCFilter(factor);

        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
        }
    }

    /**
     * 实现onSensorChanged接口
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:  // 记录线性加速度
                // 使用系统时间代替传感器时间
                mTime = System.currentTimeMillis();
                // 记录线性加速度值
                speedOL = event.values;
                // 通知界面线程处理
                handler.sendEmptyMessage(0);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:        // 记录磁场信息
                // 记录磁场值
                geomagnetic = event.values;
                break;
            case Sensor.TYPE_GRAVITY:                // 记录重力加速度
                // 记录重力加速度值
                gravity = event.values;
                break;
            default:
                break;
        }
    }

    /**
     * 实现onAccuracyChanged接口
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 重写Activity的暂停事件
     */
    @Override
    protected void onPause() {
        super.onPause();

        // 注销监听器
        sensorManager.unregisterListener(this);

        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    /**
     * 重写Activity的恢复事件
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 注册监听器
        // 获取线性加速度
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, delaytimeUS);

        // 加速度
//        Sensor sensor1=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        sensorManager.registerListener(this,sensor1,delaytimeUS);

        // 磁场
        Sensor sensor2 = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, sensor2, delaytimeUS);

        // 重力加速度
        Sensor sensor3 = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, sensor3, delaytimeUS);

        // 旋转向量
//        Sensor sensor4=sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        sensorManager.registerListener(this,sensor4,delaytimeUS);

        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }
}
