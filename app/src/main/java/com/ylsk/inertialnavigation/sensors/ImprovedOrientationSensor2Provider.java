package com.ylsk.inertialnavigation.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import com.ylsk.inertialnavigation.representation.Quaternion;

/**
 * The orientation provider that delivers the absolute orientation from the {@link Sensor#TYPE_GYROSCOPE
 * Gyroscope} and {@link Sensor#TYPE_ROTATION_VECTOR Android Rotation Vector sensor}.
 * 该类提供绝对方向。使用的是陀螺仪传感器和旋转向量传感器来综合获得。
 * <p/>
 * It mainly relies on the gyroscope, but corrects with the Android Rotation Vector which also provides an absolute
 * estimation of current orientation. The correction is a static weight.
 * 它主要依靠陀螺仪，但用android旋转向量修正，它也提供了一个绝对当前方向的估计.校正是一个静态的重量。
 *
 * @author Alexander Pacha
 */
public class ImprovedOrientationSensor2Provider extends OrientationProvider {

    /**
     * Constant specifying the factor between a Nano-second and a second
     * 纳秒和秒的转换因数
     */
    private static final float NS2S = 1.0f / 1000000000.0f;

    /**
     * This is a filter-threshold for discarding Gyroscope measurements that are below a certain level and
     * potentially are only noise and not real motion. Values from the gyroscope are usually between 0 (stop) and
     * 10 (rapid rotation), so 0.1 seems to be a reasonable threshold to filter noise (usually smaller than 0.1) and
     * real motion (usually > 0.1). Note that there is a chance of missing real motion, if the use is turning the
     * device really slowly, so this value has to find a balance between accepting noise (threshold = 0) and missing
     * slow user-action (threshold > 0.5). 0.1 seems to work fine for most applications.
     */
    private static final double EPSILON = 0.1f;

    /**
     * This weight determines indirectly how much the rotation sensor will be used to correct. This weight will be
     * multiplied by the velocity to obtain the actual weight. (in sensor-fusion-scenario 2 -
     * SensorSelection.GyroscopeAndRotationVector2).
     * Must be a value between 0 and approx. 0.04 (because, if multiplied with a velocity of up to 25, should be still
     * less than 1, otherwise the SLERP will not correctly interpolate). Should be close to zero.
     */
    private static final float INDIRECT_INTERPOLATION_WEIGHT = 0.01f;
    /**
     * The threshold that indicates an outlier of the rotation vector. If the dot-product between the two vectors
     * (gyroscope orientation and rotationVector orientation) falls below this threshold (ideally it should be 1,
     * if they are exactly the same) the system falls back to the gyroscope values only and just ignores the
     * rotation vector.
     * <p/>
     * This value should be quite high (> 0.7) to filter even the slightest discrepancies that causes jumps when
     * tiling the device. Possible values are between 0 and 1, where a value close to 1 means that even a very small
     * difference between the two sensors will be treated as outlier, whereas a value close to zero means that the
     * almost any discrepancy between the two sensors is tolerated.
     */
    private static final float OUTLIER_THRESHOLD = 0.85f;
    /**
     * The threshold that indicates a massive discrepancy between the rotation vector and the gyroscope orientation.
     * If the dot-product between the two vectors
     * (gyroscope orientation and rotationVector orientation) falls below this threshold (ideally it should be 1, if
     * they are exactly the same), the system will start increasing the panic counter (that probably indicates a
     * gyroscope failure).
     * <p/>
     * This value should be lower than OUTLIER_THRESHOLD (0.5 - 0.7) to only start increasing the panic counter,
     * when there is a huge discrepancy between the two fused sensors.
     */
    private static final float OUTLIER_PANIC_THRESHOLD = 0.75f;
    /**
     * The threshold that indicates that a chaos state has been established rather than just a temporary peak in the
     * rotation vector (caused by exploding angled during fast tilting).
     * <p/>
     * If the chaosCounter is bigger than this threshold, the current position will be reset to whatever the
     * rotation vector indicates.
     */
    private static final int PANIC_THRESHOLD = 60;
    private static float[] RMatrixRemapped = new float[16];
    private static float[] orientation = new float[3];
    /**
     * The quaternion that stores the difference that is obtained by the gyroscope.
     * Basically it contains a rotational difference encoded into a quaternion.
     * <p/>
     * To obtain the absolute orientation one must add this into an initial position by
     * multiplying it with another quaternion
     */
    private final Quaternion deltaQuaternion = new Quaternion();
    /**
     * The Quaternions that contain the current rotation (Angle and axis in Quaternion format) of the Gyroscope
     */
    private Quaternion quaternionGyroscope = new Quaternion();
    /**
     * The quaternion that contains the absolute orientation as obtained by the rotationVector sensor.
     */
    private Quaternion quaternionRotationVector = new Quaternion();
    /**
     * The time-stamp being used to record the time when the last gyroscope event occurred.
     */
    private long timestamp;
    /**
     * Value giving the total velocity of the gyroscope (will be high, when the device is moving fast and low when
     * the device is standing still). This is usually a value between 0 and 10 for normal motion. Heavy shaking can
     * increase it to about 25. Keep in mind, that these values are time-depended, so changing the sampling rate of
     * the sensor will affect this value!
     */
    private double gyroscopeRotationVelocity = 0;
    /**
     * Flag indicating, whether the orientations were initialised from the rotation vector or not. If false, the
     * gyroscope can not be used (since it's only meaningful to calculateAzimuth differences from an initial state). If
     * true,
     * the gyroscope can be used normally.
     */
    private boolean positionInitialised = false;
    /**
     * Counter that sums the number of consecutive frames, where the rotationVector and the gyroscope were
     * significantly different (and the dot-product was smaller than 0.7). This event can either happen when the
     * angles of the rotation vector explode (e.g. during fast tilting) or when the device was shaken heavily and
     * the gyroscope is now completely off.
     */
    private int panicCounter;


