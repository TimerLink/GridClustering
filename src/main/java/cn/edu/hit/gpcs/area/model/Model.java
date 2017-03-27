package cn.edu.hit.gpcs.area.model;

import cn.edu.hit.gpcs.DB;
import com.alibaba.fastjson.JSON;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * DBO抽象类
 */
public abstract class Model {
    /**
     * 处理查询语句
     * @param sql SQL语句
     */
    public static boolean execute (String sql) {
        Connection connection = DB.getInstance().getConnection();
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            return stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) { }
        }
        return false;
    }

    /**
     * 处理查询语句并调用接口处理结果集
     * @param sql SQL语句
     * @param listener 结果处理接口
     */
    public static void executeQuery (String sql, OnQueryResultListener listener) {
        Connection connection = DB.getInstance().getConnection();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            if (listener != null) listener.onResult(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) { }
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) { }
        }
    }


    /**
     * 处理查询语句并调用接口处理结果集
     */
    public static void executeQuery (String sql) {
        executeQuery(sql, null);
    }

    /**
     * @return 数据库列名与属性值之间的映射
     */
    public abstract Map<String, String> mapPropertyToColumn ();

    /**
     * 保存到数据库
     */
    public abstract boolean commit ();

    @Override
    public String toString() {
        return JSON.toJSONString(this, true);
    }

    /**
     * 查询结果接口
     */
    public interface OnQueryResultListener {
        void onResult(ResultSet rs) throws SQLException;
    }
}
