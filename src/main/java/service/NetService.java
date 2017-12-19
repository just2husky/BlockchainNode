package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Block;
import entity.ValidatorAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.JsonUtil;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

/**
 * Created by chao on 2017/12/18.
 */
public class NetService {
    private final static Logger logger = LoggerFactory.getLogger(NetService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private static class LazyHolder {
        private static final NetService INSTANCE = new NetService();
    }
    private NetService (){}
    public static NetService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 向除了 ip:localport 以外的 url 地址广播消息 msg
     * @param ip
     * @param localPort
     * @param msg
     * @throws IOException
     */
    public static void broadcastMsg(String ip, int localPort, String msg) throws IOException {
        List<ValidatorAddress> list = JsonUtil.getValidatorAddressList(Const.ValidatorListFile);
        for (ValidatorAddress va : list) {
            // 排除本机，向 ValidatorListFile 中存储的其他节点发送预准备消息
            if (!((va.getIp().equals(ip) || va.getIp().equals("127.0.0.1")) && va.getPort() == localPort)) {
                Socket broadcastSocket = new Socket(va.getIp(), va.getPort());
                OutputStream outToServer = broadcastSocket.getOutputStream();
                DataOutputStream outputStream = new DataOutputStream(outToServer);
                logger.info("开始向 " + va.getIp() + ":" + va.getPort() + " 发送消息: " + msg);
                outputStream.writeUTF(msg);

                InputStream inFromServer = broadcastSocket.getInputStream();
                DataInputStream inputStream = new DataInputStream(inFromServer);
                String rcvMsg = inputStream.readUTF();
                logger.info("服务器响应消息的结果为： " + rcvMsg);

                broadcastSocket.close();
            }
        }
    }

    /**
     * 向指定 url 发送消息 msg
     * @param msg
     * @param ip
     * @param port
     * @param timeout 连接超时时间
     */
    public String sendMsg(String msg, String ip, int port, int timeout) {
        String rcvMsg = null;
        logger.info("开始发送 msg: " + msg);
        Socket client = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        try {
            client.connect(socketAddress, timeout);
            logger.info("连接到主机：" + ip + " ，端口号：" + port);
        } catch (ConnectException e) {
            logger.error("连接主机：" + ip + " ，端口号：" + port + " 拒绝！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try
        {
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(msg);

            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            rcvMsg = in.readUTF();
            logger.debug("服务器响应： " + rcvMsg);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rcvMsg;
    }
}
