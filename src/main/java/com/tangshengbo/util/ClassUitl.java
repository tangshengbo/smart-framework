package com.tangshengbo.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by TangShengBo on 2017/12/23.
 */
public class ClassUitl {

    private static final Logger logger = LoggerFactory.getLogger(ClassUitl.class);

    /**
     * 获取类加载器
     *
     * @return
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载类
     *
     * @param className
     * @param isInitialized
     * @return
     */
    public static Class<?> loadClass(String className, boolean isInitialized) {
        Class<?> cls;
        try {
            cls = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.error("load class failure {}", e);
            throw new RuntimeException(e);
        }
        return cls;
    }

    /**
     * 加载类 懒加载
     *
     * @param className
     * @return
     */
    public static Class<?> loadClass(String className) {
        return loadClass(className, false);
    }

    /**
     * 获取指定包名下的所有类
     *
     * @param packageName
     * @return
     */
    public static Set<Class<?>> getClassSet(String packageName) {
        Set<Class<?>> classSet = new HashSet<>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(packageName.replace(".", "/"));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    parseFile(packageName, classSet, url);
                } else if ("jar".equals(protocol)) {
                    parseJar(classSet, url);
                }
            }
        } catch (Exception e) {
            logger.error("get class set failure {}", e);
        }
        return classSet;
    }

    private static boolean filter(File file) {
        return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
    }

    private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {
        File[] files = new File(packagePath).listFiles(ClassUitl::filter);
        for(File file : files) {
            String fileName = file.getName();
            if (file.isFile()) {
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                if (StringUtils.isNotEmpty(packageName)) {
                    className = packageName + "." + className;
                }
                doAddClass(classSet, className);
                continue;
            }
            String subPackagePath = fileName;
            if (StringUtils.isNotEmpty(packagePath)) {
                subPackagePath = packagePath + "/" + subPackagePath;
            }
            String subPackageName = fileName;
            if (StringUtils.isNotEmpty(packageName)) {
                subPackageName = packagePath + "." + subPackageName;
            }
            addClass(classSet, subPackagePath, subPackageName);
        }
    }

    private static void doAddClass(Set<Class<?>> classSet, String className) {
        Class<?> cls = loadClass(className);
        classSet.add(cls);
    }


    private static void parseFile(String packageName, Set<Class<?>> classSet, URL url) {
        String packagePath = url.getPath().replaceAll("%20", " ");
        addClass(classSet, packagePath, packageName);
    }

    private static void parseJar(Set<Class<?>> classSet, URL url) throws IOException {
        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
        if (Objects.isNull(jarURLConnection)) {
            return;
        }
        JarFile jarFile = jarURLConnection.getJarFile();
        if (Objects.isNull(jarFile)) {
            return;
        }
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarEntryName = jarEntry.getName();
            if (jarEntryName.endsWith(".class")) {
                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf("."))
                        .replaceAll("/", ".");
                doAddClass(classSet, className);
            }
        }
    }
}
