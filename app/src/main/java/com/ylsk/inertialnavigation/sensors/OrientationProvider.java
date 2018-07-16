/**
 *
 */
package com.ylsk.inertialnavigation.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ylsk.inertialnavigation.representation.EulerAngles;
import com.ylsk.inertialnavigation.representation.Matrixf4x4;
import com.ylsk.inertialnavigation.representation.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * Classes implementing this interface provide an orientation of the device
 * either by directly accessing hardware, using Android sensor fusion or fusing
 * sensors itself.
 * <p/>
 * The orientation can be provided as rotation matrix or quaternion.
 *
 * @author Alexander Pacha
 */
public abstract class OrientationProvider implements SensorEventListener {
    /**
     * Sync-token for syncing read/write to sensor-data from sensor manager and
     * fusion algorithm
     */
    protected final Object syncToken = new Object();
    /**
     * The matrix that holds the current rotation
     */
    protected final Matrixf4x4 currentOrientationRotationMatrix;
    /**
     * The quaternion that holds the current rotation
     */
    protected final Quaternion currentOrientationQuaternion;
    /**
     * The list of sensors used by this provider
     *  传感器列表
     */
    protected List<Sensor> sensorList = new ArrayList<Sensor>();
    /**
     * The sensor manager for accessing android sensors
     * 用于访问android传感器的传感器管理对象
     */
    protected SensorManager sensorManager;

    /**
     * Initialises a new OrientationProvider
     * 构造函数，实例化一个新的OrientationProvider对象
     *
     * @param sensorManager The android sensor manager
     */
    public OrientationProvider(SensorManager sensorManager) {

        // 保存传感器管理器对象
        this.sensorManager = sensorManager;

        // 初始化
        // Initialise with identity
        currentOrientationRotationMatrix = new Matrixf4x4();

        // 初始化
        // Initialise with identity
        currentOrientationQuaternion = new Quaternion();
    }

    /**
     * Starts the sensor fusion (e.g. when resuming the activity)
     * 启动传感器融合
     */
    public void start() {
        // enable our sensor when the activity is resumed, ask for
        // 10 ms updates.
        for (Sensor sensor : sensorList) {
            // enable our sensors when the activity is resumed, ask for
            // 20 ms updates (Sensor_delay_game)
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /**
     * 停止传感器融合
     * Stops the sensor fusion (e.g. when pausing/suspending the activity)
     */
    public void stop() {
        // make sure to turn our sensors off when the activity is paused
        for (Sensor sensor : sensorList) {
            sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not doing anything
    }

    /**
     * 获取旋转矩阵
     * @return Returns the current rotation of the device in the rotation matrix
     * format (4x4 matrix)
     */
    public Matrixf4x4 getRotationMatrix() {
        synchronized (syncToken) {
            return currentOrientationRotationMatrix;
        }
    }

    /**
     * 获取四元数
     * @return Returns the current rotation of the device in the quaternion
     * format (vector4f)
     */
    public Quaternion getQuaternion() {
        synchronized (syncToken) {
            return currentOrientationQuaternion.clone();
        }
    }

    /**
     * 获取手机设备当前姿态的欧拉角
     * @return Returns the current rotation of the device in the Euler-Angles
     */
    public EulerAngles getEulerAngles() {
        synchronized (syncToken) {
            float[] angles = new float[3];
            SensorManager.getOrientation(currentOrientationRotationMatrix.matrix, angles);
            return new EulerAngles(angles[0], angles[1], angles[2]);
        }
    }
}
