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
import java.util.Map;

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
            logger.info("远程主机地址：" + socket.getRemoteSocketAddress() +
                    ", 本机地址：" + NetUtil.getRealIp() + ":" + socket.getLocalPort());

            DataInputStream in = new DataInputStream(socket.getInputStream());
            String rcvMsg = in.readUTF();
            String msgType = (String) objectMapper.readValue(rcvMsg, Map.class).get("msgType");
            logger.info(msgType);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // 1. 如果socket中接受到的消息为 cliMsg 类型
            if(msgType.equals("cliMsg")) {
                TransactionMessage txMsg = objectMapper.readValue(rcvMsg, TransactionMessage.class);
                PrePrepareMessage ppm = MessageService.genPrePrepareMsg("PrePrepare", txMsg.getTransaction().getTxId());
//                List<ValidatorAddress> list = getValidatorAddressList(ValidatorListFile);
//                for(ValidatorAddress va : list) {
                    Socket sendPrePreSocket = new Socket("127.0.0.1", 8001);
                    String ppmStr = objectMapper.writeValueAsString(ppm);
                    OutputStream outToServer = sendPrePreSocket.getOutputStream();
                    DataOutputStream outputStream = new DataOutputStream(outToServer);
                    logger.info("开始发送 PrePrepareMessage: " + ppmStr);
                    outputStream.writeUTF(ppmStr);

                    InputStream inFromServer = sendPrePreSocket.getInputStream();
                    DataInputStream inputStream = new DataInputStream(inFromServer);
                    String ppRcvMsg = inputStream.readUTF();
                    logger.info("服务器响应： " + ppRcvMsg);

                    sendPrePreSocket.close();
//                }
            }

            // 1. 如果socket中接受到的消息为 PrePrepare 类型
            else if(msgType.equals("PrePrepare")) {
                PrePrepareMessage msg = objectMapper.readValue(rcvMsg, PrePrepareMessage.class);
                logger.info("接收到 PrePrepareMsg：" + rcvMsg);
                logger.info("开始校验 PrePrepareMsg ...");
                String verifyRes = MessageService.verifyPrePrepareMsg(msg);
                logger.info("校验结束，结果为：" + verifyRes);
                out.writeUTF(verifyRes);
            }


//            String tx_json = in.readLine();
//            logger.info("接收信息：" + tx_json);
//            JsonNode jsonNode = objectMapper.readTree(tx_json);
//            // 去除两端的 " 号
//            String msgType = jsonNode.get("msg_type").toString().replace("\"", "");
//            logger.info("msg_type: " + msgType);
//            if (msgType.equals(MsgType.CLI)) {
//                TransactionMessage txMsg = objectMapper.readValue(tx_json, TransactionMessage.class);
//                logger.info("消息类型为客户端发来的请求");
//            }
//
//            PrintWriter out = new PrintWriter(socket.getOutputStream()); //输出，to 客户端
//            out.println("服务器 [" + NetUtil.getRealIp() + ":" + socket.getLocalPort() + "] 接收到你的消息");
            out.writeUTF("\n连接结束");
            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
