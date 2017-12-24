package com.tangshengbo;

import com.tangshengbo.helper.BeanHelper;
import com.tangshengbo.helper.ClassHelper;
import com.tangshengbo.helper.ControllerHelper;
import com.tangshengbo.helper.IocHelper;
import com.tangshengbo.util.ClassUitl;

import java.util.Arrays;
import java.util.List;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class HelperLoader {

    public static void init() {
        Class<?>[] classes = {ClassHelper.class, BeanHelper.class, IocHelper.class, ControllerHelper.class};
        //加载helper类
        List<Class<?>> classList = Arrays.asList(classes);
        classList.forEach(helperClass -> ClassUitl.loadClass(helperClass.getName()));
    }
}
