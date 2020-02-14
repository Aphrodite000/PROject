package util;

import com.sun.org.apache.bcel.internal.generic.DUP;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import task.DBInit;

import javax.sql.DataSource;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {
    //工具类
    //提供数据库工具，实现一些封装

    private static volatile DataSource DATA_SOURCE;

    /**
     * 提供获取数据库连接池的功能
     * 使用单例模式（多线程安全版本）
     * 回顾多线程安全版本的单例模式
     * 1.为什么在外层判断是否等于null
     * 2.synchronized加锁以后，为什么还要判断等于null
     * 3.为什么DataSource类变量要使用volatile关键字修饰
     * 多线程操作：原子性，可见性（主内存拷贝到工作内存），有序性
     *
     *
     * @return
     */
    private  static DataSource getDataSource(){
        if(DATA_SOURCE==null){//提高效率，加锁安全但效率不高，所以锁的范围尽可能小
            synchronized (DBUtil.class){
                if(DATA_SOURCE==null){
                    //初始化操作，使用volatile关键字禁止指令重排序
                    SQLiteConfig config=new SQLiteConfig();
                    //使用这个类设置日期格式
                    config.setDateStringFormat(Util.DATE_PATERN);
                    //初始化一个sqlite数据库对象   初始化出来日期格式的一个配置
                    DATA_SOURCE=new SQLiteDataSource(config);
                    ((SQLiteDataSource) DATA_SOURCE).setUrl(getUrl());
                }
            }
        }
        return DATA_SOURCE;
    }
    //得到sqlite数据库文件url的方法，把文件放到指定地方
    private static String getUrl(){
        try {
            //数据库文件约定好，放在target/everything-like
            //String url="jdbc:sqlite://"+
            //加上数据库本地文件路径，要定位到target（编译文件夹）目录
            //获取target编译文件夹路径
            //这是反射，获取类加载器，按获取资源
            //通过classLoder.getResource()/getResourceAsStream这样的方法默认跟路径为编译文件夹路径（target/classes）
            /**
             * URL target=DBInit.class.getClassLoader().getResource("../");//获取到它的上层路径  获取target
             *         //默认的根路径就是classes所以回不到上一级了
             *         System.out.println(target.getPath());
             */
            //获取当前路径
            URL classesURL= DBUtil.class.getClassLoader().getResource("./");
            //当前路径就会转化为字符串
            //获取target/classes的父目录路径
            String dir=new File(classesURL.getPath()).getParent();
            String url="jdbc:sqlite://"+dir+File.separator+"everything-like.db";
            //文件分隔符
            //new SqliteDataSource() 把这个对象的URL设置
            //文件已存在就会读取这个文件
            url= URLDecoder.decode(url,"UTF-8");//转义，如果因为为名字有空格会异常
            System.out.println("获取数据库文件路径"+url);
            return url;//会在target下面出现everything-like 的文件
            //这就是数据库的URL，数据库的路径，作为传入到sqlite的
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("获取数据库文件失败",e);
        }
    }
    /**
     * 提供获取数据库连接的方法
     * 从数据库连接池DataSource.getConnection()获取连接
     * @return
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
        //getDataSource()这个方法来获取连接，getConnection 是来返回
    }

    public static void main(String[] args) throws SQLException {
        System.out.println(getConnection());
    }

    public static void close(Connection connection, Statement statement) {
        close(connection,statement,null);
    }

    /**
     * 数据库释放资源
     * @param connection  连接
     * @param statement    sql操作对象
     * @param resultSet   结果集
     */
    public static void close(Connection connection, Statement statement,  ResultSet resultSet) {
        try {
            if(connection!=null){
                connection.close();
            }
            if(statement!=null){
                statement.close();
            }
            if(resultSet!=null){
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("释放数据库资源错误");
        }
    }
}
