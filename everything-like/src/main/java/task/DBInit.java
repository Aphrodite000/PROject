package task;

import com.sun.scenario.effect.impl.state.AccessHelper;
import org.sqlite.core.DB;
import util.DBUtil;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * 1.初始化数据库:数据库文件约定好，放在target/everything-like
 * 调用DBUtil.getConnection()就可以完成数据库初始化
 * 2.并且读取sql文件，（init.sql）
 * 3.再执行sql语句初始化表
 */
public class DBInit {
    //在Controller里面监听方法以被调用

    //读取操作
    //以分号结束
    public static String[] readSQL(){
        try {
            //通过ClassLoader获取流，或者用FileInputStream    classes下的init.sql  通过类加载器，获取资源得到字节流
            InputStream is=DBInit.class.getClassLoader().getResourceAsStream("init.sql");
            //这是字节流，要转换为字符流  需要字节和字符转换流操作
            BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuilder sb=new StringBuilder();
            String line;
            //sql按行读取
            while ((line=br.readLine())!=null){
                if(line.contains("--")){
                    //如果一行有--就读取到两个--之前
                    line=line.substring(0,line.indexOf("--"));
                }
                //每行读取完添加，相当于去掉了很多注解，然后合并代码
                sb.append(line);
            }
            //按照分号分割
            String[] sqls=sb.toString().split(";");
            return sqls;
        } catch (Exception e) {
            //改大，把readLine()的异常就也能捕获到了
            e.printStackTrace();
            throw  new RuntimeException("读取sql文件错误",e);
        }
    }
    public static void init(){
        //数据库jdbc操作，sql语句的执行
        /**
         * 加载驱动
         * 建立连接
         * 创建操作对象
         * 执行sql语句
         * 获取结果集，处理结果集
         * 释放资源
         */
        Connection connection=null;
        Statement statement=null;//因为完成初始化，是比较简单的sql语句，所以用这个就行了
        try{//获取连接
            connection= DBUtil.getConnection();
            //常见操作对象
            statement=connection.createStatement();
            String[] sqls=readSQL();
            //多条sql语句循环执行
            for(String sql:sqls){
                //只是执行和获取结果集的方法不一样
                //System.out.println(sql);
                statement.executeUpdate(sql);
            }
            //只是初始化，所以没有处理结果集那一步
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("初始化数据库表失败");
        }finally {
            //释放资源
            DBUtil.close(connection,statement);
        }
    }

    public static void main(String[] args) {
        init();
    }
}
