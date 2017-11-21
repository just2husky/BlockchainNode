package task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ClientService;

/**
 * Created by chao on 2017/11/6.
 */
public class Task implements  Runnable{
    private final static Logger logger = LoggerFactory.getLogger(Task.class);
    private  String name;
    private String serverName;
    private int port;

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, String serverName, int port) {
        this.name = name;
        this.serverName = serverName;
        this.port = port;
    }

    public void run() {
        long startTime=System.currentTimeMillis();   //获取开始时间
        ClientService.connServer(serverName, port);
        long endTime=System.currentTimeMillis(); //获取结束时间
        long timeout = endTime - startTime;
        logger.info(Thread.currentThread().getName() + "...执行完成..task=" + name +"    耗时：" + timeout + "ms");
    }
}