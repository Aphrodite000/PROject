package task;

import app.FileMeta;
import util.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FileSearch {
    //提供文件信息查询操作
    //根据文件目录，搜索框内容进行查询
    public static List<FileMeta> search(String dir,String content){
        List<FileMeta>  metas =new ArrayList<>();
        Connection connection=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try{
            connection=DBUtil.getConnection();//根据拼音去查询，获取值的时候是不要的，不用显示
            String sql="select name, path, is_directory, size, last_modified" +
                    " from file_meta where " +
                    " (path=? or path like ?)";//搜索框内容可能为空
            if(content !=null&&content.trim().length()!=0){     //路径和文件名称都匹配上才说明找到了
                sql+=" and (name like ? or pinyin like ? or pinyin_first like ?)";
            }
            ps=connection.prepareStatement(sql);
            //占位符设值
            ps.setString(1,dir);
            ps.setString(2,dir+ File.separator+"%");//加上文件分隔符，在肩上模糊查询
            if(content !=null&&content.trim().length()!=0){
                ps.setString(3,"%"+content+"%");//前后都是百分号，中间字也可以搜索到
                ps.setString(4,"%"+content+"%");
                ps.setString(5,"%"+content+"%");
            }
            //执行sql语句
            rs=ps.executeQuery();
            //处理结果集
            rs=ps.executeQuery();
            while(rs.next()){
                String name=rs.getString("name");//这个列名一定要和sql语句一致
                String path=rs.getString("path");
                Boolean isDirectory=rs.getBoolean("is_directory");
                Long size=rs.getLong("size");
                Timestamp lastModified=rs.getTimestamp("last_modified");
                FileMeta meta=new FileMeta(name,path,isDirectory,size,new java.util.Date(lastModified.getTime()));
                metas.add(meta);
            }
        }catch (Exception e){
            throw new RuntimeException("数据库文件查询失败,路径是"+dir+"搜索内容："+content,e);
        }finally {
            DBUtil.close(connection,ps,rs);
        }
        return metas;
    }
}
