package com.ylsk.inertialnavigation.activities;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.ylsk.androidacc.R;
import com.ylsk.inertialnavigation.sensors.Core;

/**
 * 计步器Activity
 */
public class StepCounter extends Activity implements Core.onStepUpdateListener {

    private String Tag = "StepCounter";

    private TextView info;
    private Core mCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        init();
    }

    /**
     * 初始化
     */
    private void init()
    {
        this.info = (TextView)this.findViewById(R.id.textview_info);
        this.mCore = new Core(this);

        double defaultLat = 50.000000D;
        double defaultLon = 10.000000D;
        double mittellat = defaultLat * 0.01745329252;
        double abstandLaengengrade = 111.3D * Math.cos(mittellat);
        Core.initialize(defaultLat, defaultLon, abstandLaengengrade, 200, 3000);
        Core.stepLength = 0.8f;

        this.mCore.startSensors();
    }

    /**
     * 实现计步操作
     * @param event
     */
    @Override
    public void onStepUpdate(int event) {

        //
        if (event == 0) {
            //New step detected, change position
            positionUpdate();
        } else {
            //Threshold reached for Autocorrection
            //mLocationer.starteAutocorrect();
            // 矫正处理
            starteAutocorrect();
        }

    }

    /**
     * 位置更新
     */
    private void positionUpdate() {
        int latE6 = (int) (Core.startLat * 1E6);
        int lonE6 = (int) (Core.startLon * 1E6);
        int steps = Core.stepCounter;

        this.info.setText("--------------------------------\n");
        this.info.append("纬度： "+latE6+"\n");
        this.info.append("经度： "+lonE6+"\n");
        this.info.append("--------------------------------\n");
        this.info.append("步数： "+ steps +"\n");
        this.info.append("--------------------------------\n");
        this.info.append("陀螺仪传感器："+this.mCore.gyroExists+"\n");
        this.info.append("--------------------------------\n");

        // TODO: 位置显示
//        if (myLocationOverlay != null) {
//            myLocationOverlay.setLocation(new GeoPoint(latE6, lonE6));
//            myLocationOverlay.setBearing((float) Core.azimuth);
//            map.invalidate();
//            if (followMe) {
//                mapController.animateTo(new GeoPoint(latE6, lonE6));
//            }
//        }
    }

    /**
     * 矫正计算
     */
    private void starteAutocorrect()
    {
        // TODO: 矫正处理
        Log.i(Tag,"自动矫正： starteAutocorrect()！ ");
    }

}
