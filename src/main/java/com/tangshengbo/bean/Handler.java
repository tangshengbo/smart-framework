package com.tangshengbo.bean;

import java.lang.reflect.Method;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public class Handler {

    /**
     * Controller类
     */
    private Class<?> controllerClass;

    /**
     * 请求的方法
     */
    private Method requestMethod;

    public Handler(Class<?> controllerClass, Method requestMethod) {
        this.controllerClass = controllerClass;
        this.requestMethod = requestMethod;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(Method requestMethod) {
        this.requestMethod = requestMethod;
    }
}
