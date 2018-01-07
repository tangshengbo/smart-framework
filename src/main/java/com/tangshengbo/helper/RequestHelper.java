package com.tangshengbo.helper;

import com.tangshengbo.bean.FormParam;
import com.tangshengbo.bean.Param;
import com.tangshengbo.util.CodecUtil;
import com.tangshengbo.util.StreamUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by TangShengBo on 2018/1/7.
 */
public final class RequestHelper {

    private static final Logger logger = LoggerFactory.getLogger(RequestHelper.class);

    /**
     * 创建请求对象
     *
     * @param request
     * @return
     */
    public static Param createParam(HttpServletRequest request) {
        List<FormParam> formParamList = new ArrayList<>();
        formParamList.addAll(parseParameterNames(request));
        formParamList.addAll(parseInputStream(request));
        return new Param(formParamList);
    }

    /**
     * 解析参数
     *
     * @param request
     * @return
     */
    private static List<FormParam> parseParameterNames(HttpServletRequest request) {
        List<FormParam> formParamList = new ArrayList<>();
        Enumeration<String> paramsNames = request.getParameterNames();
        while (paramsNames.hasMoreElements()) {
            String fieldName = paramsNames.nextElement();
            String[] fieldValues = request.getParameterValues(fieldName);
            if (ArrayUtils.isNotEmpty(fieldValues)) {
                Object fieldValue;
                if (fieldValues.length == 1) {
                    fieldValue = fieldValues[0];
                } else {
                    StringBuilder sb = new StringBuilder("");
                    for (int i = 0; i < fieldValues.length; i++) {
                        sb.append(fieldValues[i]);
                        if (i != fieldValues.length - 1) {
                            sb.append("&");
                        }
                    }
                    fieldValue = sb.toString();
                }
                formParamList.add(new FormParam(fieldName, fieldValue));
            }
        }
        return formParamList;
    }

    /**
     * 解析请求body数据
     *
     * @param request
     * @return
     */
    private static List<FormParam> parseInputStream(HttpServletRequest request) {
        List<FormParam> formParamList = new ArrayList<>();
        try {
            String body = CodecUtil.decodeURL(StreamUtil.getString(request.getInputStream()));
            if (StringUtils.isEmpty(body)) {
                return formParamList;
            }
            String[] keys = StringUtils.split(body, "&");
            if (ArrayUtils.isNotEmpty(keys)) {
                return formParamList;
            }
            List<String> keyList = Arrays.asList(keys);
            keyList.forEach(key -> {
                String[] array = StringUtils.split(key, "=");
                if (ArrayUtils.isNotEmpty(array) && array.length == 2) {
                    String fieldName = array[0];
                    String fieldValue = array[1];
                    formParamList.add(new FormParam(fieldName, fieldValue));
                }
            });
        } catch (IOException e) {
            logger.error("解析请求body数据 异常:{}", e);
        }
        return formParamList;
    }
}
