package com.ylsk.androidacc;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class AccelerometerActivity extends BaseSensorActivity {
    private TextView mTvInfo;
    private float mGravity = SensorManager.STANDARD_GRAVITY-0.8f;

    private int index = 0;
    private long starttime = System.currentTimeMillis();
    private long endtime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_accelerometer);
        mTvInfo = (TextView)findViewById(R.id.tv_info);
        if(mSensor == null) {
            mTvInfo.setText("No Accelerometer senor!");
            Log.d("linc","no this sensor.");
        }

    }

    /**
     * 接收数据通知
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("linc", "value size: " + sensorEvent.values.length);
        float xValue = sensorEvent.values[0];// Acceleration minus Gx on the x-axis
        float yValue = sensorEvent.values[1];//Acceleration minus Gy on the y-axis
        float zValue = sensorEvent.values[2];//values[2]: Acceleration minus Gz on the z-axis
        Log.d("加速度信息"+(this.index++)+"|"+System.currentTimeMillis()+"| :","x："+xValue+", y："+yValue+ ", z： "+zValue);

        this.endtime = System.currentTimeMillis();

        float fps  = 0.0f;
        if(this.endtime - this.starttime > 0) {
            fps = (this.index*1000 / (this.endtime - this.starttime));
            Log.d("加速度信息" + (this.index) + "| 实时统计信息 | :", "时长：" + (this.endtime - this.starttime) + ", 次数：" + this.index + ", 每秒平均值：" + fps);
        }

        mTvInfo.setText(
                        "Max-fps:"+fps+ "   \r\nx轴： "+xValue+"  \r\ny轴： "+yValue+"  \r\nz轴： "+zValue +
                        "   \r\n"+
                        "| x |： "+Math.sqrt(xValue*xValue)+"  \r\n| y |： "+Math.sqrt(yValue*yValue)+"  \r\n| z |： "+Math.sqrt(zValue*zValue) +
                        "   \r\n"+
                        "| x + y + z|: " + Math.sqrt(xValue*xValue + yValue*yValue + zValue*zValue)
        );

        if(xValue > mGravity) {
            mTvInfo.append("\n重力指向设备左边");
        } else if(xValue < -mGravity) {
            mTvInfo.append("\n重力指向设备右边");
        } else if(yValue > mGravity) {
            mTvInfo.append("\n重力指向设备下边");
        } else if(yValue < -mGravity) {
            mTvInfo.append("\n重力指向设备上边");
        } else if(zValue > mGravity) {
            mTvInfo.append("\n屏幕朝上");
        } else if(zValue < -mGravity) {
            mTvInfo.append("\n屏幕朝下");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    protected int getDefaultSensor() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    /**
     * 自定义 采样频率 128Hz
     * @return
     */
    @Override
    protected int getDefaultSensorDelay() {

        // 重新初始化计数和时间统计
        this.index = 0;
        this.starttime = System.currentTimeMillis();
        this.endtime = System.currentTimeMillis();

        //return SensorManager.SENSOR_DELAY_FASTEST;
        //return SensorManager.SENSOR_DELAY_GAME;
        //return SensorManager.SENSOR_DELAY_UI;
        //return SensorManager.SENSOR_DELAY_NORMAL;

        // 采样频率设计为128Hz： 1000000/128 = 7,812.5, 定位 7812
        //return 7812; //124

        return SensorManager.SENSOR_DELAY_FASTEST;
    }
}
