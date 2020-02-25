import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {
    private static volatile AtomicInteger count=new AtomicInteger(0);
//这个类是的方法是同步的，类似线程安全的int
    public static void main(String[] args) {
        for(int i=0;i<20;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0;i<10000;i++){
                        //count.incrementAndGet();//++i先获取值在加加
                        count.getAndIncrement();//i++先获取值在加加
                    }
                }
            }).start();
        }
        while(Thread.activeCount()>1){
            Thread.yield();
        }
        System.out.println(count.get());
    }
}
