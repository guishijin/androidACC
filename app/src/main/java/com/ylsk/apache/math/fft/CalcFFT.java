package com.ylsk.apache.math.fft;

import java.util.List;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * 测试用例实例
 * @author gsj
 */
public class CalcFFT implements ITestCase
{

    public CalcFFT()
    {
        System.out.print("本算例用于计算快速傅立叶变换。正在初始化 计算数据(" + arrayLength + "点)... ...");
        inputData = new double[arrayLength];
        for (int index = 0; index < inputData.length; index++)
        {
            inputData[index] = (Math.random() - 0.5) * 100.0;
        }
        System.out.println("初始化完成");
    }

    public CalcFFT(int arrayLength)
    {
        this.arrayLength = arrayLength;
        
        System.out.print("本算例用于计算快速傅立叶变换。正在初始化 计算数据(" + arrayLength + "点)... ...");
        inputData = new double[arrayLength];
        for (int index = 0; index < inputData.length; index++)
        {
            inputData[index] = (Math.random() - 0.5) * 100.0;
        }
        System.out.println("初始化完成");
    }
    
    
    public CalcFFT(double[] datas)
    {
        this.arrayLength = datas.length;
        this.inputData = new double[this.arrayLength];
        System.arraycopy(datas, 0, this.inputData, 0, this.arrayLength);
        
        System.out.print("本算例用于计算快速傅立叶变换。正在初始化 计算数据(" + arrayLength + "点)... ...");
        System.out.println("初始化完成");
    }

    @Override
    public List<Object> getParams()
    {
        return null;
    }

    @Override
    public Object run(List<Object> params) throws Exception
    {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] result = fft.transform(inputData, TransformType.FORWARD);
        return result;
    }

    /** 测试数据 **/
    private double[] inputData = null;



    /** 测试数据长度 **/
    //private final int arrayLength = 4 * 1024 * 1024;
    private int arrayLength = 4 * 1024 * 1024;

    public int getArrayLength()
    {
        return arrayLength;
    }
    
    public void setArrayLength(int arrayLength)
    {
        this.arrayLength = arrayLength;
    }

    public double[] getInputData() {
        return inputData;
    }
}
