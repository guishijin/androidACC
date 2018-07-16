package com.ylsk.androidacc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ylsk.inertialnavigation.activities.StepCounter;

/**
 * 主页面
 */
public class MainActivity extends Activity {

    // 定位服务管理器
    private LocationManager locationManager;
    // 地理位置工具
    GeoLocationTool glt;

    // 主页面功能列表
    private ListView mListView;

    /**
     * 页面创建事件
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取列表控件
        mListView = (ListView)findViewById(R.id.main_list);

        // 获取列表条目数组
        String[] features = getResources().getStringArray(R.array.sensors);
        // 使用数组定义一个适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,features);
        // 使用适配器初始化列表控件
        mListView.setAdapter(adapter);
        // 设置条目的单击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent();
                switch (position) {
                    case 0:// sensor info - 列举传感器信息
                        intent.setClass(MainActivity.this, DeviceSensorInfoActivity.class);
                        startActivity(intent);
                        break;
                    case 1://orientation - 水平方向传感器
                        intent.setClass(MainActivity.this, OrientationActivity.class);
                        startActivity(intent);
                        break;
                    case 2://gyroscope - 重力传感器
                        intent.setClass(MainActivity.this, GyroscopeActivity.class);
                        startActivity(intent);
                        break;
                    case 3://accelerometer - 加速度传感器
                        intent.setClass(MainActivity.this, AccelerometerActivity.class);
                        startActivity(intent);
                        break;
                    case 4://CompassActivity - 加速度传感器
                        intent.setClass(MainActivity.this, CompassActivity.class);
                        startActivity(intent);
                        break;

                    case 5://RotationVectorDemo - 加速度传感器
                        intent.setClass(MainActivity.this, RotationVectorDemo.class);
                        startActivity(intent);
                        break;
                    case 6://StepCounter - 惯性导航-计步器
                        intent.setClass(MainActivity.this, StepCounter.class);
                        startActivity(intent);
                        break;
                    case 7://NaviBase - 惯性导航-计步器
                        intent.setClass(MainActivity.this, NaviBase.class);
                        startActivity(intent);
                        break;

                }
            }
        });

        // 获取位置管理服务
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
        glt = new GeoLocationTool(locationManager);

    }
}
