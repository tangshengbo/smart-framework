package com.tangshengbo.helper;

import com.tangshengbo.annotation.Aspect;
import com.tangshengbo.annotation.Service;
import com.tangshengbo.proxy.AbstractAspect;
import com.tangshengbo.proxy.Proxy;
import com.tangshengbo.proxy.ProxyManager;
import com.tangshengbo.proxy.TransactionAspect;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Created by TangShengBo on 2018/1/1.
 */
public final class AopHelper {

    static {
        Map<Class<?>, Set<Class<?>>> proxyMap = createProxyMap();
        Map<Class<?>, List<Proxy>> targetMap = createTargetMap(proxyMap);
        targetMap.forEach((targetClass, proxyList) -> {
            Object proxy = ProxyManager.createProxy(targetClass, proxyList);
            BeanHelper.setBean(targetClass, proxy);
        });
    }

    /**
     * 创建目标类集合
     *
     * @param aspect
     * @return
     */
    public static Set<Class<?>> createTargetClassSet(Aspect aspect) {
        Set<Class<?>> targetClassSet = new HashSet<>();
        Class<? extends Annotation> annotation = aspect.value();
        if (!annotation.equals(Aspect.class)) {
            targetClassSet.addAll(ClassHelper.getClassSetByAnnotation(annotation));
        }
        return targetClassSet;
    }

    /**
     * 创建代理类和目标类映射
     *
     * @return
     */
    public static Map<Class<?>, Set<Class<?>>> createProxyMap() {
        Map<Class<?>, Set<Class<?>>> proxyMap = new HashMap<>();
        addAspectProxy(proxyMap);
        addTransactionProxy(proxyMap);
        return proxyMap;
    }

    /**
     * 添加普通切面
     * @param proxyMap
     */
    private static void addAspectProxy(Map<Class<?>, Set<Class<?>>> proxyMap) {
        Set<Class<?>> proxyClassSet = ClassHelper.getClassSetBySuper(AbstractAspect.class);
        proxyClassSet.stream()
                .filter(proxyClass -> proxyClass.isAnnotationPresent(Aspect.class))
                .forEach(proxyClass -> {
                    Aspect aspect = proxyClass.getAnnotation(Aspect.class);
                    Set<Class<?>> targetClassSet = createTargetClassSet(aspect);
                    proxyMap.put(proxyClass, targetClassSet);
                });
    }

    /**
     * 添加事务切面
     * @param proxyMap
     */
    private static void addTransactionProxy(Map<Class<?>, Set<Class<?>>> proxyMap) {
        Set<Class<?>> serviceClassSet = ClassHelper.getClassSetByAnnotation(Service.class);
        proxyMap.put(TransactionAspect.class, serviceClassSet);
    }

    /**
     * 创建目标对象集合
     *
     * @param proxyMap
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>, Set<Class<?>>> proxyMap) {
        Map<Class<?>, List<Proxy>> targetMap = new HashMap<>();
        proxyMap.forEach((proxyClass, targetClassSet) -> targetClassSet
                .forEach(targetClass -> {
                    try {
                        Proxy proxy = (Proxy) proxyClass.newInstance();
                        if (targetMap.containsKey(targetClass)) {
                            targetMap.get(targetClass).add(proxy);
                        } else {
                            List proxyList = new ArrayList();
                            proxyList.add(proxy);
                            targetMap.put(targetClass, proxyList);
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }));
        return targetMap;
    }
}
