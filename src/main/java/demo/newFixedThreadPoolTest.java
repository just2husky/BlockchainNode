package demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chao on 2017/11/6.
 */
public class newFixedThreadPoolTest {
    public static void main(String[] args) {
//        Runtime.getRuntime().availableProcessors();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for(int i=0;i<10;i++) {
            final int index = i;
            fixedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        System.out.println(index);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }
}
