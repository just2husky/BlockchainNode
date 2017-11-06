/**
 * Created by chao on 2017/11/6.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ValidatorServer implements Runnable
{
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public ValidatorServer(int port, int poolSize) throws IOException
    {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(poolSize);
        serverSocket.setSoTimeout(100000);
    }

    public void run()
    {
        try {
            logger.info("服务器开始启动 ...");
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
            Thread t = new Thread(new ValidatorServer(port, 4));
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}

class Handler implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);
    private final Socket socket;
    Handler(Socket socket) { this.socket = socket; }
    public void run() {
        // read and service request on socket
        try {
            logger.info("远程主机地址：" + socket.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            logger.info(in.readUTF());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("谢谢连接我：" + socket.getLocalSocketAddress() + "\nGoodbye!");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}