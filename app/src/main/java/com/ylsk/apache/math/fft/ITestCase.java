package com.ylsk.apache.math.fft;

import java.util.List;

/**
 *
 * @author gsj
 */
public interface ITestCase
{
    /**
     * 测试用例核心run方法
     * @param params
     * @return
     * @throws Exception 
     */
    public Object run(List<Object> params) throws Exception;

    /**
     * 获取run的传入参数
     * @return 
     */
    public List<Object> getParams();
}
