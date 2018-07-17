package com.ylsk.inertialnavigation.tools;

import java.math.BigDecimal;

public class MathExt {

    public static float FRount(float inf)
    {
        return FRount(inf,6,4);
    }

    public static float FRount(float inf, int bit, int roundMode) {
         //float ft = 134.3435f;
        float ft = inf;

        //设置位数
        int scale = bit;
        //表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
        int roundingMode = roundMode;
        BigDecimal bd = new BigDecimal((double) ft);
        bd   =bd.setScale(scale,roundingMode);
        ft   =bd.floatValue();
        return ft;
    }
}
