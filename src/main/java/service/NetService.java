package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.ValidatorAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.JsonUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Created by chao on 2017/12/18.
 */
public class NetService {
    private final static Logger logger = LoggerFactory.getLogger(NetService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

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
}
