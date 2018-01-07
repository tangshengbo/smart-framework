package com.tangshengbo;

import com.tangshengbo.bean.Data;
import com.tangshengbo.bean.Handler;
import com.tangshengbo.bean.Param;
import com.tangshengbo.bean.View;
import com.tangshengbo.helper.*;
import com.tangshengbo.util.*;
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
@WebServlet(urlPatterns = "/", loadOnStartup = 1)
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
        //初始化文件上传组件
        UploadHelper.init(servletContext);
        logger.info("初始化完成>>>>>>>>>>>>");
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取请求方法和请求路径
        String requestMethod = req.getMethod().toLowerCase();
        String requestPath = req.getServletPath();
        //获取requestMapping 处理器
        Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);
        if (Objects.isNull(handler)) {
//            req.getRequestDispatcher(ConfigHelper.getAppJspPath() + "error.html").forward(req, resp);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //获取Controller 类和Bean实例
        Class<?> controllerClass = handler.getControllerClass();
        Object controllerBean = BeanHelper.getBean(controllerClass);
        Param param;
        //获取参数请求对象
        if (UploadHelper.isMultipart(req)) {
            param = UploadHelper.createParam(req);
        } else {
            param = RequestHelper.createParam(req);
        }
        //调用方法(controller 方法)
        Object result = invokeMethod(controllerBean, param, handler);
        if (isView(result)) {
            handleView(req, resp, (View) result);
        }
        if (isData(result)) {
            handleData(resp, (Data) result);
        }
    }

    /**
     * 调用对应的controller 方法
     *
     * @param controllerBean
     * @param param
     * @param handler
     * @return
     */
    private Object invokeMethod(Object controllerBean, Param param, Handler handler) {
        Method method = handler.getRequestMethod();
        Object result;
        if (param.isEmpty()) {
            result = ReflectionUtil.invokeMethod(controllerBean, method);
        } else {
            result = ReflectionUtil.invokeMethod(controllerBean, method, param);
        }
        return result;
    }

    private void handleData(HttpServletResponse resp, Data result) throws IOException {
        //返回Data 数据
        Object model = result.getModel();
        if (Objects.isNull(model)) {
            return;
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        String json = JsonUtil.toJson(result);
        writer.write(json);
        writer.flush();
        writer.close();
    }

    private void handleView(HttpServletRequest req, HttpServletResponse resp, View result) throws IOException, ServletException {
        //返回Jsp 页面
        String path = result.getPath();
        if (Objects.isNull(path)) {
            return;
        }
        if (path.startsWith("/")) {
            resp.sendRedirect(req.getContextPath() + path);
            return;
        }
        Map<String, Object> model = result.getModel();
        model.forEach(req::setAttribute);
        req.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(req, resp);
    }

    private boolean isData(Object result) {
        return result instanceof Data;
    }

    private boolean isView(Object result) {
        return result instanceof View;
    }

}
