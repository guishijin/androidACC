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

public class MainActivity extends Activity {

    private LocationManager locationManager;
    GeoLocationTool glt;

    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView)findViewById(R.id.main_list);
        String[] features = getResources().getStringArray(R.array.sensors);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,features);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent();
                switch (position) {
                    case 0://sensor info
                        intent.setClass(MainActivity.this, DeviceSensorInfoActivity.class);
                        startActivity(intent);
                        break;
                    case 1://orientation
                        intent.setClass(MainActivity.this, OrientationActivity.class);
                        startActivity(intent);
                        break;
                    case 2://gyroscope
                        intent.setClass(MainActivity.this, GyroscopeActivity.class);
                        startActivity(intent);
                        break;
                    case 3://accelerometer
                        intent.setClass(MainActivity.this, AccelerometerActivity.class);
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
