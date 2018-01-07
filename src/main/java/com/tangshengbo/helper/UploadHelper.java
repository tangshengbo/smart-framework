package com.tangshengbo.helper;

import com.tangshengbo.bean.FileParam;
import com.tangshengbo.bean.FormParam;
import com.tangshengbo.bean.Param;
import com.tangshengbo.util.CollectionUtil;
import com.tangshengbo.util.FileUtil;
import com.tangshengbo.util.StreamUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by TangShengBo on 2018/1/7.
 */
public class UploadHelper {

    private static final Logger logger = LoggerFactory.getLogger(UploadHelper.class);

    /**
     * Servlet 文件上传对象
     */
    private static ServletFileUpload servletFileUpload;

    /**
     * 初始化
     *
     * @param servletContext
     */
    public static void init(ServletContext servletContext) {
        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        servletFileUpload = new ServletFileUpload(
                new DiskFileItemFactory(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, repository));
        int uploadLimit = ConfigHelper.getAppUploadLimit();
        if (uploadLimit != 0) {
            servletFileUpload.setFileSizeMax(uploadLimit * 1024 * 1024);
        }
    }

    /**
     * 判断请求是否为 multipart 类型
     *
     * @param request
     * @return
     */
    public static boolean isMultipart(HttpServletRequest request) {
        return ServletFileUpload.isMultipartContent(request);
    }

    /**
     * 创建请求对象
     *
     * @param request
     * @return
     */
    public static Param createParam(HttpServletRequest request) {
        List<FormParam> formParamList = new ArrayList<>();
        List<FileParam> fileParamList = new ArrayList<>();
        try {
            Map<String, List<FileItem>> fileItemListMap = servletFileUpload.parseParameterMap(request);
            if (CollectionUtil.isEmpty(fileItemListMap)) {
                return new Param(formParamList, fileParamList);
            }
            fileItemListMap.forEach((fieldName, fileItemList) -> fileItemList.forEach(fileItem -> {
                if (fileItem.isFormField()) {
                    addFormParam(formParamList, fieldName, fileItem);
                } else {
                    addFileParam(fileParamList, fieldName, fileItem);
                }
            }));
        } catch (FileUploadException e) {
            logger.error("create param failure {}", e);
        }
        return new Param(formParamList, fileParamList);
    }

    /**
     * 添加文件参数
     *
     * @param fileParamList
     * @param fieldName
     * @param fileItem
     */
    private static void addFileParam(List<FileParam> fileParamList, String fieldName, FileItem fileItem) {
        try {
            String fileName = FileUtil.getRealFileName(new String(fileItem.getName().getBytes(), "UTF-8"));
            if (StringUtils.isNotEmpty(fieldName)) {
                long fileSize = fileItem.getSize();
                String contentType = fileItem.getContentType();
                InputStream is = fileItem.getInputStream();
                fileParamList.add(new FileParam(fieldName, fileName, contentType, fileSize, is));
            }
        } catch (IOException e) {
            logger.error("添加文件参数 异常: {}", e);
        }
    }

    /**
     * 添加表单参数
     *
     * @param formParamList
     * @param fieldName
     * @param fileItem
     */
    private static void addFormParam(List<FormParam> formParamList, String fieldName, FileItem fileItem) {
        try {
            String fieldValue = fileItem.getString("UTF-8");
            formParamList.add(new FormParam(fieldName, fieldValue));
        } catch (UnsupportedEncodingException e) {
            logger.error("添加表单参数 异常: {}", e);
        }
    }

    /**
     * 文件上传
     * @param basePath
     * @param fileParam
     */
    public static void uploadFile(String basePath, FileParam fileParam) {
        if (Objects.isNull(fileParam)) {
            return;
        }
        try {
            String filePath = basePath + fileParam.getFileName();
            File file = FileUtil.createFile(filePath);
            InputStream is = new BufferedInputStream(fileParam.getInputStream());
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            StreamUtil.copyStream(is, os);
        } catch (FileNotFoundException e) {
            logger.error("文件上传 异常: {}", e);
        }
    }

    /**
     * 批量文件上传
     * @param basePath
     * @param fileParamList
     */
    public static void uploadFile(String basePath, List<FileParam> fileParamList) {
        if (CollectionUtil.isNotEmpty(fileParamList)) {
            fileParamList.forEach(fileParam -> uploadFile(basePath, fileParam));
        }
    }
}
