package com.ylsk.androidacc;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class GeoLocationTool extends Thread {

    private LocationManager locationManager;

    private float speed;

    public float getSpeed() {
        return speed;
    }

    /**
     * 构造函数，位置管理器
     * @param locationManager 位置管理器
     */
    public GeoLocationTool(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public void run() {

        while (true) {
            try {
                Location location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                this.speed = location.getSpeed();
                Log.i("速度数据：", "speed = " +speed );
                Thread.sleep(1000);
            }
            catch(SecurityException | InterruptedException exs)
            {
                exs.printStackTrace();
            }
        }
    }
}
