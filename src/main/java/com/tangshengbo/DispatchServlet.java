package com.tangshengbo;

import com.tangshengbo.bean.Data;
import com.tangshengbo.bean.Handler;
import com.tangshengbo.bean.Param;
import com.tangshengbo.bean.View;
import com.tangshengbo.helper.BeanHelper;
import com.tangshengbo.helper.ConfigHelper;
import com.tangshengbo.helper.ControllerHelper;
import com.tangshengbo.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by TangShengBo on 2017/12/24.
 */
@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatchServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DispatchServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("初始化开始>>>>>>>>>>>>");
        //初始化相关 Helper 类
        HelperLoader.init();
        //获取 ServletContext 对象 注册Servlet
        ServletContext servletContext = config.getServletContext();
        //注册处理Jsp的Servlet
        ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
        jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");
        //注册处理静态资源 默认Servlet
        ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
        defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");
        logger.info("初始化完成>>>>>>>>>>>>");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取请求方法和请求路径
        String requestMethod = req.getMethod().toLowerCase();
        String requestPath = req.getPathInfo();
        //获取requestMapping 处理器
        Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);
        if (Objects.isNull(handler)) {
            return;
        }
        //获取Controller 类和Bean实例
        Class<?> controllerClass = handler.getControllerClass();
        Object controllerBean = BeanHelper.getBean(controllerClass);
        //穿件参数请求对象
        Param param = getParam(req);
        Method method = handler.getRequestMethod();
        Object result = ReflectionUitl.invokeMethod(controllerBean, method, param);
        if (isView(result)) {
            handlerView(req, resp, (View) result);
        }
        if (isData(result)) {
            handlerData(resp, (Data) result);
        }
    }

    private void handlerData(HttpServletResponse resp, Data result) throws IOException {
        //返回Data 数据
        Data data = result;
        Object model = data.getModel();
        if (Objects.isNull(model)) {
            return;
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        String json = JsonUtil.toJson(data);
        writer.write(json);
        writer.flush();
        writer.close();
    }

    private boolean handlerView(HttpServletRequest req, HttpServletResponse resp, View result) throws IOException, ServletException {
        //返回Jsp 页面
        View view = result;
        String path = view.getPath();
        if (Objects.isNull(path)) {
            return true;
        }
        if (path.startsWith("/")) {
            resp.sendRedirect(req.getContextPath() + path);
        }
        Map<String, Object> model = view.getModel();
        model.forEach(req::setAttribute);
        req.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(req, resp);
        return false;
    }

    private boolean isData(Object result) {
        return result instanceof Data;
    }

    private boolean isView(Object result) {
        return result instanceof View;
    }


    /**
     * 获取请求参数
     *
     * @param req
     * @return
     * @throws IOException
     */
    private Param getParam(HttpServletRequest req) throws IOException {
        Map<String, Object> paramMap = new HashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = req.getParameter(paramName);
            paramMap.put(paramName, paramValue);
        }
        String body = CodecUtil.decodeURL(StreamUtil.getString(req.getInputStream()));
        if (Objects.isNull(body)) {
            return null;
        }
        String[] params = StringUtils.split(body, "&");
        List<String> paramList = Arrays.asList(params);
        paramList.forEach(param -> {
            String[] array = StringUtils.split(param, "=");
            if (Objects.nonNull(array) && array.length == 2) {
                String paramName = array[0];
                String paramValue = array[1];
                paramMap.put(paramName, paramValue);
            }
        });
        return new Param(paramMap);
    }
}