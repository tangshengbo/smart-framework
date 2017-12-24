package com.tangshengbo.helper;

import com.tangshengbo.annotation.RequestMapping;
import com.tangshengbo.bean.Handler;
import com.tangshengbo.bean.Request;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class ControllerHelper {

    private static final Map<Request, Handler> REQUEST_MAPPING_MAP = new HashMap<>();

    static {
        Set<Class<?>> controllerClassSet = ClassHelper.getControllerClassSet();
        controllerClassSet.forEach(controllerClass -> {
            Method[] methods = controllerClass.getDeclaredMethods();
            if (ArrayUtils.isEmpty(methods)) {
                return;
            }
            List<Method> methodList = Arrays.asList(methods);
            methodList.stream()
                    .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                    .forEach(method -> addHandler(controllerClass, method));

        });
    }

    /**
     * 添加 Handler
     *
     * @param controllerClass
     * @param method
     */
    private static void addHandler(Class<?> controllerClass, Method method) {
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        String mapping = requestMapping.value();
        if (mapping.matches("\\w+:/\\w*")) {
            String[] mappings = StringUtils.split(mapping, ":");
            String requestMethod = mappings[0];
            String requestPath = mappings[1];
            Request request = new Request(requestMethod, requestPath);
            Handler handler = new Handler(controllerClass, method);
            REQUEST_MAPPING_MAP.put(request, handler);
        }
    }

    /**
     * 获取 Handler
     *
     * @param requestMethod
     * @param requestPath
     * @return
     */
    public static Handler getHandler(String requestMethod, String requestPath) {
        return REQUEST_MAPPING_MAP.get(new Request(requestMethod, requestPath));
    }
}
