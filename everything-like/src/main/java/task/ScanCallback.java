package task;

import java.io.File;
//文件扫描的回调接口
public interface ScanCallback {
    //对对于文件夹进行操作，对于文件夹的扫描任务进行回调
    //将文件夹下一级子文件和者子文件夹保存到数据库
    void callback(File dir);
}
