package com.tangshengbo.annotation;

import java.lang.annotation.*;

/**
 * Created by TangShengBo on 2018/1/1.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {

    /**
     * 注解
     */
    Class<? extends Annotation> value();
}
