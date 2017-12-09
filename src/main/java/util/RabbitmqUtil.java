package util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.TransactionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by chao on 2017/12/9.
 */

public class RabbitmqUtil {
    private final static Logger logger = LoggerFactory.getLogger(RabbitmqUtil.class);
    private final static String QUEUE_NAME = Const.QUEUE_NAME;
    private static ConnectionFactory factory = new ConnectionFactory();

    static {
        String userName = "admin";
        String password = "admin";
        String hostName = "127.0.0.1";
        int portNumber = 5672;

        factory.setUsername(userName);
        factory.setPassword(password);
//        factory.setVirtualHost(virtualHost);
        factory.setHost(hostName);
        factory.setPort(portNumber);
    }

    /**
     * 将 String push 到队列中
     *
     * @param message
     */
    public static void push(String message) {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            logger.debug(" [x] Sent '" + message + "'");
            channel.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将 list push 到队列中
     *
     * @param messages
     */
    public static void push(List<String> messages) {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            for (String message : messages) {
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                logger.debug(" [x] Sent '" + message + "'");
            }
            channel.close();
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从队列中获取一条消息
     */
    public static void pull() {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            GetResponse response = channel.basicGet(QUEUE_NAME, false);
            logger.info(new String(response.getBody()));
//            long queueLen = channel.messageCount(QUEUE_NAME);
//            logger.info("队列长度： " + queueLen);
            channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param limitTime 单位：毫秒
     * @param limitSize 单位: MB
     * @return 接收到的消息 list
     */
    public static List<String> pull(double limitTime, double limitSize) {
        List<String> msgList = new ArrayList<String>();
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            long totalLen = 0;
            long beginTime = System.nanoTime();
            //  1. 接收队列中消息的时间是否已超时
            while ((double) (System.nanoTime() - beginTime) / 1000000 < limitTime) {
                if(channel.messageCount(QUEUE_NAME) > 0) {
                    GetResponse response = channel.basicGet(QUEUE_NAME, false);
                    String msg = new String(response.getBody());
                    // 2. 接收队列中消息的大小是否超过限制
                    totalLen += msg.getBytes(Const.CHAR_SET).length;
                    if (totalLen / Math.pow(1024, 2) < limitSize) {
                        logger.debug("接收到队列消息：" + msg);
                        msgList.add(msg);
                        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

                    } else {
                        logger.info("大小超出限制，停止接收该消息");
                        // requeue - true if the rejected message should be requeued rather than discarded/dead-lettered
                        channel.basicReject(response.getEnvelope().getDeliveryTag(), true);
                        break;
                    }
                }
                else {
                    logger.info("队列为空");
                    break;
                }
            }
            logger.info("接收结束");
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msgList;
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        try {
            for (int i = 0; i < 200; i++) {
                Transaction tx = TransactionService.genTx("string" + i, "测试" + i);
                push(tx.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> msgList = pull(100000, 2.0/1024.0);
        for(String msg : msgList) {
            System.out.println(msg);
        }
    }
}
