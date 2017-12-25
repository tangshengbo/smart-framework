package com.tangshengbo.bean;

import com.tangshengbo.util.CastUtil;

import java.util.Map;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public class Param {

    private Map<String, Object> paramMap;

    public Param(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    /**
     * 根据参数名获取 long 参数
     * @param name
     * @return
     */
    public long getLong(String name) {
        return CastUtil.castLong(paramMap.get(name));
    }

    /**
     * 根据参数名获取 String 参数
     * @param name
     * @return
     */
    public String getString(String name) {
        return CastUtil.castString(paramMap.get(name));
    }

    /**
     * 获取所有字段信息
     * @return
     */
    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    @Override
    public String toString() {
        return "Param{" +
                "paramMap=" + paramMap +
                '}';
    }
}
