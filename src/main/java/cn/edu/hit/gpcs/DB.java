package cn.edu.hit.gpcs;

import cn.edu.hit.gpcs.utils.DotEnv;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
    private static DB instance;
    private static Connection connection;

    private DB () {}

    /**
     * @return 数据库操作者单例
     */
    public static DB getInstance () {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    /**
     * 连接到数据库
     */
    private static void connect () {
        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String dbURL = String.format("jdbc:sqlserver://%s:1433;DatabaseName=%s",
                DotEnv.get("SERVER_NAME"),
                DotEnv.get("DB_NAME"));
        String userName = DotEnv.get("DB_USER_NAME");
        String userPwd = DotEnv.get("DB_USER_PWD");
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(dbURL, userName, userPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 数据库连接
     */
    public Connection getConnection () {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    /**
     * 析构函数中关闭数据库连接
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (connection != null) try { connection.close(); } catch (Exception ignored) { }
    }

}
