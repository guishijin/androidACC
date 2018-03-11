package com.ylsk.apache.math.fft;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author gsj
 */
public class TimeCostCalculator
{

    /**
     * 构造函数
     */
    public TimeCostCalculator()
    {
    }

    /**
     * 计算指定对象的运行时间开销。
     *
     * @param testCase 指定被测对象。
     * @return 返回sub.run的时间开销，单位为s。
     * @throws Exception
     */
    public double calcTimeCost(ITestCase testCase) throws Exception
    {
        List<Object> params = testCase.getParams();
        long startTime = System.nanoTime();
        Complex[] result = (Complex[]) testCase.run(params);

        long stopTime = System.nanoTime();
        System.out.println("start: " + startTime + " / stop: " + stopTime);
        double timeCost = (stopTime - startTime) * 1.0e-9;
        // double timeCost = BigDecimal.valueOf(stopTime - startTime, 9).doubleValue();          

        for (int i = 0; i < result.length; i++)
        {
            Complex temp = result[i];
            System.out.println("Complex[ " + i + " ] abs = "+temp.abs()+" | " + temp.toString()+" - Real = "+temp.getReal()+", Imaginary = "+temp.getImaginary());
        }

        // 过虑掉第6个
        //result[5] = new Complex(0.0);
        
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] result2 = fft.transform(result, TransformType.INVERSE);
        
        System.out.println("反向变换");
        for (int i = 0; i < result2.length; i++)
        {
            Complex temp = result2[i];
            System.out.println("Complex[ " + i + " ] abs = "+temp.abs()+" | " + temp.toString());
        }
        
        return timeCost;
    }

    public static void main(String[] args) throws Exception
    {
        TimeCostCalculator tcc = new TimeCostCalculator();
        double timeCost;

        System.out.println("--------------------------------------------------------------------------");
        //timeCost = tcc.calcTimeCost(new CalcFFT());
        //timeCost = tcc.calcTimeCost(new CalcFFT(32));
        double[] datas =
        {
            //1, 1.2, 0.1, 1.2, 0.1, 0.21, 1.05, 1.09
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.4,
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.0,
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.4,
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.0,
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.4,
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.0,
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.4,
            1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.2, 1.0
        };
        timeCost = tcc.calcTimeCost(new CalcFFT(datas));
        System.out.println("time cost is: " + timeCost + "s");
        System.out.println("--------------------------------------------------------------------------");

//        
//        
//        // 音频模拟数据
//        double[] xr1ght =
//        {
//            15724,
//            5963,
//            10566,
//            -5873,
//            12772,
//            11644,
//            25465,
//            -3866,
//            20357,
//            26956,
//            23135,
//            18000,
//            34314,
//            20428,
//            11993,
//            18948,
//            14801,
//            25335,
//            1561,
//            1338,
//            16658,
//            -1113,
//            9455,
//            23564,
//            -4610,
//            8771,
//            7266,
//            4793,
//            4726,
//            4813,
//            6817,
//            23457,
//            464,
//            -4812,
//            -1721,
//            -13123,
//            10605,
//            -10163,
//            -3558,
//            10337,
//            -4765,
//            3995,
//            2789,
//            1551,
//            10328,
//            33231,
//            23535,
//            5830,
//            14266,
//            26770,
//            28313,
//            15983,
//            2767,
//            18373,
//            19276,
//            33410,
//            27975,
//            18312,
//            1269,
//            22674,
//            7295,
//            2952,
//            7208,
//            5313,
//            -8271,
//            10149,
//            13110,
//            163,
//            11524,
//            -9564,
//            -15561,
//            -16080,
//            -6263,
//            -15522,
//            3518,
//            -18523,
//            -12823,
//            -2574,
//            -4046,
//            -6176,
//            -25527,
//            -3914,
//            -25030,
//            -21466,
//            -19816,
//            3007,
//            -1853,
//            -1981,
//            -7355,
//            -14343,
//            -1919,
//            -5438,
//            4773,
//            10259,
//            4617,
//            -10258,
//            13662,
//            -34872,
//            711l,
//            641,
//            18042,
//            4524,
//            -17037,
//            -13620,
//            21118,
//            1780,
//            4582,
//            -8892,
//            -21186,
//            -16446,
//            -20312,
//            -17560,
//            -14117,
//            -23643,
//            -20709,
//            -9037,
//            -19048,
//            -30367,
//            -30730,
//            -12902,
//            -8516,
//            -24199,
//            -35567,
//            533,
//            -8327,
//            -36613,
//            -29423,
//            -8775,
//            -15565,
//            -20939,
//            9533,
//            -2165,
//            9019,
//            8192,
//            -11218,
//            -6974,
//            -8021,
//            23752,
//            15752,
//            3446,
//            -6975,
//            2967,
//            20725,
//            3759,
//            11186,
//            2631,
//            17566,
//            17218,
//            1108,
//            3248,
//            28130,
//            -8391,
//            14573,
//            17172,
//            12184,
//            16067,
//            -2520,
//            11109,
//            -4449,
//            -6633,
//            -15209,
//            9046,
//            -4561,
//            6586,
//            81,
//            2844,
//            -8295,
//            -3949,
//            -16965,
//            2692,
//            2110,
//            -20482,
//            8275,
//            -3,
//            15240,
//            10330,
//            10634,
//            7641,
//            15571,
//            34142,
//            -3144,
//            1335,
//            3967,
//            10775,
//            23829,
//            44361,
//            9715,
//            24660,
//            9386,
//            12504,
//            2658,
//            16495,
//            2991,
//            6612,
//            10542,
//            29195,
//            18975,
//            10546,
//            5146,
//            8987,
//            11267,
//            14728,
//            -400,
//            -3750,
//            3423,
//            4240,
//            9294,
//            -1022,
//            -10012,
//            10883,
//            -10217,
//            10586,
//            -5991,
//            -8655,
//            -4431,
//            -10768,
//            16803,
//            -31119,
//            9179,
//            -4021,
//            10225,
//            6424,
//            8905,
//            3408,
//            18741,
//            648,
//            608,
//            16994,
//            13133,
//            15278,
//            22657,
//            18771,
//            -4734,
//            -7269,
//            20490,
//            14890,
//            471,
//            4417,
//            -24941,
//            -7648,
//            -6196,
//            17551,
//            -9208,
//            -20127,
//            -3003,
//            -17522,
//            -11985,
//            -11828,
//            -12800,
//            -34994,
//            -14923,
//            -27122,
//            -12043,
//            -31284,
//            -27869,
//            -34964
//        };
//        
//        TimeCostCalculator tcc2 = new TimeCostCalculator();
//        double timeCost2;
//
//        System.out.println("--------------------------------------------------------------------------");
//        double[] datas2 = xr1ght;
//        timeCost2 = tcc.calcTimeCost(new CalcFFT(datas2));
//        System.out.println("time cost2 is: " + timeCost2 + "s");
//        System.out.println("--------------------------------------------------------------------------");
    }
}
