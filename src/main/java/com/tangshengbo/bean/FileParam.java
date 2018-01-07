package com.tangshengbo.bean;

import java.io.InputStream;

/**
 * Created by TangShengBo on 2018/1/7.
 */
public class FileParam {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 表示上传的Content-Type 可判断文件类型
     */
    private String contentType;

    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 文件输入流
     */
    private InputStream inputStream;

    public FileParam(String fieldName, String fileName, String contentType,
                     long fileSize, InputStream inputStream) {
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.inputStream = inputStream;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
