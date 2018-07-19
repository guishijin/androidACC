package com.ylsk.datafilter;


/**
 * 一阶滤波算法的原理
 * 一阶滤波，又叫一阶惯性滤波，或一阶低通滤波。是使用软件编程实现普通硬件RC低通滤波器的功能。
 *
 *　　一阶低通滤波的算法公式为：
 *
 *　　　　　　　　　　　　　Y(n)=αX(n) + (1-α)Y(n-1)
 *    式中：
 *      α=滤波系数；
 *      X(n)=本次采样值；
 *      Y(n-1)=上次滤波输出值；
 *      Y(n)=本次滤波输出值。
 *
 *    一阶低通滤波法采用本次采样值与上次滤波输出值进行加权，得到有效滤波值，使得输出对输入有反馈作用。
 *
 *    滤波系数越小，滤波结果越平稳，但是灵敏度越低；滤波系数越大，灵敏度越高，但是滤波结果越不稳定。
 *
 *　　一阶滤波无法完美地兼顾灵敏度和平稳度。有时，我们只能寻找一个平衡，在可接受的灵敏度范围内取得尽可能好的平稳度。
 *    而在一些场合，我们希望拥有这样一种接近理想状态的滤波算法。
 *       即：
 *          当数据快速变化时，滤波结果能及时跟进（灵敏度优先）；
 *          当数据趋于稳定，在一个固定的点上下振荡时，滤波结果能趋于平稳（平稳度优先）。
 *       参考CompassFilter实现
 */
public class RCFilter implements IFilter {

    // 上一次滤波值
    private Float x0 = null;
    // 滤波系数 范围（0.0 - 1.0）
    private Float a = 0.01f;
    // 采样频率 Hz
    private Float f = null;

    /**
     * 构造函数
     * @param a 滤波系数
     */
    public RCFilter(float a) {
        this.a = a;
    }

    /**
     * 滤波操作
     * @param val 本次采样值
     * @return
     */
    @Override
    public float doFilter(float val){
        float result;
        if(this.x0 == null)
        {
            this.x0 = val;
        }
        result = a * val + ( 1.0f -a ) * this.x0;
        this.x0 = result;
        return result;
    }
}
