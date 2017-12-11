package socket;

import handler.CommittedMsgHandler;
import handler.PreparedMsgHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.NetUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import handler.Handler;
/**
 * Created by chao on 2017/11/21.
 */
@SuppressWarnings("InfiniteLoopStatement")
public class ValidatorServer implements Runnable
{
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final ExecutorService pdmhPool;

    public ValidatorServer(int port, int poolSize) throws IOException
    {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(poolSize);
        pdmhPool = Executors.newFixedThreadPool(poolSize);
//        serverSocket.setSoTimeout(100000);
    }

    public void run()
    {
        try {
            logger.info("服务器 [" + NetUtil.getRealIp() + ":"
                    + serverSocket.getLocalPort() + "] 启动");
            logger.info("启动检测生成 PreparedMessage 的服务器");
            new Thread(new PreparedMsgHandler(NetUtil.getRealIp(), serverSocket.getLocalPort())).start();
            logger.info("启动检测生成 CommittedMessage 的服务器");
            new Thread(new CommittedMsgHandler(NetUtil.getRealIp(), serverSocket.getLocalPort())).start();

            while(true) {
                threadPool.execute(new Handler(serverSocket.accept()));
            }
        } catch (IOException ex) {
            threadPool.shutdown();
        }

    }

    public static void main(String [] args)
    {
        int port = 8000;
        try
        {
            Thread t = new Thread(new ValidatorServer(port, Runtime.getRuntime().availableProcessors()));
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
