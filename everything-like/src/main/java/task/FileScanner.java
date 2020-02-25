package task;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//第四大步
//文件扫描类
public class FileScanner {
    //创建线程池
    //1.核心线程数，适中运行的线程数量
    //正式工
    //2.最大的线程数  有新任务，并且当前运行的线程数，小于最大线程数，就会创建新的线程，
    //正式工+临时工
    //3-4 时间和数量，超过3这个数量，4这个时间单位，2-1最大线程减去核心线程数的这些线程就会关闭
    //5.阻塞队列
    //6.如果超出工作队列的长度，任务要进行处理的方式（4中）,谁调用线程池取执行这个任务
    //可是队列满了，那它就去执行，在本身取执行，线程在本调用的代码里面去执行
    //private ThreadPoolExecutor pool=new ThreadPoolExecutor(
      //      3,3, 0, TimeUnit.MICROSECONDS,
        //    new LinkedBlockingDeque<>(),new ThreadPoolExecutor.AbortPolicy());
    //C这个方式是：谁调用我谁处理任务，不拒绝这个任务调用到pool.shutdown()
    //方式；还有退出，丢弃老的，丢弃新的
    //与abort策略一样
    private ExecutorService pool=Executors.newFixedThreadPool(4);
    /**
     * 解释：当前线程数小于最大的线程数时，有新任务来了，可以雇临时工（也就是线程）
     * 来干活，等活干完了，等待0个时间，就让这些临时工走掉，只留下核心线程数
     */

    //定义一个计数器。来判断等待什么时候结束,不传入数值表示初始化为0
    private  volatile AtomicInteger count=new AtomicInteger();
    //给线程等待的一个锁对象
    private  Object lock=new Object();
    private  ScanCallback callback;
    public FileScanner(ScanCallback callback) {
        this.callback=callback;
    }
    //之前多线程讲解的是一种快捷的方式
    //private ExecutorService exe= Executors.newFixedThreadPool(4);  内部还是调用上面的方法创建的

    /**
     * 扫描文件目录，不知道有多少子文件夹，不知道启动多少个线程数
     * @param path
     */
    public  void scan(String path) {// F下360downloads
        count.incrementAndGet();//++i;启动根路率扫描任务
        //1.多级扫描，每一级都启动新线程扫描,
        //递归
       doScan(new File(path));//把根目录放进去，作为待处理的文件夹
        //方法套用是为了scan方法可以返回，下面的阻塞的方法就可以阻塞，到时候就可以中断
    }
    private void doScan(File dir){
        //2.扫描这个文件夹
        //启动新任务放到线程池里面去运行
        pool.execute(new Runnable() {//3.线程池执行的任务类
            //线程池巡行的方法
            @Override
            public void run() {
                try{
                    //如果是文件的话，在这一步就被保存了
                    //把下一级的子文件和子文件夹都一保存，下面如果是文件夹就递归
                    // 让他打印处理子文件或者子文件夹，下面只是递归
                    callback.callback(dir);
                    File[] children=dir.listFiles();//下一级文件和文件夹
                    if(children!=null){
                        for(File child:children){
                            if(child.isDirectory()){
                                //如果是文件夹，要递归处理
                                //System.out.println("文件夹"+child.getPath());//把打印替换到数据库里
                                //把打印改成保存进数据库
                                count.incrementAndGet();//++i;启动子文件夹扫描任务
                                System.out.println("当前任务数"+count.get());
                                doScan(child);//线程数是根据递归调用才知道多少个
                                //每次递归启动新任务在线程池运行
                            }
                        }
                    }
                }finally {
                    //保证线程的计数，不管是否出现异常，都能进行减一操作
                    //就算出现异常退出了，也要执行下面的方法
                    int r=count.decrementAndGet();
                    if(r==0){
                        synchronized (lock){
                            lock.notify();
                        }
                    }//这段代码可能会出现问题
                    //在int r这段代码之前上面的代码出现了异常，这段代码执行不到了会造成
                    //之后的代码会之一阻塞等待，所以这样写不谨慎，加一个finally
                    //线程方法出现异常，整个线程退出
                    //如果run方法里面抛了异常，方法退出，那么整个线程也就退出了
                }
            }
        });
    }
    /**
     * 等待扫描任务结束（scan方法结束）
     * 多线程等待的方式
     * 1.join（）
     * 2.wait()线程间等待
     */
    public void waitFinish() throws InterruptedException {
        //采用计数器，进入一个任务的时候+1，结尾的时候-1，如果等于0，则任务已经执行完毕
        //等待所有线程都运行完毕
        try {
            synchronized (lock){
                lock.wait();
            }
        } finally {
            //无论上面抛不抛出异常，都把线程池给关闭了
            //阻塞等待，直到任务完成，完成之后还要需要关闭线程池
            System.out.println("关闭线程池");
            //pool.shutdown();
            pool.shutdownNow();
        }
    }


    public static void main(String[] args) {

    }
}
