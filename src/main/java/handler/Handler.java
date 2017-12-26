package handler;

/**
 * Created by chao on 2017/11/21.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.*;
import util.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;

import static service.MessageService.getSeqNum;
import static service.MessageService.updateSeqNum;

/**
 * 用于解析 socket 中 msg 的类型，并根据不同的类型，交由不同的其他 Handler 去处理
 */
public class Handler implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Handler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Socket socket;
    private CommitMessageService cmtms = new CommitMessageService();

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
            logger.debug("接收到的 Msg 类型为： [" + msgType + "]");
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//            out.writeUTF("接收到你发来的消息");
//            out.flush();
//            socket.close();

            // 如果socket中接受到的消息为 blockMsg 类型
            if (msgType.equals(Const.BM)) {
                out.writeUTF("接收到你发来的客户端 Block 消息，准备校验后广播预准备消息");
                out.flush();
                socket.close();
                try {
                    BlockMessageService.procBlockMsg(rcvMsg, localPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(msgType.equals(Const.TXM)) {
                out.writeUTF("接收到你发来的客户端 Transaction 消息，准备校验后广播预准备消息");
                out.flush();
                socket.close();
                TransactionMessageService.procTxMsg(rcvMsg, localPort);
            }
            // 如果socket中接受到的消息为 PrePrepare 类型
            else if (msgType.equals(Const.PPM)) {
                out.writeUTF("接收到你发来的预准备消息，准备校验后广播准备消息");
                out.flush();
                socket.close();
                PrePrepareMessageService.procPPMsg(rcvMsg, localPort);
            }
            // 如果socket中接受到的消息为 Prepare 类型
            else if (msgType.equals(Const.PM)) {
                out.writeUTF("接收到你发来的准备消息");
                out.flush();
                socket.close();
                logger.info("接收到准备消息");
                PrepareMessageService.procPMsg(rcvMsg, localPort);
            }
            // 如果socket中接受到的消息为 commit 类型
            else if (msgType.equals(Const.CMTM)) {
                out.writeUTF("接收到你发来的commit消息");
                out.flush();
                socket.close();
                logger.info("接收到commit消息");
                cmtms.procCMTM(rcvMsg, localPort);
            }
            else {
                out.writeUTF("未知的 msgType 类型");
                out.flush();
                socket.close();
                logger.error("未知的 msgType 类型");
            }

//            out.writeUTF("\n连接结束");
//            out.flush();
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(getSeqNum("seq"));
            updateSeqNum("seq");
            System.out.println(getSeqNum("seq"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
