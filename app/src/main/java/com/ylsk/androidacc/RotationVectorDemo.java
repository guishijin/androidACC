package com.ylsk.androidacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RotationVectorDemo extends Activity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_rotation_vector_demo);
//    }

    private GLSurfaceView mGLSurfaceView;
    private SensorManager mSensorManager;
    private MyRenderer mRenderer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //获取SensorManager的实例
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        // 创建我们的预览视图并将其设置为我们的内容活动页面
        mRenderer = new MyRenderer();
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(mRenderer);
        setContentView(mGLSurfaceView);
    }


    @Override
    protected void onResume() {
        // 理想情况下，游戏应该实现onResume()和onPause()在活动发生时采取适当的行动
        super.onResume();
        mRenderer.start();
        mGLSurfaceView.onResume();
    }


    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mRenderer.stop();
        mGLSurfaceView.onPause();
    }

    class MyRenderer implements GLSurfaceView.Renderer, SensorEventListener {
        private Cube mCube;
        private Sensor mRotationVectorSensor;
        private final float[] mRotationMatrix = new float[16];


        public MyRenderer() {
            //找到旋转矢量传感器
            mRotationVectorSensor = mSensorManager.getDefaultSensor(
                    Sensor.TYPE_ROTATION_VECTOR);


            mCube = new Cube();
            // 将旋转矩阵初始化为恒等式
            mRotationMatrix[0] = 1;
            mRotationMatrix[4] = 1;
            mRotationMatrix[8] = 1;
            mRotationMatrix[12] = 1;
        }


        public void start() {
            // 激活我们的传感器当活动恢复时，请求10 ms更新。

            mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
        }


        public void stop() {
            // 当活动暂停时，请确保将传感器关闭
            mSensorManager.unregisterListener(this);
        }


        public void onSensorChanged(SensorEvent event) {
            // /我们收到了一个传感器事件。检查我们是否收到了适当的活动是一个很好的做法
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // 将旋转矢量转换为4x4矩阵。这个矩阵被打开的GL解释为旋转矢量的倒数，这就是我们想要的。
                SensorManager.getRotationMatrixFromVector(
                        mRotationMatrix, event.values);
            }
        }


        public void onDrawFrame(GL10 gl) {
            // 清除屏幕
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);


            // 设置modelview矩阵
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, -3.0f);
            gl.glMultMatrixf(mRotationMatrix, 0);


            // 画出我们的目标
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);


            mCube.draw(gl);
        }


        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // 设置视图端口
            gl.glViewport(0, 0, width, height);
            //设置投影矩阵
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        }


        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // dither是默认启用的，我们不需要它
            gl.glDisable(GL10.GL_DITHER);
            // 清除屏幕
            gl.glClearColor(1, 1, 1, 1);
        }



        class Cube {
            // 初始化我们多维数据集
            private FloatBuffer mVertexBuffer;
            private FloatBuffer mColorBuffer;
            private ByteBuffer mIndexBuffer;


            public Cube() {
                final float vertices[] = {
                        -1, -1, -1, 1, -1, -1,
                        1, 1, -1, -1, 1, -1,
                        -1, -1, 1, 1, -1, 1,
                        1, 1, 1, -1, 1, 1,
                };


                final float colors[] = {
                        0, 0, 0, 1, 1, 0, 0, 1,
                        1, 1, 0, 1, 0, 1, 0, 1,
                        0, 0, 1, 1, 1, 0, 1, 1,
                        1, 1, 1, 1, 0, 1, 1, 1,
                };


                final byte indices[] = {
                        0, 4, 5, 0, 5, 1,
                        1, 5, 6, 1, 6, 2,
                        2, 6, 7, 2, 7, 3,
                        3, 7, 4, 3, 4, 0,
                        4, 7, 6, 4, 6, 5,
                        3, 0, 1, 3, 1, 2
                };


                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
                vbb.order(ByteOrder.nativeOrder());
                mVertexBuffer = vbb.asFloatBuffer();
                mVertexBuffer.put(vertices);
                mVertexBuffer.position(0);


                ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
                cbb.order(ByteOrder.nativeOrder());
                mColorBuffer = cbb.asFloatBuffer();
                mColorBuffer.put(colors);
                mColorBuffer.position(0);


                mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
                mIndexBuffer.put(indices);
                mIndexBuffer.position(0);
            }


            public void draw(GL10 gl) {
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glFrontFace(GL10.GL_CW);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
            }
        }


        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }
}
