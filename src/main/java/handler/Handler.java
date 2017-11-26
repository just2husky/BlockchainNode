package handler;

/**
 * Created by chao on 2017/11/21.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MessageService;
import util.NetUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static util.Const.ValidatorListFile;
import static util.JsonUtil.getValidatorAddressList;

/**
 * 用于解析 socket 中 msg 的类型，并根据不同的类型，交由不同的其他 Handler 去处理
 */
public class Handler implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        // read and service request on socket
        try {
            String realIp = NetUtil.getRealIp();
            int localPort = socket.getLocalPort();
            logger.info("远程主机地址：" + socket.getRemoteSocketAddress());

            DataInputStream in = new DataInputStream(socket.getInputStream());
            String rcvMsg = in.readUTF();
            String msgType = (String) objectMapper.readValue(rcvMsg, Map.class).get("msgType");
            logger.info("接收到的 Msg 类型为： " + msgType);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // 1. 如果socket中接受到的消息为 cliMsg 类型
            if (msgType.equals("cliMsg")) {
                procCliMsg(rcvMsg, localPort);
            }

            // 1. 如果socket中接受到的消息为 PrePrepare 类型
            else if (msgType.equals("PrePrepare")) {
                out.writeUTF(procPPMsg(rcvMsg));
            }

            out.writeUTF("\n连接结束");
            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理客户端发送的消息
     * @param rcvMsg 接收到的消息
     * @param localPort 本机的端口
     * @throws IOException
     */
    private void procCliMsg(String rcvMsg, int localPort) throws IOException {
        String realIp = NetUtil.getRealIp();
        logger.info("本机地址为：" + realIp + ":" + localPort);
        TransactionMessage txMsg = objectMapper.readValue(rcvMsg, TransactionMessage.class);
        PrePrepareMessage ppm = MessageService.genPrePrepareMsg("PrePrepare", txMsg.getTransaction().getTxId());
        List<ValidatorAddress> list = getValidatorAddressList(ValidatorListFile);
        for (ValidatorAddress va : list) {
            // 排除本机，向 ValidatorListFile 中存储的其他节点发送预准备消息
            if (!((va.getIp().equals(realIp) || va.getIp().equals("127.0.0.1")) && va.getPort() == localPort)) {
                Socket sendPrePreSocket = new Socket(va.getIp(), va.getPort());
                String ppmStr = objectMapper.writeValueAsString(ppm);
                OutputStream outToServer = sendPrePreSocket.getOutputStream();
                DataOutputStream outputStream = new DataOutputStream(outToServer);
                logger.info("开始向 " + va.getIp() + ":" + va.getPort() + " 发送 PrePrepareMessage: " + ppmStr);
                outputStream.writeUTF(ppmStr);

                InputStream inFromServer = sendPrePreSocket.getInputStream();
                DataInputStream inputStream = new DataInputStream(inFromServer);
                String ppRcvMsg = inputStream.readUTF();
                logger.info("服务器响应 PrePrepareMessage 的结果为： " + ppRcvMsg);

                sendPrePreSocket.close();
            }
        }
    }

    private String procPPMsg(String rcvMsg) throws IOException {
        PrePrepareMessage msg = objectMapper.readValue(rcvMsg, PrePrepareMessage.class);
        logger.info("接收到 PrePrepareMsg：" + rcvMsg);
        logger.info("开始校验 PrePrepareMsg ...");
        String verifyRes = MessageService.verifyPrePrepareMsg(msg);
        logger.info("校验结束，结果为：" + verifyRes);
        return verifyRes;
    }
}
