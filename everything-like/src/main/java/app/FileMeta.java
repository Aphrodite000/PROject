package app;


import util.PinyinUtil;
import util.Util;

import java.io.File;
import java.util.Date;
import java.util.Objects;

//和sql和空间对应起来
public class FileMeta {
//sql的字段都要写出来
    //文件名称
    private String name;
    //文件所在父目录的路径
    private String path;
    //long类型要用包装类型，不能用基础类型，因为包装类型默认为Null
    //文件大小
    private  Long size;
    //文件上次修改时间
    private Date  lastModified;
    //是否是文件夹
    private boolean isDirectory;//因为数据库删除的时候，要判断，如果是文件夹，连底下的子文件也要一起删除
    //给客户端控件使用，和app.fxml中定义的一致；
    private  String sizeText;
    //和app.fxml中定义的一致；
    private String lastModifiedText;
    //文件名拼音
    private String pinyin;
    //文件名拼音首字母
    private String pinyinFirst;

    //通过文件设置属性
    public FileMeta(File file){
        //构造方法复用
        this(file.getName(),file.getParent(),file.isDirectory(),file.length(),new Date(file.lastModified()));
    }
    //通过数据库获取的数据设置Filemate  这里可以用数据类型long 因为肯定有值
    public FileMeta(String name ,String path,Boolean isDirectory,long size,Date lastMooified){
        this.name=name;
        this.path=path;
        this.size=size;
        this.isDirectory=isDirectory;
        this.lastModified=lastMooified;
        if(PinyinUtil.containsChinese(name)){
            String[] pinyins=PinyinUtil.get(name);
            pinyin=pinyins[0];
            pinyinFirst=pinyins[1];
        }
        //客户端表格控件文件大小，文件上次修改时间的设置
        sizeText= Util.parseSize(size);
        lastModifiedText=Util.parseDate(lastMooified);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getSizeText() {
        return sizeText;
    }

    public void setSizeText(String sizeText) {
        this.sizeText = sizeText;
    }

    public String getLastModifiedText() {
        return lastModifiedText;
    }

    public void setLastModifiedText(String lastModifiedText) {
        this.lastModifiedText = lastModifiedText;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getPinyinFirst() {
        return pinyinFirst;
    }

    public void setPinyinFirst(String pinyinFirst) {
        this.pinyinFirst = pinyinFirst;
    }

    /**
     *
     * public boolean isDirectory() {
     *         return isDirectory;
     *     }
     */
    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
    public boolean getDirectory(){
        return isDirectory;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMeta fileMeta = (FileMeta) o;
        return isDirectory == fileMeta.isDirectory &&
                Objects.equals(name, fileMeta.name) &&
                Objects.equals(path, fileMeta.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, isDirectory);
    }

    @Override
    public String toString() {
        return "FileMeta{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", isDirectory=" + isDirectory +
                '}';
    }
}
