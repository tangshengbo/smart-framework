package com.tangshengbo.helper;

import com.tangshengbo.annotation.Controller;
import com.tangshengbo.annotation.Service;
import com.tangshengbo.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class ClassHelper {

    private static final Set<Class<?>> CLASS_SET;

    static {
        String basePackage = ConfigHelper.getAppBasePackage();
        CLASS_SET = ClassUtil.getClassSet(basePackage);
    }

    /**
     * 获取应用包下所有的类
     *
     * @return
     */
    public static Set<Class<?>> getClassSet() {
        return CLASS_SET;
    }

    /**
     * 获取应用包下所有的Service类
     *
     * @return
     */
    public static Set<Class<?>> getServiceClassSet() {
        return CLASS_SET.stream()
                .filter(c -> c.isAnnotationPresent(Service.class))
                .collect(Collectors.toSet());
    }

    /**
     * 获取应用包下所有的Controller类
     *
     * @return
     */
    public static Set<Class<?>> getControllerClassSet() {
        return CLASS_SET.stream()
                .filter(c -> c.isAnnotationPresent(Controller.class))
                .collect(Collectors.toSet());
    }

    /**
     * 获取应用包下所有的Bean类
     *
     * @return
     */
    public static Set<Class<?>> getBeanClassSet() {
        Set<Class<?>> beanClassSet = new HashSet<>();
        beanClassSet.addAll(getServiceClassSet());
        beanClassSet.addAll(getControllerClassSet());
        return beanClassSet;
    }

    /**
     * 获取应用包名下某父类（或接口）的所有子类（或实现类）
     * @param superClass
     * @return
     */
    public static Set<Class<?>> getClassSetBySuper(Class<?> superClass) {
        return CLASS_SET.stream()
                .filter(cls -> superClass.isAssignableFrom(cls) && !superClass.equals(cls))
                .collect(Collectors.toSet());
    }

    /**
     * 获取应用包下带有某注解的所有类
     * @param annotationClass
     * @return
     */
    public static Set<Class<?>> getClassSetByAnnotation(Class<? extends Annotation> annotationClass) {
        return CLASS_SET.stream()
                .filter(cls -> cls.isAnnotationPresent(annotationClass))
                .collect(Collectors.toSet());
    }
}
