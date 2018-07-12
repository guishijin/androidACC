package com.ylsk.androidacc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ylsk.apache.math.fft.*;

import junit.framework.Test;

import java.io.FileOutputStream;

public class AccelerometerActivity extends BaseSensorActivity {

    // 定位数据
    private LocationManager locationManager;
    private Location loc;
    private double xx = 0.0;
    private double yy= 0.0;
    private float speed;

    // 采样频率
    private int hz = 200;
    private TextView mTvInfo;
    private float mGravity = SensorManager.STANDARD_GRAVITY - 0.8f;

    private int index = 0;
    private long starttime = System.currentTimeMillis();
    private long endtime = System.currentTimeMillis();

    // x,y,z,和总的加速度模采样数据。
    private double[] xValues = new double[hz];
    private double[] yValues = new double[hz];
    private double[] zValues = new double[hz];
    private double[] accValues = new double[hz];
    private double[] speedValues = new double[hz];

    FileOutputStream fos;
    private PowerManager.WakeLock mWakeLock;

    public float getSpeed() {
        return this.speed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_accelerometer);
        mTvInfo = (TextView) findViewById(R.id.tv_info);
        if (mSensor == null) {
            mTvInfo.setText("No Accelerometer senor!");
            Log.d("linc", "no this sensor.");
        }

        // 获取位置管理服务
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // 保存定位信息
                    AccelerometerActivity.this.loc = location;
                    AccelerometerActivity.this.xx = location.getLongitude();
                    AccelerometerActivity.this.yy = location.getLatitude();
                    AccelerometerActivity.this.speed = (float)(location.getSpeed()*3.6);
                    Toast.makeText(AccelerometerActivity.this,"定位信息："+location.getSpeed()+"-"+location.getLatitude()+" | " + location.getLongitude(),Toast.LENGTH_SHORT);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Toast.makeText(AccelerometerActivity.this,"定位状态： "+status,Toast.LENGTH_SHORT);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Toast.makeText(AccelerometerActivity.this,"定位打开！！！！ ",Toast.LENGTH_SHORT);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Toast.makeText(AccelerometerActivity.this,"定位关闭！！！！ ",Toast.LENGTH_SHORT);
                }
            });

            this.loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        catch(SecurityException sex)
        {
            sex.printStackTrace();
        }

        String dir = "androidACC";
        String filename = "加速度原始数据-"+System.currentTimeMillis()+".txt";
        try {
            fos = new FileOutputStream(FileUtil.CreateFile(dir, filename));
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }


        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
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
        float sValue = this.speed;
        long curtime = sensorEvent.timestamp / 1000000;
        long systime = System.currentTimeMillis();
        String lineinfotem = "{\"x\":%s,\"y\":%s,\"z\":%s,\"speed\":%s,\"timestamp\":%s,\"systime\":%s},\r\n";
        String lineinfoval = String.format(lineinfotem,xValue,yValue,zValue,sValue,curtime,systime);
        try {
            if(fos!= null) {
                fos.write(lineinfoval.getBytes());
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Log.d("加速度信息：",lineinfoval);

        this.endtime = System.currentTimeMillis();

        float fps  = 0.0f;
//        double x = Math.sqrt(xValue*xValue);
//        double y = Math.sqrt(yValue*yValue);
//        double z = Math.sqrt(zValue*zValue);
//        double acc = Math.sqrt(xValue*xValue + yValue*yValue + zValue*zValue);
//        double sp = this.speed;
//
//
//
//        if(this.index >=0 && this.index < hz) {
//            this.xValues[this.index] = x;
//            this.yValues[this.index] = y;
//            this.zValues[this.index] = z;
//            this.accValues[this.index] = acc;
//            this.speedValues[this.index] = sp;
//        }
//
        if(this.endtime - this.starttime > 0) {
            fps = ((this.index)*1000 / (this.endtime - this.starttime));
            Log.d("加速度信息" + (this.index) + "| 实时统计信息 | :", "时长：" + (this.endtime - this.starttime) + ", 次数：" + this.index + ", 每秒平均值：" + fps);
        }

        mTvInfo.setText(
                "fps:"+fps+ "   \r\nx轴： "+xValue+"  \r\ny轴： "+yValue+"  \r\nz轴： "+zValue +
                        "   \r\n"+
                        "| x |： "+Math.sqrt(xValue*xValue)+"  \r\n| y |： "+Math.sqrt(yValue*yValue)+"  \r\n| z |： "+Math.sqrt(zValue*zValue) +
                        "   \r\n"+
                        "| x + y + z|: " + Math.sqrt(xValue*xValue + yValue*yValue + zValue*zValue) +
                        "   \r\n"+
                        "速度: " + sValue + " \r\n经度：" + xx + "\r\n纬度: " + yy
        );
//
////        if(xValue > mGravity) {
////            mTvInfo.append("\n重力指向设备左边");
////        } else if(xValue < -mGravity) {
////            mTvInfo.append("\n重力指向设备右边");
////        } else if(yValue > mGravity) {
////            mTvInfo.append("\n重力指向设备下边");
////        } else if(yValue < -mGravity) {
////            mTvInfo.append("\n重力指向设备上边");
////        } else if(zValue > mGravity) {
////            mTvInfo.append("\n屏幕朝上");
////        } else if(zValue < -mGravity) {
////            mTvInfo.append("\n屏幕朝下");
////        }
//
//        // 每256组数据做一次变换
//        if(this.index == 256)
//        {
//            this.index = 0;
//            this.starttime = this.endtime;
//            this.endtime = System.currentTimeMillis();
//
////            CalcFFT fft = new CalcFFT(this.accValues);
////            TestThread th = new TestThread(fft);
////            th.setName("Thread-"+System.currentTimeMillis());
////            th.start();
//        }

        this.index++;
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

        //采样频率设计为128Hz： 1000000/128 = 7,812.5, 定位 7812
        //return 7812; //124
        return 1000000 / hz;

        //return SensorManager.SENSOR_DELAY_FASTEST;
    }

    @Override
    protected void onDestroy() {
        if(this.fos!= null)
        {
            try {
                this.fos.close();
            }catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause(){
       super.onPause();

        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    // 权限请求响应码
    private static final int  REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    /**
     * 检查文件读写权限
     */
    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);

        } else {
            Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                              int[] grantResults)
    {
        if(REQUEST_WRITE_EXTERNAL_STORAGE == requestCode)
        {
            Toast.makeText(this,"权限申请成功！Code = "+requestCode,Toast.LENGTH_SHORT);
        }
        else
        {
            Toast.makeText(this,"权限申请失败！Code = "+requestCode,Toast.LENGTH_SHORT);
        }
    }


}