    /**
     * Initialises a new ImprovedOrientationSensor2Provider
     * 构造函数，实例化一个ImprovedOrientationSensor2Provider类型的对象
     *
     * @param sensorManager The android sensor manager
     */
    public ImprovedOrientationSensor2Provider(SensorManager sensorManager) {
        super(sensorManager);

        // 添加陀螺仪传感器和旋转向量传感器
        //Add the gyroscope and rotation Vector
        sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorList.add(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
    }

    /**
     * 实现SensorEventListener.onSensorChanged方法
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        // 根据传感器类型处理回调数据
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            // 处理旋转向量（这里仅仅是保存下来）
            // Process rotation vector (just safe it)

            float[] q = new float[4];
            // Calculate angle. Starting with API_18, Android will provide this value as event.values[3], but if not, we have to calculateAzimuth it manually.
            // 计算角度。从API_18开始，andoid通过event.values[3]提供。
            // 但是，如果没有的话，我们必须手动计算。
            SensorManager.getQuaternionFromVector(q, event.values);

            // Store in quaternion
            // 保存到四位数中
            quaternionRotationVector.setXYZW(q[1], q[2], q[3], -q[0]);
            if (!positionInitialised) {
                // Override
                quaternionGyroscope.set(quaternionRotationVector);
                positionInitialised = true;
            }

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // 处理陀螺仪数据，并进行数据的融合（将陀螺仪和旋转向量传感器数据进行融合）
            // Process Gyroscope and perform fusion

            // This timestep's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            if (timestamp != 0) {

                // 计算当前时间的时间和上次事件的时间差
                final float dT = (event.timestamp - timestamp) * NS2S;

                // Axis of the rotation sample, not normalized yet.
                // 旋转轴的采样值，尚未归一化
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                // Calculate the angular speed of the sample
                // 计算采样数据的角速度
                gyroscopeRotationVelocity = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                // 如果数据的模大于设定的阈值EPSILON，则对旋转向量进行归一化处理
                if (gyroscopeRotationVelocity > EPSILON) {
                    axisX /= gyroscopeRotationVelocity;
                    axisY /= gyroscopeRotationVelocity;
                    axisZ /= gyroscopeRotationVelocity;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                // 通过时间步法将这个轴与角速度结合在一起，
                // 以便从这个样本中得到一个三角形在时间步法上的旋转，
                // 我们将把这个三角形旋转的轴角表示转换为四元数，
                // 然后再将它转化为旋转矩阵。
                double thetaOverTwo = gyroscopeRotationVelocity * dT / 2.0f;
                double sinThetaOverTwo = Math.sin(thetaOverTwo);
                double cosThetaOverTwo = Math.cos(thetaOverTwo);
                deltaQuaternion.setX((float) (sinThetaOverTwo * axisX));
                deltaQuaternion.setY((float) (sinThetaOverTwo * axisY));
                deltaQuaternion.setZ((float) (sinThetaOverTwo * axisZ));
                deltaQuaternion.setW(-(float) cosThetaOverTwo);

                // Move current gyro orientation
                deltaQuaternion.multiplyByQuat(quaternionGyroscope, quaternionGyroscope);

                // Calculate dot-product to calculateAzimuth whether the two orientation sensors have diverged
                // (if the dot-product is closer to 0 than to 1), because it should be close to 1 if both are the same.
                // 计算点积计算方位传感器是否发散,(如果点积接近0比1），因为如果两个都相同，则它应该接近1。
                float dotProd = quaternionGyroscope.dotProduct(quaternionRotationVector);

                // If they have diverged, rely on gyroscope only (this happens on some devices when the rotation vector "jumps").
                // 如果它们有分歧，只依赖陀螺仪（当旋转矢量“跳跃”时，在某些设备上会发生这种情况）。
                if (Math.abs(dotProd) < OUTLIER_THRESHOLD) {
                    // Increase panic counter
                    // 增加惩罚计数
                    if (Math.abs(dotProd) < OUTLIER_PANIC_THRESHOLD) {
                        panicCounter++;
                    }

                    // Directly use Gyro
                    // 直接使用陀螺仪
                    setOrientationQuaternionAndMatrix(quaternionGyroscope);

                } else {
                    // Both are nearly saying the same. Perform normal fusion.
                    // 比较接近，执行融合

                    // Interpolate with a fixed weight between the two absolute quaternions obtained from gyro and rotation vector sensors
                    // The weight should be quite low, so the rotation vector corrects the gyro only slowly, and the output keeps responsive.
                    // 由陀螺和旋转矢量传感器获得的两个绝对四元数之间的固定权值插值
                    // 权值应该相当低，因此旋转矢量只缓慢地校正陀螺仪，并且输出保持响应。

                    Quaternion interpolate = new Quaternion();
                    // 进行插值操作
                    quaternionGyroscope.slerp(quaternionRotationVector, interpolate,
                            (float) (INDIRECT_INTERPOLATION_WEIGHT * gyroscopeRotationVelocity));

                    // Use the interpolated value between gyro and rotationVector
                    // 利用陀螺与旋转矢量之间的插值值
                    setOrientationQuaternionAndMatrix(interpolate);
                    // Override current gyroscope-orientation
                    // 覆盖当前的陀螺仪定向
                    quaternionGyroscope.copyVec4(interpolate);

                    // Reset the panic counter because both sensors are saying the same again
                    // 重置惩罚计数
                    panicCounter = 0;
                }

                // 惩罚计数大于惩罚阈值时，执行惩罚操作。
                if (panicCounter > PANIC_THRESHOLD) {
                    Log.d("Rotation Vector",
                            "Panic counter is bigger than threshold; this indicates a Gyroscope failure. Panic reset is imminent.");

                    if (gyroscopeRotationVelocity < 3) {
                        Log.d("Rotation Vector",
                                "Performing Panic-reset. Resetting orientation to rotation-vector value.");

                        // Manually set position to whatever rotation vector says.
                        setOrientationQuaternionAndMatrix(quaternionRotationVector);
                        // Override current gyroscope-orientation with corrected value
                        quaternionGyroscope.copyVec4(quaternionRotationVector);

                        panicCounter = 0;
                    } else {
                        Log.d("Rotation Vector",
                                String.format(
                                        "Panic reset delayed due to ongoing motion (user is still shaking the device). Gyroscope Velocity: %.2f > 3",
                                        gyroscopeRotationVelocity));
                    }
                }
            }

            // 记录当前事件发生的时间
            timestamp = event.timestamp;
        }
    }

    /**
     * Sets the output quaternion and matrix with the provided quaternion and synchronises the setting
     * 用所提供的四元数设置输出四元数和矩阵并同步设置
     *
     * @param quaternion The Quaternion to set (the result of the sensor fusion)
     */
    private void setOrientationQuaternionAndMatrix(Quaternion quaternion) {
        Quaternion correctedQuat = quaternion.clone();
        // We inverted w in the deltaQuaternion, because currentOrientationQuaternion required it.
        // Before converting it back to matrix representation, we need to revert this process
        correctedQuat.w(-correctedQuat.w());

        synchronized (syncToken) {
            // Use gyro only
            currentOrientationQuaternion.copyVec4(quaternion);

            // Set the rotation matrix as well to have both representations
            SensorManager.getRotationMatrixFromVector(currentOrientationRotationMatrix.matrix, correctedQuat.ToArray());
        }
    }

    public float getAzimuth(float decl) {
        SensorManager.remapCoordinateSystem(currentOrientationRotationMatrix.matrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, RMatrixRemapped);
        SensorManager.getOrientation(RMatrixRemapped, orientation);
        if (orientation[0] >= 0) {
            // Azimuth-Calculation (rad in degree) + difference to true north (decl)
            return (orientation[0] * 57.29577951f + decl);
        } else {
            // Azimuth-Calculation (rad in degree) +360 + difference to true north (decl)
            return (orientation[0] * 57.29577951f + 360 + decl);
        }

    }
}
