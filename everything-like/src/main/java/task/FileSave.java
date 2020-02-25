package task;

import app.FileMeta;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.ws.wsdl.parser.RuntimeWSDLParser;
import util.DBUtil;
import util.PinyinUtil;
import util.Util;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSave implements ScanCallback{
    //文件扫描后要保存在数据库中，实现接口
    @Override
    public void callback(File dir) {
        //根据前面传进来的肯定是文件夹
        //文件夹下一级子文件和子文件夹保存早数据库
        //获取本地目录下一级子文件和子文件夹
        //集合框架中使用自定义类型，判断是否某个对象在集合 中存在
        //两个list如何比对
        File[] children=dir.listFiles();
        //相当于把doScan的打印放到了这里了
        List<FileMeta> locals=new ArrayList<>();
        if(children!=null){
            for(File child:children){
                locals.add(new FileMeta(child));
            }
        }//本地的已经读取出来了

        //文件保存改成文件比对
        //获取数据库保存的dir目录下的子文件和子文件夹(jdbc select)
        //集合框架的使用如何判断一个集合里面有，一个集合里面没有
        List<FileMeta> metas=query(dir);//只获取dir下一级
        //TODO List<File>  先删除后插入
        //数据库有，本地没有，做删除，这个要放前面来做，不然先插入后删除，会把之前插入的删了
        for(FileMeta meta:metas){
            if(!locals.contains(meta)){
                //meta删除0
                // 1.删除meta本身，
                // 2.如果是目录，把所有子文件子文件夹删掉
                delete(meta);
                System.out.println("删除文件"+meta);
            }
        }
        //本地有数据库没有，做插入，把数据插入数据库
        for(FileMeta meta:locals){
            if(!metas.contains(meta)){
                save(meta);
                System.out.println("插入文件"+meta);  }
        }

    }
    //meta删除
    //删除meta本身的信息
    //2.如果是目录，还要讲meta所有的子文件，子文件夹删除
    private void delete(FileMeta meta) {
        Connection connection=null;
        PreparedStatement ps=null;
        try{
            connection=DBUtil.getConnection();
            String sql="delete from file_meta where"+
                    " (name=? and path=? and is_directory=?)";//删除文件本身，这是and
            if(meta.getDirectory()){//是文件夹，还要删除子文件和子文件夹
                sql+=" or path=?" +//儿子辈
                        "or path like ?";//孙子辈,和孙子辈之前  张%=路径/%
                //path在数据库表达的是父路径
            }//儿子和孙子不能合在一起模糊查询的的原因因为，它可能又多个儿子
            ps=connection.prepareStatement(sql);
            ps.setString(1,meta.getName());
            ps.setString(2,meta.getPath());//父路径
            ps.setBoolean(3,meta.getDirectory());
            if(meta.getDirectory()){
                ps.setString(4,
                        meta.getPath()+File.separator+meta.getName());
                ps.setString(5,meta.getPath()+File.separator+meta.getName()+File.separator+"%");
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("删除文件信息,检查delete语句");
        }finally{
            DBUtil.close(connection,ps);
        }
    }

    //根据已有数据库目录，查询数据库相关文件信息
    private  List<FileMeta> query(File dir){
        Connection connection=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        List<FileMeta> metas=new ArrayList<>();
        try{
            //1.创建数据库连接
            connection=DBUtil.getConnection();
            String sql="select name,path,is_directory,size,last_modified"+
                    " from file_meta where path=?";//要父路径
            //2.创建jdbc操作命令对象
            ps=connection.prepareStatement(sql);
            ps.setString(1,dir.getPath());//dir的路径是要获取的 父路径
            //3.执行sql
            rs=ps.executeQuery();
            //4.处理结果值
            while(rs.next()){
                String name=rs.getString("name");
                String path=rs.getString("path");
                Boolean isDirectory=rs.getBoolean("is_directory");
                Long size=rs.getLong("size");
                Timestamp lastModified=rs.getTimestamp("last_modified");
                FileMeta meta=new FileMeta(name,path,isDirectory,size,new java.util.Date(lastModified.getTime()));
                System.out.printf("查询文件信息：name=%s,path=%s,is_directory=%s,size=%s," +
                        "last_modified=%s\n",name,path,String.valueOf(isDirectory),String.valueOf(size),
                        Util.parseDate(new java.util.Date(lastModified.getTime())));
                metas.add(meta);
            }
            return metas;
        }catch (Exception e){
            e.printStackTrace();
            throw  new RuntimeException("查询文件信息出错,检查sql查询语句",e);
        }finally {//5.释放资源
            DBUtil.close(connection,ps,rs);
        }
    }
    //数据库保存
    private  void save(FileMeta meta){
        Connection connection=null;
        PreparedStatement statement=null;
        try {
            //获取数据库连接
            connection=DBUtil.getConnection();
            String sql="insert into file_meta (name, path, is_directory, size, last_modified, pinyin, pinyin_first)"+
                    " values(?, ? , ?, ? , ? , ? ,?)";
            //获取操作对象
            statement =connection.prepareStatement(sql);//添加is_directory字段
            statement.setString(1,meta.getName());
            statement.setString(2,meta.getPath());
            statement.setBoolean(3,meta.getDirectory());
            statement.setLong(4,meta.getSize());
            //数据库保存日期类西行，可以按数据库设置的日期格式，以字符串传入
            statement.setString(5,meta.getLastModifiedText());//long类型值转换为日期类型的值
            statement.setString(6,meta.getPinyin());
            statement.setString(7,meta.getPinyinFirst());
            System.out.println("执行文件保存操作："+sql);
            //执行sql
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("文件保存失败，检查一下sql insert语句");
        } finally {
            DBUtil.close(connection,statement);
        }
    }

    public static void main(String[] args) {
        /**
         *         DBInit.init();
         *         File file=new File("E:\\Desktop\\笔记");
         *         FileSave fileSave=new FileSave();
         *         fileSave.save(file);
         *         fileSave.query(file.getParentFile());
         */
        List<FileMeta> locals=new ArrayList<>();
        locals.add(new FileMeta("新建文件夹","D:\\TMP\\maven-test-副本",
                true,0,new Date()));
        locals.add(new FileMeta("中华人民共和国","D:\\TMP\\maven-test-副本",
                true,0,new Date()));
        locals.add(new FileMeta("阿凡达.txt","D:\\TMP\\maven-test-副本\\中华人民共和国",
                true,0,new Date()));

        List<FileMeta> metas=new ArrayList<>();
        metas.add(new FileMeta("新建文件夹","D:\\TMP\\maven-test-副本",
                true,0,new Date()));
        metas.add(new FileMeta("中华人民共和国2","D:\\TMP\\maven-test-副本",
                true,0,new Date()));
        metas.add(new FileMeta("阿凡达.txt","D:\\TMP\\maven-test-副本\\中华人民共和国2",
                true,0,new Date()));
        //如何对比这两个List中的元素是否相同呢，new的在内存中肯定不是一个对象，所以肯定
        //要覆写equals方法  list.contains()，根据业务需要重写哪些属性
        //集合中是否包含某个元素，不代表这个元素的内存地址是真的在里面的
        //在什么条件下可以办到，覆写equals和hashcode，因为调用equals的时候使用相关hashcode


    }
}
//接口回调好处：以后有新的实现类，对现有的程序不会修改，直接在外层使用的时候使用其他的实现类就好，对代码的嵌入性更小，设计层面
//层面更为优雅