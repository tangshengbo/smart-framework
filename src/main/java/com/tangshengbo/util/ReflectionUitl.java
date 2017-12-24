package com.tangshengbo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class ReflectionUitl {

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUitl.class);

    /**
     * 创建实例
     *
     * @param cls
     * @return
     */
    public static Object newInstance(Class<?> cls) {
        Object instance;
        try {
            instance = cls.newInstance();
        } catch (ReflectiveOperationException e) {
            logger.error("new instance failure {}", e);
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * 调用方法
     *
     * @param obj
     * @param method
     * @param args
     * @return
     */
    public static Object invokeMethod(Object obj, Method method, Object... args) {
        Object result;
        try {
            method.setAccessible(true);
            result = method.invoke(obj, args);
        } catch (ReflectiveOperationException e) {
            logger.error("invoke method failure {}", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 设置成员变量的值
     * @param obj
     * @param field
     * @param value
     */
    public static void setField(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        }catch (ReflectiveOperationException e) {
            logger.error("set field failure {}", e);
            throw new RuntimeException(e);
        }
    }
}
