package handler;

/**
 * Created by chao on 2017/11/21.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.*;
import util.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static service.MessageService.getSeqNum;
import static service.MessageService.updateSeqNum;
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
            logger.info("接收到的 Msg 类型为： [" + msgType + "]");
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//            out.writeUTF("接收到你发来的消息");
//            out.flush();
//            socket.close();

            // 如果socket中接受到的消息为 blockMsg 类型
            if (msgType.equals(Const.BM)) {
                out.writeUTF("接收到你发来的客户端消息，准备校验后广播预准备消息");
                out.flush();
                socket.close();
                try {
                    procBlockMsg(rcvMsg, localPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 如果socket中接受到的消息为 PrePrepare 类型
            else if (msgType.equals(Const.PPM)) {
                out.writeUTF("接收到你发来的预准备消息，准备校验后广播准备消息");
                out.flush();
                socket.close();
                procPPMsg(rcvMsg, localPort);
            }
            // 如果socket中接受到的消息为 Prepare 类型
            else if (msgType.equals(Const.PM)) {
                out.writeUTF("接收到你发来的准备消息");
                out.flush();
                socket.close();
                logger.info("接收到准备消息");
                procPMsg(rcvMsg, localPort);
            }
            // 如果socket中接受到的消息为 commit 类型
            else if (msgType.equals(Const.CMTM)) {
                out.writeUTF("接收到你发来的commit消息");
                out.flush();
                socket.close();
                logger.info("接收到commit消息");
                procCMTM(rcvMsg, localPort);
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

    /**
     * 处理客户端发送的消息
     * @param rcvMsg 接收到的消息
     * @param localPort 本机的端口
     * @throws IOException
     */
    private void procBlockMsg(String rcvMsg, int localPort) throws Exception {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        logger.info("本机地址为：" + url);
        // 1. 将从客户端收到的 Block Message 存入到集合中
        String cmCollection = url + "." + Const.BM;
        if(BlockMessageService.save(rcvMsg, cmCollection)) {
            logger.info("Block Message 存入成功");
        } else {
            logger.info("Block Message 已存在");
        }

        // 2. 从集合中取出给当前 PrePrepareMessage 分配的序列号
        long seqNum = updateSeqNum(url + ".seqNum");

        // 3. 根据 Block Message 生成 PrePrepareMessage，存入到集合中
        String ppmCollection = url + "." + Const.PPM;
        BlockMessage blockMsg = objectMapper.readValue(rcvMsg, BlockMessage.class);
        PrePrepareMessage ppm = PrePrepareMessageService.genInstance(Long.toString(seqNum), blockMsg);
        PrePrepareMessageService.save(ppm, ppmCollection);

        // 4. 主节点向其他备份节点广播 PrePrepareMessage
        broadcastMsg(NetUtil.getRealIp(), localPort, objectMapper.writeValueAsString(ppm));
    }

    /**
     * 处理接收到的预准备消息
     * @param rcvMsg
     * @param localPort
     * @return
     * @throws IOException
     */
    private boolean procPPMsg(String rcvMsg, int localPort) throws IOException {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 PrePrepareMessage
        PrePrepareMessage ppm = objectMapper.readValue(rcvMsg, PrePrepareMessage.class);
        logger.info("接收到 PrePrepareMsg：" + ppm.getMsgId());
        logger.info("开始校验 PrePrepareMsg ...");
        boolean verifyRes = PrePrepareMessageService.verify(ppm);
        logger.info("校验结束，结果为：" + verifyRes);

        if(verifyRes) {
            // 2. 校验结果为 true ，将 PrePrepareMessage 存入到集合中
            String ppmCollection = url + "." + Const.PPM;
            if(PrePrepareMessageService.save(ppm, ppmCollection)) {
                logger.info("PrePrepareMessage [" + ppm.getMsgId() + "] 已存入数据库");
            } else {
                logger.info("PrePrepareMessage [" + ppm.getMsgId() + "] 已存在");
            }

            // 3. 生成 PrepareMessage，存入集合，并向其他节点进行广播
            PrepareMessage pm = MessageService.genPrepareMsg(ppm.getSignature(), ppm.getViewId(), ppm.getSeqNum(),
                    NetUtil.getRealIp(), localPort);
            String pmCollection = url + "." + Const.PM;
            MessageService.savePMsg(pm, pmCollection);
            logger.info("PrepareMessage [" + pm.getMsgId() + "] 已存入数据库");
            broadcastMsg(NetUtil.getRealIp(), localPort, pm.toString());
        }
        return verifyRes;
    }

    /**
     * 处理准备消息
     * 只要准备消息的签名是正确的，它们的视图编号等于副本的当前视图，并且它们的序列号介于 h 和 H，
     * 副本节点（包括主节点）便接受准备消息，并将它们添加到日志中。
     * @param rcvMsg
     * @param localPort
     * @return
     */
    private boolean procPMsg(String rcvMsg, int localPort) throws IOException {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 PrepareMessage
        PrepareMessage pm = objectMapper.readValue(rcvMsg, PrepareMessage.class);
        logger.info("接收到 PrepareMsg：" + rcvMsg);
        logger.info("开始校验 PrepareMsg ...");
        boolean verifyRes = MessageService.verifyPrepareMsg(pm);
        logger.info("校验结束，结果为：" + verifyRes);

        if(verifyRes) {
            String pmCollection = url + "." + Const.PM;
            String ppmCollection = url + "." + Const.PPM;
            String cmtmCollection = url + "." + Const.CMTM;
            // 2.  PrepareMessage 存入前检验
            PrePrepareMessage ppm = MongoUtil.findPPMById(SignatureUtil.getSha256Base64(pm.getPpmSign()), ppmCollection);

            //  统计 ppmSign 出现的次数
            int count = MongoUtil.countPPMSign(pm.getPpmSign(), pm.getViewId(), pm.getSeqNum(), pmCollection);

            // 3. 将 PrePrepareMessage 存入到集合中
            if(MessageService.savePMsg(pm, pmCollection)) {
                logger.info("PrepareMessage [" + pm.getMsgId() + "] 存入数据库");
            } else {
                logger.info("PrepareMessage [" + pm.getMsgId() + "] 已存在");
            }

            logger.info("count = " + count);
            // 4. 达成 count >= 2 * f 后存入到集合中
            if (2 * PeerUtil.getFaultCount() <= count) {
                logger.info("开始生成 PreparedMessage 并存入数据库");
                String pdmCollection = url + "." + Const.PDM;
                PreparedMessage pdm = PreparedMessageService.genInstance(ppm.getBlockMsg().getMsgId(), ppm.getViewId(),
                        ppm.getSeqNum(), NetUtil.getRealIp(), localPort);
                if(PreparedMessageService.save(pdm, pdmCollection)) {
                    logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存入数据库");
                    CommitMessage cmtm = CommitMessageService.genCommitMsg(ppm.getSignature(), ppm.getViewId(),
                            ppm.getSeqNum(), NetUtil.getRealIp(), localPort);
                    logger.info("commit message: " + cmtm.toString());
                    if(CommitMessageService.save(cmtm, cmtmCollection)) {
                        logger.info("CommitMessage [" + cmtm.getMsgId() + "] 已存入数据库");
                        broadcastMsg(NetUtil.getRealIp(), localPort, cmtm.toString());
                    } else {
                        logger.info("CommitMessage [" + pdm.getMsgId() + "] 已存在");
                    }
                } else {
                    logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存在");
                }
            } else {
                logger.info("Prepare Message 数量不够");
            }

            // 5. 生成 commit message 存入集合中，并广播给其他节点

        }


        return true;
    }

    /**
     * 处理接收到的 commit message
     * @param rcvMsg
     * @param localPort
     * @throws IOException
     */
    public static void procCMTM(String rcvMsg, int localPort) throws IOException {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 CommitMessage
        CommitMessage cmtm = objectMapper.readValue(rcvMsg, CommitMessage.class);
        logger.info("接收到 CommitMsg：" + rcvMsg);
        logger.info("开始校验 CommitMsg ...");
        boolean verifyRes = CommitMessageService.verify(cmtm);
        logger.info("校验结束，结果为：" + verifyRes);

        if(verifyRes) {
            String cmtmCollection = url + "." + Const.CMTM;
            String cmtdmCollection = url + "." + Const.CMTDM;
            String ppmCollection = url + "." + Const.PPM;
            String blockChainCollection = url + "." + Const.BLOCK_CHAIN;

            PrePrepareMessage ppm = MongoUtil.findPPMById(SignatureUtil.getSha256Base64(cmtm.getPpmSign()), ppmCollection);
            if(ppm != null) {
                // 1. 统计 ppmSign 出现的次数
                int count = MongoUtil.countPPMSign(cmtm.getPpmSign(), cmtm.getViewId(), cmtm.getSeqNum(), cmtmCollection);

                // 2. 将 CommitMessage 存入到集合中
                if (CommitMessageService.save(cmtm, cmtmCollection)) {
                    logger.info("将CommitMessage [" + cmtm.getMsgId() + "] 存入数据库");
                } else {
                    logger.info("CommitMessage [" + cmtm.getMsgId() + "] 已存在");
                }

                logger.info("count = " + count);
                // 3. 达成 count >= 2 * f 后存入到集合中
                if (2 * PeerUtil.getFaultCount() <= count) {
                    CommittedMessage cmtdm = CommittedMessageService.genInstance(ppm.getBlockMsg().getMsgId(), ppm.getViewId(),
                            ppm.getSeqNum(), NetUtil.getRealIp(), localPort);
                    if (CommittedMessageService.save(cmtdm, cmtdmCollection)) {
                        logger.info("将 CommittedMessage [" + cmtdm.toString() + "] 存入数据库");
                        if(BlockService.saveBlock(ppm.getBlockMsg().getBlock(), blockChainCollection)) {
                            logger.info("区块 " + ppm.getBlockMsg().getBlock().getBlockId() + " 存入成功");
                        }
                    } else {
                        logger.info("CommittedMessage [" + cmtdm.getMsgId() + "] 已存在");
                    }
                }
            }
        }

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
