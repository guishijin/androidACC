package com.ylsk.androidacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 传感器基础窗口类
 */
public abstract class BaseSensorActivity extends Activity implements SensorEventListener {

    private static final String TAG = "BaseSensorActivity";

    // 传感器管理器
    protected SensorManager mSensorManager;
    // 传感器对象
    protected Sensor mSensor;

    // 传感器采集延时
    protected int mSensorDelay;

    /**
     * 初始构造函数
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 根据传感器类型获取传感器对象---传感器类型由子类重写
        try{
            mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(getDefaultSensor());//sensor type
            mSensorDelay = getDefaultSensorDelay(); // 获取采集延时 默认为：SensorManager.SENSOR_DELAY_GAME 20毫秒一个采集周期
        } catch (Exception ex) {
            Toast.makeText(this, "检测失败！", Toast.LENGTH_SHORT).show();
        } catch (Error error) {
            Toast.makeText(this,"检测失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 恢复处理
     */
    @Override
    protected void onResume() {
        super.onResume();
        try{
            mSensorDelay = getDefaultSensorDelay();
            mSensorManager.registerListener(this, mSensor,
                    //SensorManager.SENSOR_DELAY_NORMAL);
                    mSensorDelay);
        } catch (Exception ex) {
            Toast.makeText(this, "检测失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 暂停处理
     */
    @Override
    protected void onPause() {
        super.onPause();
        try{
            mSensorManager.unregisterListener(this);
        } catch (Exception ex) {
            Toast.makeText(this,"检测失败！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 实现传感器接口，由子类实现
     * @param sensorEvent
     */
    @Override
    public abstract void onSensorChanged(SensorEvent sensorEvent);

    /**
     * 实现传感器接口，由子类实现
     * @param sensor
     * @param i
     */
    @Override
    public abstract void onAccuracyChanged(Sensor sensor, int i);

    /**
     * 获取默认的传感器，由子类实现
     * @return
     */
    protected abstract int getDefaultSensor();

    /**
     * 获取传感器延时，默认为GAME等级
     * @return
     */
    protected int getDefaultSensorDelay()
    {
        return SensorManager.SENSOR_DELAY_GAME;
    }
}
