package handler;

/**
 * Created by chao on 2017/11/21.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MsgType;
import util.NetUtil;

import java.io.*;
import java.net.Socket;

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
            System.out.println(in.readUTF());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("谢谢连接我：" + socket.getLocalSocketAddress() + "\nGoodbye!");
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
//            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
