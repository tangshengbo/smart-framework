package com.tangshengbo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by TangShengBo on 2017/12/24.
 */
public final class StreamUtil {

    private final static Logger logger = LoggerFactory.getLogger(StreamUtil.class);

    /**
     * 从输入流获取字符串
     */
    public static String getString(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            logger.error("get string failure", e);
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    /**
     * 将输入流复制到输出流
     *
     * @param inputStream
     * @param outputStream
     */
    public static void copyStream(InputStream inputStream, OutputStream outputStream) {
        try (final InputStream is = inputStream;
             final OutputStream os = outputStream) {
            int length;
            byte[] buffer = new byte[4 * 1024];
            while ((length = is.read(buffer, 0, buffer.length)) != -1) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception e) {
            logger.error("copy stream failure" + e);
            throw new RuntimeException(e);
        }
    }
}
