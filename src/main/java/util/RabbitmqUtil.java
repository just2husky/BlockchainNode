package util;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private String queueName;
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

    public RabbitmqUtil(String queueName) {
        this.queueName = queueName;
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
            logger.debug("创建队列：" + this.queueName);
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getQueueName() {
        return this.queueName;
    }
    /**
     * 将 String push 到队列中
     *
     * @param message
     */
    public void push(String message) {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
            channel.basicPublish("", this.queueName, null, message.getBytes());
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
    public void push(List<String> messages) {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            for (String message : messages) {
                channel.queueDeclare(this.queueName, false, false, false, null);
                channel.basicPublish("", this.queueName, null, message.getBytes());
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
    public String pull() {
        String content = null;
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            if(channel.messageCount(this.queueName) > 0) {
                GetResponse response = channel.basicGet(this.queueName, false);
                content = new String(response.getBody());
                logger.debug(content);
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            } else {
                logger.debug("队列为空");
            }
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }


    /**
     *
     * @param limitTime 单位：毫秒
     * @param limitSize 单位: MB
     * @return 接收到的消息 list
     */
    public List<String> pull(double limitTime, double limitSize) {
        List<String> msgList = new ArrayList<String>();
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            long totalLen = 0;
            long beginTime = System.nanoTime();
            //  1. 接收队列中消息的时间是否已超时
            while ((double) (System.nanoTime() - beginTime) / 1000000 < limitTime) {
                if(channel.messageCount(this.queueName) > 0) {
                    GetResponse response = channel.basicGet(this.queueName, false);
                    String msg = new String(response.getBody());
                    // 2. 接收队列中消息的大小是否超过限制
                    totalLen += msg.getBytes(Const.CHAR_SET).length;
                    if (totalLen / Math.pow(1024, 2) < limitSize) {
                        logger.debug("接收到队列消息：" + msg);
                        msgList.add(msg);
                        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

                    } else {
                        logger.info("大小超出限制，停止接收该消息，准备生成区块");
                        // requeue - true if the rejected message should be requeued rather than discarded/dead-lettered
                        channel.basicReject(response.getEnvelope().getDeliveryTag(), true);
                        break;
                    }
                }
                else {
                    logger.debug("队列为空");
                    break;
                }
            }
            logger.debug("接收结束");
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msgList;
    }

}
