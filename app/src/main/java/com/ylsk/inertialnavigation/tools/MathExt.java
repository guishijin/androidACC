package com.ylsk.inertialnavigation.tools;

import java.math.BigDecimal;

/**
 * 扩展类
 */
public class MathExt {

    /**
     * 按照四舍五入算法对输入的浮点数进行保留5位小数的取舍计算
     * @param inf
     * @return
     */
    public static float FRount(float inf)
    {
        return FRount(inf,5,4);
    }

    /**
     * 按照四舍五入算法对输入的浮点数和小数位数进行取舍计算
     * @param inf
     * @param bit
     * @return
     */
    public static float FRount(float inf, int bit)
    {
        return FRount(inf, bit,4);
    }

    /**
     * 取舍小数位数
     * @param inf 输入的浮点数
     * @param bit 小数的位数
     * @param roundMode 取舍算法
     * @return 处理后浮点数
     */
    public static float FRount(float inf, int bit, int roundMode) {
        float ft = inf;

        //设置位数
        int scale = bit;
        //表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
        int roundingMode = roundMode;
        BigDecimal bd = new BigDecimal((double) ft);
        bd = bd.setScale(scale,roundingMode);
        ft = bd.floatValue();
        return ft;
    }


    /**
     * 计算从v2到v1的夹角公式：θ=atan2(v2.y,v2.x)−atan2(v1.y,v1.x)
     * 需要注意的是：atan2的取值范围是[−π,π]，在进行相减之后得到的夹角是在[−2π,2π]，因此当得到的结果大于π时，对结果减去2π，当结果小于−π时，对结果加上2π
     * 然后将角度变换到 0-2π范围
     *
     * @param x1 向量v1.x
     * @param y1 向量v1.y
     * @param x2 向量v2.x
     * @param y2 向量v2.y
     * @return 夹角 0-2π范围
     */
    public static double CaculateAngle(float x1, float y1, float x2, float y2)
    {
        double angle = 0;
        angle = Math.atan2(y2,x2) - Math.atan2(y1,x1);

        if(angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        else if(angle < (-Math.PI)) {
            angle += 2*Math.PI;
        }

        return (angle + Math.PI*2) % (Math.PI*2);
    }

    /**
     * 计算向量V和y轴夹角
     * @param x 向量V.x
     * @param y 向量V.y
     * @return 角度 ，范围 [-PI, PI]
     */
    public static double CaculateYxisAngle(float x, float y)
    {
        return CaculateAngle( x, y, 0,1);
    }

    /**
     * 计算向量V和x轴夹角
     * @param x 向量V.x
     * @param y 向量V.y
     * @return 角度 ，范围 [-PI, PI]
     */
    public static double CaculateXxisAngle(float x, float y)
    {
        return CaculateAngle( x, y, 1,0);
    }


    /**
     * 计算向量V（x，y）和y轴的夹角，返回度数表示的角度值
     * @param x 向量V.x
     * @param y 向量V.y
     * @return 角度值 0-359
     */
    public static float CaculateYxisAngleAsDegree(float x, float y)
    {
        double a = CaculateYxisAngle(x,y);
        return MathExt.FRount((float)Math.toDegrees((float)a),2);
    }

    /**
     * 测试
     * @param argvs
     */
    public static void main(String[] argvs)
    {
        // 45度向量
        float x45=1,y45=1;
        float x135=1,y135=-1;
        float x225=-1,y225 = -1;
        float x315=-1,y315=1;

        // 测试1
        double a = CaculateYxisAngle(x45,y45);
        System.out.println("45度角计算结果："+MathExt.FRount((float)Math.toDegrees((float)a),2));

        a = CaculateYxisAngle(x135,y135);
        System.out.println("135度角计算结果："+MathExt.FRount((float)Math.toDegrees((float)a),2));

        a = CaculateYxisAngle(x225,y225);
        System.out.println("225度角计算结果："+MathExt.FRount((float)Math.toDegrees((float)a),2));

        a = CaculateYxisAngle(x315,y315);
        System.out.println("315度角计算结果："+MathExt.FRount((float)Math.toDegrees((float)a),2));
    }
}
