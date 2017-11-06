import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chao on 2017/11/6.
 */
public class Task implements  Runnable{
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);
    private ThreadPoolExecutor es;
    private  String name;

    public Task(ThreadPoolExecutor es, String name) {
        this.es = es;
        this.name = name;
    }

    public void run() {
        long startTime=System.currentTimeMillis();   //获取开始时间
        ValidatorClient.connServer("127.0.0.1", 8000);
        long endTime=System.currentTimeMillis(); //获取结束时间
        long timeout = endTime - startTime;
        logger.info(Thread.currentThread().getName() + "...执行完成..task=" + name +"    耗时：" + timeout + "ms");
    }
}