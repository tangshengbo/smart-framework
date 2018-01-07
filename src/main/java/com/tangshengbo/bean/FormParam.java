package com.tangshengbo.bean;

/**
 * Created by TangShengBo on 2018/1/7.
 */
public class FormParam {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段值
     */
    private Object fieldValue;

    public FormParam(String fieldName, Object fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
