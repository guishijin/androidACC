package com.ylsk.datafilter;

/**
 * 过虑器抽象接口
 */
public interface IFilter {
    /**
     * 执行过虑操作
     * @param newSample
     * @return
     */
    float doFilter(float newSample);
}
