package com.tangshengbo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class JsonUtil {

    private final static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private final static ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将Pojo 转 json
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String toJson(T obj) {
        String json;
        try {
            json = MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("convert POJO to JSON failure {}", e);
            throw new RuntimeException(e);
        }
        return json;
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        T obj;
        try {
            obj = MAPPER.readValue(json, cls);
        } catch (IOException e) {
            logger.error("convert JSON to POJO failure", e);
            throw new RuntimeException(e);
        }
        return obj;
    }

}
