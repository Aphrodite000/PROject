package app;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import task.*;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {

    @FXML
    private GridPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<FileMeta> fileTable;

    @FXML
    private Label srcDirectory;

    private Thread task;
//第三大步的前景
    public void initialize(URL location, ResourceBundle resources) {
        //界面初始化时，需要初始化数据库和数据库表
        DBInit.init();
        // 添加搜索框监听器，内容改变时执行监听事件
        searchField.textProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //改变之后还要刷新
                freshTable();
            }
        });
    }

    /**
     * 点击选择目录按钮，发生的事件
     * 选择按钮事件，选择的目录的路径会显示在选择目录按钮后边
     *
     *在正在扫描的时候，重新点击了一个选择目录，会中断当前臊面的去扫描新的
     * @param event
     */
    public void choose(Event event) {
        // 选择文件目录
        DirectoryChooser directoryChooser=new DirectoryChooser();
        Window window = rootPane.getScene().getWindow();
        File file = directoryChooser.showDialog(window);
        if(file == null)
            return;
        // 获取选择的目录路径，并显示
        String path = file.getPath();//path要显示在标签里面
        // TODO
        srcDirectory.setText(path);//把路径设置进去，在标签里，返回之后这个标签才会起作用
        //选择了目录，就需要执行目录的扫描任务，请将目录下的子文件和子文件夹都扫描下来
        //因为方法是选择按钮事件，选择按钮，事件完成之后，方法返回之后，set标签才会起作用，
        //也就是说界面会一直卡着，不能扫描一个出来一个，如果扫描任务大，会等待事件比较长
        //所以新启线程来完成，这样就不用等待这个任务完成
        /**
         * 新启动一个线程，只是启动线程，然后线程就格子工作了，choose便不会等在这里
         * 就相当于已经执行完了，然后标签（顶层目录的标签就会出现了，路径会出现在按钮后边）
         * 如果不用多线程扫描的话，直接在方法里扫描，等扫描任务全部进行完，标签路径才能出现
         */

        //改造代码中断之前的任务
        if(task!=null){//在点了选择目录之后，任务很大，可能还没执行完，又点击选择目录
            //不管线程启动了，中断了，还是已经结束了，都可以这样调用
            task.interrupt();//时间轮转到下面的线程，下面的阻塞方法会抛异常
            //如果task为空，说明前面没有没执行完的线程
        }
        //线程池完成扫描任务
        task=new Thread(new Runnable() {
            @Override
            public void run() {

                ScanCallback callback=new FileSave();//文件扫描回调接口，做文件夹下一级子文件和子文件夹保存数据库操作
                FileScanner scanner=new FileScanner(callback);//传入扫描任务类
                try {
                    //做文件任务扫描
                    System.out.println("执行文件扫描任务");//新创建一个类来执行，在task包里FileScanner
                    scanner.scan(path);//1.为了提高效率多线程执行扫描任务,根目录的烧苗
                    //6为什么不用静态，而用实例
                    //.5因为要用线程池，所以要每次都实例化一个FileScanner对象兑现来完成来操作
                    //每一线程用完就取消掉
                    //4先设计代码，然后在实现代码，先用了，在实现方法
                    //2等待文件扫描任务执行完毕，需要阻塞等待
                    System.out.println("等待任务扫描结束"+path);
                    scanner.waitFinish();//可能会抛异常，阻塞方法
                 ////因为是多线程，进去scan方法里面还会   多线程执行，所以如果这里不加阻塞等待
                 ////那么时间片调度，任务还没有扫描完的时候，就已经打印所有任务执行完毕，刷新表格了
                    //刷新表格：将扫描出来的子文件和子文件夹都展示在表格里面
                    System.out.println("所有任务执行完毕，刷新表格");
                    freshTable();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //关闭线程池，所以要放到外层，这里才能使
                }//不能把shutdown放在finally里面,因为对于try里面的代码块来说，关闭应该再刷新表格之前
                //写在finally里面就变成之后了

            }
        });
        task.start();
        //线程开启后就不管了，方法就已经返回了，如果再次点选择目录的话，就会重新执行choose方法
        //但是原有线程还在阻塞等待，第一次在代码块中创建的Thread和FileScanner还在运行
        //以前的还在炖盅运行，只不过把它的引用scanner赋给了一个新的对象
        //现在要中断原来的运行
    }

    // 刷新表格数据
    private void freshTable() {
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();
        // TODO
        //如果选择了某个目录，代表需要在根据搜索框的内容，开进行数据库文件的查询
        String dir=srcDirectory.getText();//获取文件成字符串的形式，即使是用File，到时候数据库中查询的时候还是用它的path
        if(dir!=null&&dir.trim().length()!=0){
            String content=searchField.getText();//根据搜索框，在上面绑定了搜索框内容
              //提供数据库的查询方法
            List<FileMeta> fileMetas= FileSearch.search(dir,content);
            //这个类有多少属性，就会给xml文件里面设置多少
            //用反射调用一个类获取它的属性
            //通过反射把属性值传入表单
            //TODO
            metas.addAll(fileMetas);
            //java的集合框架  collection--
        }
        //方法返回后，javafx表单做什么
        //通过反射获取FileMate的属性（app.fxml文件中定义的属性），在表单里面设置进去，每一个框就有了信息
    }
}