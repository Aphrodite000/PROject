import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//引入javaFx技术，完成客户端代码编写
public class Main extends Application {//打开客户端，加载插件文件，全部都是有关界面元素的都在app.fxml里面配置好了
//启动方法 默认调用文件
    @Override
    public void start(Stage primaryStage) throws Exception{
        //相对路径，本来就在根目录classes的下一层
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("app.fxml"));
        primaryStage.setTitle("文件搜索");
        primaryStage.setScene(new Scene(root, 1000, 800));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
    //launch方法会默认调start方法

    //显示客户端图像
    //入口类   加载javafx卑职  渲染客户端空间  加载 app.Controller  initialize 初始化执行
    //choose 每次选择目录调用该方法
}