import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用于编写客户端向区块链发送请求的各种命令
 * Created by chao on 2017/11/9.
 */
public class Client {
    public static void main(String [] args)
    {
        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.
                newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < 5 ; i++) {
            es.execute(new Task(es,"task-"+i));
        }

        es.shutdown();
    }
}
