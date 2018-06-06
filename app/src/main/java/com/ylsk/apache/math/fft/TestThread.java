package com.ylsk.apache.math.fft;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by gsj on 2018/3/11.
 */

public class TestThread extends Thread {

    private final CalcFFT fft;

    /**
     *
     * @param obj
     */
    public TestThread(CalcFFT obj)
    {
        this.fft = obj;
    }
    
    @Override
    public void run() {
        List<Object> params = this.fft.getParams();
        try {

            String dir = "androidACC";
            String filename = this.getName() + ".txt";
            FileOutputStream fos = new FileOutputStream(FileUtil.CreateFile(dir,filename));
            // 原始数据 写入
            String info1 = "原始数据：\r\n";
            fos.write(info1.getBytes());

            for(double d:this.fft.getInputData())
            {
                // 写入到文件
                String line = d + ",\r\n";
                fos.write(line.getBytes());
            }
            fos.flush();

//            // 变换后数据 写入
//            String info2 = "变换后数据：\r\n";
//            fos.write(info2.getBytes());
//
//            Complex[] result = (Complex[])this.fft.run(params);
//
//            for(Complex data : result)
//            {
//                Log.d("傅里叶变换结果:",data.abs() + " | " + data.toString());
//                String line = "傅里叶变换结果:"+data.abs() + " | " + data.toString()+"\r\n";
//                fos.write(line.getBytes());
//            }
//            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
