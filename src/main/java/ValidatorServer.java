/**
 * Created by chao on 2017/11/6.
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Message;
import entity.Transaction;

import entity.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.MsgType;
import util.NetUtil;

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
            logger.info("服务器 [" + NetUtil.getRealIp() + ":"
                    + serverSocket.getLocalPort() + "] 启动");
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

/**
 * 用于解析 socket 中 msg 的类型，并根据不同的类型，交由不同的其他 Handler 去处理
 */
class Handler implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Socket socket;
    Handler(Socket socket) { this.socket = socket; }

    public void run() {
        // read and service request on socket
        try {
            logger.info("远程主机地址：" + socket.getRemoteSocketAddress() +
                    ", 本机地址：" + NetUtil.getRealIp() + ":" + socket.getLocalPort());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String tx_json = in.readLine();
            logger.info("接收信息："+ tx_json);
            JsonNode jsonNode = objectMapper.readTree(tx_json);
            // 去除两端的 " 号
            String msgType = jsonNode.get("msg_type").toString().replace("\"", "");
            logger.info("msg_type: " + msgType);
            if(msgType.equals(MsgType.CLI)) {
                TransactionMessage txMsg = objectMapper.readValue(tx_json, TransactionMessage.class);
                logger.info("消息类型为客户端发来的请求");
            }

            PrintWriter out = new PrintWriter(socket.getOutputStream()); //输出，to 客户端
            out.println("服务器 [" + NetUtil.getRealIp() + ":" + socket.getLocalPort() + "] 接收到你的消息");
            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 校验Transaction是否符合要求
     * @param transaction Transaction 对象
     * @return 暂时直接返回true
     */
    private boolean verifyTransaction(Transaction transaction) {
        return true;
    }

    /**
     * 校验从客户端发送的请求消息是否满足要求
     * @param msg Message 对象
     * @return 满足则返回 true
     */
    private boolean verfiyMsgFromCli(Message msg) {
        return true;
    }

    private void sendPrePrepareMsg(Message msg) {

    }
}