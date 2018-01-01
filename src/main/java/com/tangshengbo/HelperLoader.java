package com.tangshengbo;

import com.tangshengbo.helper.*;
import com.tangshengbo.util.ClassUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class HelperLoader {

    public static void init() {
        Class<?>[] classes = {ClassHelper.class, BeanHelper.class, AopHelper.class, IocHelper.class, ControllerHelper.class};
        //加载helper类
        List<Class<?>> classList = Arrays.asList(classes);
        classList.forEach(helperClass -> ClassUtil.loadClass(helperClass.getName(), true));
    }
}
