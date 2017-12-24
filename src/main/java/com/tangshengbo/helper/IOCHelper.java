package com.tangshengbo.helper;

import com.tangshengbo.annotation.Inject;
import com.tangshengbo.util.ReflectionUitl;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class IocHelper {

    static {
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
        beanMap.forEach((beanClass, beanInstance) -> {
            Field[] fields = beanClass.getDeclaredFields();
            if (ArrayUtils.isEmpty(fields)) {
                return;
            }
            List<Field> fieldList = Arrays.asList(fields);
            fieldList.stream()
                    .filter(field -> field.isAnnotationPresent(Inject.class))
                    .forEach(field -> {
                        //获取被注入的类实例
                        Class<?> beanFieldClass = field.getType();
                        Object beanFieldInstance = beanMap.get(beanFieldClass);
                        //依赖注入
                        ReflectionUitl.setField(beanInstance, field, beanFieldInstance);
                    });
        });
    }
}
