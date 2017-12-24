package com.tangshengbo.helper;

import com.tangshengbo.util.ReflectionUitl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class BeanHelper {

    private static final Map<Class<?>, Object> BEAN_MAP = new HashMap<>();

    static {
        Set<Class<?>> beanClassSet = ClassHelper.getBeanClassSet();
        beanClassSet.forEach(beanClass -> {
            Object obj = ReflectionUitl.newInstance(beanClass);
            BEAN_MAP.put(beanClass, obj);
        });
    }

    /**
     * 获取bean容器
     *
     * @return
     */
    public static Map<Class<?>, Object> getBeanMap() {
        return BEAN_MAP;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> cls) {
        T obj = (T) BEAN_MAP.get(cls);
        if (Objects.isNull(obj)) {
            throw new RuntimeException("can not get bean by class:{}" + cls);
        }
        return obj;
    }
}