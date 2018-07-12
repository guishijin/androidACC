package com.ylsk.androidacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 *  SensorManager.getOrientation
 */
public class CompassActivity extends Activity implements SensorEventListener {
    private TextView mTvInfo;

    private static final String TAG = "CompassActivity";
    //记录rotationMatrix矩阵值
    private float[] r = new float[9];
    //记录通过getOrientation()计算出来的方位横滚俯仰值
    private float[] values = new float[3];

    //------------------------------------------------------
    // 三者关系应该为： a = g+l
    // 加速度
    private float[] acceleration = null;
    // 重力加速度
    private float[] gravity = null;
    // 线性加速度
    private float[] linerAcceleration = null;

    // 旋转向量
    private float[] rotation = null;
    //------------------------------------------------------

    private float[] geomagnetic = null;
    // 定义真机的Sensor管理器
    private SensorManager mSensorManager;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            float angle = 0;
            if(gravity!=null && geomagnetic!=null) {
                if(SensorManager.getRotationMatrix(r, null, gravity, geomagnetic)) {
                    SensorManager.getOrientation(r, values);
                    float degree = (float) ( ( 360f + values[0] * 180f / Math.PI ) % 360 );
                    //Log.i(TAG, "计算出来的方位角＝" + degree);
                    angle = degree;
                }
            }

            if(acceleration != null && gravity != null && linerAcceleration != null && rotation != null)
            {
                float glx = gravity[0] + linerAcceleration[0];
                float gly = gravity[1] + linerAcceleration[1];
                float glz = gravity[2] + linerAcceleration[2];

                float gm = (float)Math.sqrt(gravity[0]*gravity[0] + gravity[1]*gravity[1] + gravity[2]*gravity[2]);
                float am = (float)Math.sqrt(acceleration[0]*acceleration[0] + acceleration[1]*acceleration[1] + acceleration[2]*acceleration[2]);
                float lm = (float)Math.sqrt(linerAcceleration[0]*linerAcceleration[0] + linerAcceleration[1]*linerAcceleration[1] + linerAcceleration[2]*linerAcceleration[2]);
                float rm = (float)Math.sqrt(rotation[0]*rotation[0] + rotation[1]*rotation[1] + rotation[2]*rotation[2]);

                Log.i(TAG,"\n==========: g="+gm+",   ("+acceleration[0]+","+acceleration[1]+","+acceleration[2]+") ,("+glx+","+gly+","+glz+")");

                mTvInfo.setText("====================");
                String anglestr = (angle+"").substring(0,(angle+"").indexOf("."));
                mTvInfo.append("\n 磁场 \n angle = " + anglestr +"（度）");
                mTvInfo.append("\n====================");
                mTvInfo.append("\n |g| = " + gm);
                mTvInfo.append("\n g.x = " + gravity[0]);
                mTvInfo.append("\n g.y = " + gravity[1]);
                mTvInfo.append("\n g.z = " + gravity[2]);
                mTvInfo.append("\n====================");
                mTvInfo.append("\n |a| = " + am);
                mTvInfo.append("\n a.x = " + acceleration[0]);
                mTvInfo.append("\n a.y = " + acceleration[1]);
                mTvInfo.append("\n a.z = " + acceleration[2]);
                mTvInfo.append("\n====================");
                mTvInfo.append("\n |l| = " + lm);
                mTvInfo.append("\n l.x = " + linerAcceleration[0]);
                mTvInfo.append("\n l.y = " + linerAcceleration[1]);
                mTvInfo.append("\n l.z = " + linerAcceleration[2]);
                mTvInfo.append("\n====================");
                mTvInfo.append("\n |l| = " + rm);
                mTvInfo.append("\n r.x = " + rotation[0]);
                mTvInfo.append("\n r.y = " + rotation[1]);
                mTvInfo.append("\n r.z = " + rotation[2]);
                //mTvInfo.append("\n ("+acceleration[0]+","+acceleration[1]+","+acceleration[2]+") \n ("+glx+","+gly+","+glz+")");

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mTvInfo = (TextView)findViewById(R.id.tv_info);

        // 获取真机的传感器管理服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册加速度传感器监听
        Sensor acceleSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensorManager.registerListener(this, acceleSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, acceleSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, acceleSensor, SensorManager.SENSOR_DELAY_UI);

        // 注册重力加速度传感器监听
        Sensor gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        //mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI);

        // TYPE_LINEAR_ACCELERATION
        Sensor linerAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //mSensorManager.registerListener(this, linerAccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, linerAccelerationSensor, SensorManager.SENSOR_DELAY_UI);

        // TYPE_ROTATION_VECTOR
        Sensor rotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_UI);


        //注册磁场传感器监听
        Sensor magSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //mSensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 取消所有注册
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度传感器
                acceleration = event.values;
                handler.sendEmptyMessage(0);
                break;
            case Sensor.TYPE_GRAVITY: //重力加速度传感器
                gravity = event.values;
                handler.sendEmptyMessage(0);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION: //线性加速度
                linerAcceleration = event.values;
                handler.sendEmptyMessage(0);
                break;
            case Sensor.TYPE_ROTATION_VECTOR://旋转
                rotation = event.values;
                handler.sendEmptyMessage(0);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD://磁场传感器
                geomagnetic = event.values;
                handler.sendEmptyMessage(0);
                break;
        }
    }
}
