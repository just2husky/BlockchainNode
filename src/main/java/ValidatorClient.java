/**
 * Created by chao on 2017/11/6.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ValidatorClient
{
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);

    /**
     * 根据服务器的域名或IP地址，以及端口，访问服务器
     * @param serverName 服务器的域名或IP地址
     * @param port 端口
     */
    public static void connServer(String serverName, int port) {
        try
        {
            String threadName = Thread.currentThread().getName();
            logger.info(threadName + ": 连接到主机：" + serverName + " ，端口号：" + port);
            Socket client = new Socket(serverName, port);
            logger.info(threadName + ": 远程主机地址：" + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            out.writeUTF("Hello from " + client.getLocalSocketAddress());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            logger.info(threadName + ": 服务器响应： " + in.readUTF());
            client.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
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