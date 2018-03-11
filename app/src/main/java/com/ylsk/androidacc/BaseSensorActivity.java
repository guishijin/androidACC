package com.ylsk.androidacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

public abstract class BaseSensorActivity extends Activity implements SensorEventListener {
    private static final String TAG = "BaseSensorActivity";
    protected SensorManager mSensorManager;
    protected Sensor mSensor;
    protected int mSensorDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    protected void onPause() {
        super.onPause();
        try{
            mSensorManager.unregisterListener(this);
        } catch (Exception ex) {
            Toast.makeText(this,"检测失败！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public abstract void onSensorChanged(SensorEvent sensorEvent);

    @Override
    public abstract void onAccuracyChanged(Sensor sensor, int i);

    protected abstract int getDefaultSensor();

    protected int getDefaultSensorDelay()
    {
        return SensorManager.SENSOR_DELAY_GAME;
    }
}
