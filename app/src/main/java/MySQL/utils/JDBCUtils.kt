package MySQL.utils

import java.sql.Connection
import java.sql.DriverManager

/**
 * function： 数据库工具类，连接数据库用
 */
object JDBCUtils {
    private const val TAG = "mysql-RayVita-JDBCUtils"

    private const val driver = "com.mysql.jdbc.Driver" // MySql驱动

    private const val dbName = "RayVita" // 数据库名称

    private const val user = "accessTest" // 用户名

    private const val password = "" // 密码

    val conn: Connection?
        get() {
            var connection: Connection? = null
            try {
                Class.forName(driver) // 动态加载类
                val ip = "10zy73167hc19.vicp.fun" // 写成本机地址，不能写成localhost，同时手机和电脑连接的网络必须是同一个

                // 尝试建立到给定数据库URL的连接
                connection = DriverManager.getConnection(
                    "jdbc:mysql://" + ip + ":17496/" + dbName,
                    user, password
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return connection
        }
}