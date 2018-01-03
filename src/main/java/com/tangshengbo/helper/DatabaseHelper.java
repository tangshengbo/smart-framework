package com.tangshengbo.helper;

import com.tangshengbo.util.CollectionUtil;
import com.tangshengbo.util.PropsUtil;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by TangShengBo on 2017/12/22.
 */
public class DatabaseHelper {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final ThreadLocal<Connection> CONNECTION_HOLDER = new ThreadLocal<>();

    private static final BasicDataSource DATA_SOURCE;

    private static final QueryRunner QUERY_RUNNER = new QueryRunner();

    private static final String DERIVE;
    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties conf = PropsUtil.loadProps("db-config.properties");
        DERIVE = conf.getProperty("jdbc.driver");
        URL = conf.getProperty("jdbc.url");
        USERNAME = conf.getProperty("jdbc.username");
        PASSWORD = conf.getProperty("jdbc.password");
//        try {
//            Class.forName(DERIVE);
//        } catch (ClassNotFoundException e) {
//            logger.error("加载驱动异常:{}", e);
//        }

        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setDriverClassName(DERIVE);
        DATA_SOURCE.setUrl(URL);
        DATA_SOURCE.setUsername(USERNAME);
        DATA_SOURCE.setPassword(PASSWORD);
        DATA_SOURCE.setDefaultAutoCommit(false);
    }


    /**
     * 开启事务
     */
    public static void beginTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                logger.error("begin transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        }
    }

    /**
     * 提交事务
     */
    public static void commitTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                logger.error("commit transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    /**
     * 回滚事务
     */
    public static void rollbackTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                logger.error("rollback transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    private static Connection getConnection() {
        Connection conn = CONNECTION_HOLDER.get();
        if (Objects.isNull(conn)) {
            try {
                conn = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                logger.error("获取连接异常:{}", e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        }
        return conn;
    }

    /**
     * 执行 SQL 文件
     */
    public static void executeSqlFile(String filePath) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String sql;
            while ((sql = reader.readLine()) != null) {
                DatabaseHelper.executeUpdate(sql);
            }
        } catch (Exception e) {
            logger.error("execute sql file failure {}", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * 查询实体列表
     */
    public static <T> List<T> queryEntityList(Class<T> entityClass, String sql, Object... params) {
        List<T> entityList;
        try {
            Connection conn = getConnection();
            entityList = QUERY_RUNNER.query(conn, sql, new BeanListHandler<>(entityClass), params);
        } catch (SQLException e) {
            logger.error("query entity list failure", e);
            throw new RuntimeException(e);
        }
        return entityList;
    }

    /**
     * 查询实体
     */
    public static <T> T queryEntity(Class<T> entityClass, String sql, Object... params) {
        T entity;
        try {
            Connection conn = getConnection();
            entity = QUERY_RUNNER.query(conn, sql, new BeanHandler<>(entityClass), params);
        } catch (SQLException e) {
            logger.error("query entity failure", e);
            throw new RuntimeException(e);
        }
        return entity;
    }

    /**
     * 执行查询语句
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> result;
        try {
            Connection conn = getConnection();
            result = QUERY_RUNNER.query(conn, sql, new MapListHandler(), params);
        } catch (SQLException e) {
            logger.error("execute query failure", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 执行更新语句(update, insert, delete)
     *
     * @return rows 受影响的行数
     */
    public static int executeUpdate(String sql, Object... params) {
        int rows;
        try {
            Connection conn = getConnection();
            rows = QUERY_RUNNER.update(conn, sql, params);
        } catch (SQLException e) {
            logger.error("execute update failure", e);
            throw new RuntimeException(e);
        }
        return rows;
    }

    /**
     * 插入实体
     */
    public static <T> boolean insertEntity(Class<T> entityClass, Map<String, Object> fieldMap) {
        if (CollectionUtil.isEmpty(fieldMap)) {
            logger.error("can not insert entity: fieldMap is empty");
            return false;
        }
        String sql = getSql(entityClass, fieldMap);
        logger.info(sql);
        Object[] params = fieldMap.values().toArray();
        return executeUpdate(sql, params) == 1;
    }

    private static <T> String getSql(Class<T> entityClass, Map<String, Object> fieldMap) {
        String sql = "INSERT INTO " + getTableName(entityClass);
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String fieldName : fieldMap.keySet()) {
            columns.append(fieldName).append(", ");
            values.append("?, ");
        }
        columns.replace(columns.lastIndexOf(", "), columns.length(), ")");
        values.replace(values.lastIndexOf(", "), values.length(), ")");
        sql += columns + " VALUES " + values;
        return sql;
    }

    public static <T> boolean insertBatchEntity(Class<T> entityClass, Map<String, Object> fieldMap, Object[][] params) {
        try {
            Connection conn = getConnection();
            ArrayListHandler handler = new ArrayListHandler();
            String sql = getSql(entityClass, fieldMap);
            QUERY_RUNNER.insertBatch(conn, sql, handler, params);
        } catch (SQLException e) {
            logger.error("execute update failure", e);
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * 更新实体
     */
    public static <T> boolean updateEntity(Class<T> entityClass, long id, Map<String, Object> fieldMap) {
        if (CollectionUtil.isEmpty(fieldMap)) {
            logger.error("can not update entity: fieldMap is empty");
            return false;
        }
        String sql = "UPDATE " + getTableName((entityClass)) + " SET ";
        StringBuilder columns = new StringBuilder();
        for (String fieldName : fieldMap.keySet()) {
            columns.append(fieldName).append("=?, ");
        }
        sql += columns.substring(0, columns.lastIndexOf(", ")) + " WHERE id=?";

        logger.info(sql);
        List<Object> paramList = new ArrayList<>();
        paramList.addAll(fieldMap.values());
        paramList.add(id);
        Object[] params = paramList.toArray();

        return executeUpdate(sql, params) == 1;
    }

    public static <T> boolean deleteEntity(Class<T> entityClass, long id) {
        String sql = "DELETE FROM " + getTableName(entityClass) + " WHERE id=?";
        logger.info(sql);
        return executeUpdate(sql, id) == 1;
    }

    private static String getTableName(Class<?> entityClass) {
        return entityClass.getSimpleName();
    }

    private static void closeConnection() {
        Connection conn = CONNECTION_HOLDER.get();
        if (Objects.nonNull(conn)) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("连接关闭异常:{}", e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }
}
