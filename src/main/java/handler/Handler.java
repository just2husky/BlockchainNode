package handler;

/**
 * Created by chao on 2017/11/21.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MessageService;
import util.Const;
import util.MongoUtil;
import util.NetUtil;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
//            out.writeUTF("接收到你发来的消息");
//            out.flush();
//            socket.close();

            // 1. 如果socket中接受到的消息为 cliMsg 类型
            if (msgType.equals("cliMsg")) {
                out.writeUTF("接收到你发来的客户端消息，准备校验后广播预准备消息");
                out.flush();
                socket.close();
                procCliMsg(rcvMsg, localPort);
            }

            // 2. 如果socket中接受到的消息为 PrePrepare 类型
            else if (msgType.equals("PrePrepare")) {
                out.writeUTF("接收到你发来的预准备消息，准备校验后广播准备消息");
                out.flush();
                socket.close();
                procPPMsg(rcvMsg, localPort);
            }

            else if (msgType.equals(Const.PM)) {
                out.writeUTF("接收到你发来的准备消息");
                out.flush();
                socket.close();
                logger.info("接收到准备消息");
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


    public static long getSeqNum(String collectionName) throws Exception {
        if(!MongoUtil.collectionExists(collectionName)) {
            logger.info("集合" + collectionName + "不存在，开始创建");
            MongoUtil.insertKV("seqNum", "0", collectionName);
            return 0;
        } else {
            String record = MongoUtil.findFirstDoc(collectionName);
            if(record != null && !record.equals("")) {
                long seqNum = -1;
                try {
                    seqNum = Long.parseLong((String) objectMapper.readValue(record, Map.class).get("seqNum"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return seqNum;

            }
            else {
                throw new Exception("获取 seqNum 失败！");
            }
        }
    }

    public static void updateSeqNum(String collectionName) throws Exception {

        long oldSeqNum = getSeqNum(collectionName);
        MongoUtil.updateKV("seqNum", Long.toString(oldSeqNum), Long.toString(oldSeqNum+1), collectionName);
    }
    /**
     * 处理客户端发送的消息
     * @param rcvMsg 接收到的消息
     * @param localPort 本机的端口
     * @throws IOException
     */
    private void procCliMsg(String rcvMsg, int localPort) throws IOException {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        logger.info("本机地址为：" + url);
        // 将接收到的客户端的消息存在名字为 url 的 collection 中
        MongoUtil.insertJson(rcvMsg, url);
        ClientSendMessage txMsg = objectMapper.readValue(rcvMsg, ClientSendMessage.class);
//        String seqNum =
        PrePrepareMessage ppm = MessageService.genPrePrepareMsg("PrePrepare", txMsg.getTransaction().getTxId());
        broadcastMsg(NetUtil.getRealIp(), localPort, objectMapper.writeValueAsString(ppm));
//        List<ValidatorAddress> list = getValidatorAddressList(ValidatorListFile);
//        for (ValidatorAddress va : list) {
//            // 排除本机，向 ValidatorListFile 中存储的其他节点发送预准备消息
//            if (!((va.getIp().equals(realIp) || va.getIp().equals("127.0.0.1")) && va.getPort() == localPort)) {
//                Socket sendPrePreSocket = new Socket(va.getIp(), va.getPort());
//                String ppmStr = objectMapper.writeValueAsString(ppm);
//                OutputStream outToServer = sendPrePreSocket.getOutputStream();
//                DataOutputStream outputStream = new DataOutputStream(outToServer);
//                logger.info("开始向 " + va.getIp() + ":" + va.getPort() + " 发送 PrePrepareMessage: " + ppmStr);
//                outputStream.writeUTF(ppmStr);
//
//                InputStream inFromServer = sendPrePreSocket.getInputStream();
//                DataInputStream inputStream = new DataInputStream(inFromServer);
//                String ppRcvMsg = inputStream.readUTF();
//                logger.info("服务器响应 PrePrepareMessage 的结果为： " + ppRcvMsg);
//
//                sendPrePreSocket.close();
//            }
//        }
    }

    private String procPPMsg(String rcvMsg, int localPort) throws IOException {
        PrePrepareMessage msg = objectMapper.readValue(rcvMsg, PrePrepareMessage.class);
        logger.info("接收到 PrePrepareMsg：" + rcvMsg);
        logger.info("开始校验 PrePrepareMsg ...");
        String verifyRes = MessageService.verifyPrePrepareMsg(msg);
        logger.info("校验结束，结果为：" + verifyRes);

        // 若 PrePrepareMessage 验证结果为 true， 则向其余节点发送 PrepareMessage。
        if(verifyRes.equals("true")) {
            PrepareMessage pm = MessageService.genPrepareMsg(Const.PM, NetUtil.getRealIp(), localPort);
            broadcastMsg(NetUtil.getRealIp(), localPort, objectMapper.writeValueAsString(pm));
        }
        return verifyRes;
    }

    /**
     * 向除了 ip:localport 以外的 url 地址广播消息 msg
     * @param ip
     * @param localPort
     * @param msg
     * @throws IOException
     */
    private static void broadcastMsg(String ip, int localPort, String msg) throws IOException {
        List<ValidatorAddress> list = getValidatorAddressList(ValidatorListFile);
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
