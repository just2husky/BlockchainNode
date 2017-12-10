package socket;

import entity.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BlockMessageService;
import service.BlockService;
import util.Const;

import java.io.*;
import java.net.*;

/**
 * Created by chao on 2017/12/10.
 */
public class Blocker implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(Blocker.class);
    private long timeInterval = 5000; //生成区块并发送的频率
    private int timeout = 5000; // Blocker 连接 Validator 的超时时间
    String primaryValidatorIP = "127.0.0.1";
    int primaryValidatorPort = 8000;
    public void run() {
        String queueName = Const.QUEUE_NAME;
        String preBlockId = "0";
        double limitTime = 100000; // 单位毫秒
        double limitSize = 2.0/1024.0; // 单位 MB
        Block block;
        while (true) {
            block = BlockService.genBlock(preBlockId, queueName, limitTime, limitSize);
            if (block != null) {
                logger.info("生成 block：" + block.getBlockId());
                preBlockId = block.getBlockId();
                logger.info("开始发送 block：");
                sendBlock(block);
            }

            try {
                Thread.currentThread().sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("Duplicates")
    public void sendBlock(Block block) {
        Socket client = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(primaryValidatorIP, primaryValidatorPort);
        try {
            client.connect(socketAddress, timeout);
            logger.info("连接到主机：" + primaryValidatorIP + " ，端口号：" + primaryValidatorPort);
            logger.info("远程主机地址：" + client.getRemoteSocketAddress());
        } catch (ConnectException e) {
            logger.error("连接主机：" + primaryValidatorIP + " ，端口号：" + primaryValidatorPort + " 拒绝！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try
        {
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(BlockMessageService.genInstance(block).toString());

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            logger.info("服务器响应： " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Thread(new Blocker()).start();
    }
}
