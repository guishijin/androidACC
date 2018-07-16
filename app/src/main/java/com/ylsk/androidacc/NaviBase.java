package com.ylsk.androidacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

/**
 * 惯性导航测试
 */
public class NaviBase extends Activity implements SensorEventListener {

        private SensorManager sensorManager;
        private Long mTime;
        private Double sTime;
        private Long startTime= Long.valueOf(0);

        private float[] geomagnetic = null;
        private float[] r = new float[9];
        private float[] I = new float[9];
        private float[] gravity=null;
        private float[] speedOL=null;
        private Double[] speedREll=new Double[]{0.0,0.0,0.0};
        private Double[] xyzDistance=new Double[]{0.0,0.0,0.0};

        private Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {

                //if( startTime == 0 && geomagnetic != null && gravity != null){
                if( geomagnetic != null && gravity != null){
                    sTime = (mTime - startTime)*0.01;

                    // 获取旋转矩阵
                    if (SensorManager.getRotationMatrix(r, I, gravity, geomagnetic)) {
                        startTime = System.currentTimeMillis();
                        float aX = speedOL[0] * r[0] + speedOL[1] * r[1] + speedOL[2] * r[2];
                        float aY = speedOL[0] * r[3] + speedOL[1] * r[4] + speedOL[2] * r[5];
                        float aZ = speedOL[0] * r[6] + speedOL[1] * r[7] + speedOL[2] * r[8];

                        xyzDistance[0] = speedREll[0] * sTime + sTime * sTime * aX / 2;
                        xyzDistance[1] = speedREll[1] * sTime + sTime * sTime * aY / 2;
                        xyzDistance[2] = speedREll[2] * sTime + sTime * sTime * aZ / 2;
                        speedREll[0] = speedREll[0] + sTime * aX;
                        speedREll[1] = speedREll[1] + sTime * aY;
                        speedREll[2] = speedREll[2] + sTime * aZ;
                        float h = (I[3] * r[0] + I[4] * r[3] + I[5] * r[6]) * geomagnetic[0] +
                                (I[3] * r[1] + I[4] * r[4] + I[5] * r[7]) * geomagnetic[1] +
                                (I[3] * r[2] + I[4] * r[5] + I[5] * r[8]) * geomagnetic[2];
                        TextView textView = (TextView) findViewById(R.id.textView);
                        textView.setText("时间差" + sTime + "\n" +
                                "x加速度" + aX + "\n" +
                                "y加速度" + aY + "\n" +
                                "z加速度" + aZ + "\n" +
                                "x速度" + speedREll[0] + "\n" +
                                "y速度" + speedREll[1] + "\n" +
                                "z速度" + speedREll[2] + "\n" +
                                "x" + xyzDistance[0] + "\n" +
                                "y" + xyzDistance[1] + "\n" +
                                "z" + xyzDistance[2] + "\n" +
                                "磁场强度" + h + "\n");
                    }
                }
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_navi_base);

            sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()){
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    speedOL=event.values;
                    mTime=event.timestamp;
                    speedOL=event.values;
                    handler.sendEmptyMessage(0);

                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    geomagnetic=event.values;
                    break;
                case Sensor.TYPE_GRAVITY:
                    gravity=event.values;
                    break;
                default:break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {


        }

        @Override
        protected void onPause() {
            super.onPause();
            sensorManager.unregisterListener(this);
        }

        @Override
        protected void onResume() {
            super.onResume();

            // 获取线性加速度
            Sensor sensor=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);

//        Sensor sensor1=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        sensorManager.registerListener(this,sensor1,SensorManager.SENSOR_DELAY_FASTEST);

            Sensor sensor2=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this,sensor2,SensorManager.SENSOR_DELAY_GAME);

            Sensor sensor3=sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            sensorManager.registerListener(this,sensor3,SensorManager.SENSOR_DELAY_GAME);

//        Sensor sensor4=sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        sensorManager.registerListener(this,sensor4,SensorManager.SENSOR_DELAY_FASTEST);
        }
}
