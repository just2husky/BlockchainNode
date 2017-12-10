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
    public void pull() {
        try {
            Connection conn = factory.newConnection();
            Channel channel = conn.createChannel();
            GetResponse response = channel.basicGet(this.queueName, false);
            logger.info(new String(response.getBody()));
//            long queueLen = channel.messageCount(this.queueName);
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
        RabbitmqUtil rmq = new RabbitmqUtil(Const.QUEUE_NAME);
        List<Transaction> txList = new ArrayList<Transaction>();
        try {
            for (int i = 0; i < 5; i++) {
                Transaction tx = TransactionService.genTx("string" + i, "测试" + i);
                if(i<4) {
                    txList.add(tx);
                }
                rmq.push(tx.toString());
            }
            rmq.push(objectMapper.writeValueAsString(txList));
//            logger.info(objectMapper.writeValueAsString(txList).substring(0,1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> msgList = rmq.pull(100000, 4.0/1024.0);
        for(String msg : msgList) {
            System.out.println(msg);
        }
    }
}
