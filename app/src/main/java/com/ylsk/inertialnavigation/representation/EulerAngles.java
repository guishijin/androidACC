package com.ylsk.inertialnavigation.representation;

/**
 * 欧拉角
 *
 *   roll：绕x轴
 *   pitch：绕y轴
 *   yaw：绕z轴
 *
 * 手机加速计的roll,yaw,pitch
 *
 *    roll:  绕着垂直于手机屏幕的轴旋转
 *    yaw:   绕着手机的向上方向旋转
 *    pitch: 改变手机的俯仰
 */
public class EulerAngles {

    private float yaw;
    private float pitch;
    private float roll;

    public EulerAngles(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }
}
