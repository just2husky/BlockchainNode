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
            logger.info(" [x] Sent '" + message + "'");
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
                logger.info(" [x] Sent '" + message + "'");
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
            channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            channel.close();
            conn.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        try {
            for (int i = 0; i < 20; i++) {
                Transaction tx = TransactionService.genTx("string" + i, "测试" + i);
                push(tx.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pull();
    }
}
