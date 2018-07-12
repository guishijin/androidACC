package com.ylsk.androidacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

/**
 * 手机传感器详细信息
 */
public class DeviceSensorInfoActivity extends Activity {

    private static final String TAG = "DeviceSensorInfo";

    // 文本框
    private TextView mTvInfo;

    /**
     * 构造函数
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_sensor_info);

        // 获取文本框
        mTvInfo = (TextView)findViewById(R.id.tv_info);

        // 获取传感器管理器
        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // 使用传感器管理器获取所有类型的传感器的列表
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        // 输出传感器总个数
        mTvInfo.setText("传感器总个数: " + sensors.size());
        Log.d(TAG, "传感器总个数: " + sensors.size());

        // 循环遍历每一个传感器
        for (int i = 0;i < sensors.size();++i) {

            // 输出传感器
            Log.d(TAG,"\n--------------------------------------------");
            Log.d(TAG,"sensor type: "+sensors.get(i).getType()+" | " + sensors.get(i).getStringType());
            Log.d(TAG,"sensor name: "+sensors.get(i).getName());
            Log.d(TAG,"sensor vendor: "+sensors.get(i).getVendor());
            Log.d(TAG,"sensor power: "+sensors.get(i).getPower());
            Log.d(TAG, "sensor resolution: " + sensors.get(i).getResolution());

            mTvInfo.append("\n--------------------------------------------");
            mTvInfo.append("\n"+i+": sensor type: "+sensors.get(i).getType()+" | " + sensors.get(i).getStringType());
            mTvInfo.append("\n"+i+": sensor name: " + sensors.get(i).getName());
            mTvInfo.append("\n"+i+": sensor vendor: "+sensors.get(i).getVendor());
            mTvInfo.append("\n"+i+": sensor power: "+sensors.get(i).getPower());
            mTvInfo.append("\n"+i+": sensor resolution: "+ sensors.get(i).getResolution());
            mTvInfo.append("\n\n");
        }
    }

}
