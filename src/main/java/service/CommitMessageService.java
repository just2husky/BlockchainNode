package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.IOException;
import java.security.PrivateKey;

import static util.SignatureUtil.*;

/**
 * Created by chao on 2017/11/21.
 */
public class CommitMessageService {
    private final static Logger logger = LoggerFactory.getLogger(CommitMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private TransactionService txService = TransactionService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    private CommittedMessageService cmtdmService = CommittedMessageService.getInstance();

    /**
     * 处理接收到的 commit message
     * @param rcvMsg
     * @param localPort
     * @throws IOException
     */
    @SuppressWarnings("Duplicates")
    public void procCMTM(String rcvMsg, int localPort) throws IOException {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        logger.info("本机地址为：" + url);

        // 1. 校验接收到的 CommitMessage
        CommitMessage cmtm = objectMapper.readValue(rcvMsg, CommitMessage.class);
        logger.info("接收到 CommitMsg：" + rcvMsg);
        logger.info("开始校验 CommitMsg ...");
        boolean verifyRes = CommitMessageService.verify(cmtm);
        logger.debug("校验结束，结果为：" + verifyRes);

        if(verifyRes) {
            String cmtmCollection = url + "." + Const.CMTM;
            String ppmCollection = url + "." + Const.PPM;

            PrePrepareMessage ppm = MongoUtil.findPPMById(SignatureUtil.getSha256Base64(cmtm.getPpmSign()), ppmCollection);
            if(ppm != null) {
                // 1. 统计 ppmSign 出现的次数
                int count = MongoUtil.countPPMSign(cmtm.getPpmSign(), cmtm.getViewId(), cmtm.getSeqNum(), cmtmCollection);
                logger.debug("count = " + count);

                // 2. 将 CommitMessage 存入到集合中
                if (CommitMessageService.save(cmtm, cmtmCollection)) {
                    logger.info("将CommitMessage [" + cmtm.getMsgId() + "] 存入数据库");
                } else {
                    logger.info("CommitMessage [" + cmtm.getMsgId() + "] 已存在");
                }

                // 3. 达成 count >= 2 * f 后存入到集合中
                if (2 * PeerUtil.getFaultCount() <= count) {
                    CommittedMessage cmtdm = cmtdmService.genInstance(ppm.getClientMsg().getMsgId(), ppm.getViewId(),
                            ppm.getSeqNum(), NetUtil.getRealIp(), localPort);
                    ClientMessage clientMessage = ppm.getClientMsg();
                    cmtdmService.procCMTDM(cmtdm, clientMessage, localPort);
                }
            }
        }

    }

    public static CommitMessage genCommitMsg(String ppmSign, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(ppmSign, viewId, seqNum, timestamp, ip, port));
        String msgId = getSha256Base64(signature);
        return new CommitMessage(msgId, timestamp, pubKey, signature, viewId, seqNum, ppmSign, ip, port);

    }

    /**
     * 生成签名的内容
     * @param ppmSign
     * @param viewId
     * @param seqNum
     * @param timestamp
     * @param ip
     * @param port
     * @return
     */
    public static String getSignContent(String ppmSign, String viewId, String seqNum, String timestamp, String ip, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append(ppmSign).append(viewId).append(seqNum).append(timestamp).append(ip).append(port);
        return sb.toString();
    }

    public static boolean save(CommitMessage cm, String collectionName){
        if(MongoUtil.findByKV("msgId", cm.getMsgId(), collectionName)) {
            logger.info("CommitMessage 消息 [" + cm.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(cm.toString(), collectionName);
            return true;
        }
    }

    public static boolean verify(CommitMessage cmtm) {
        return SignatureUtil.verify(cmtm.getPubKey(), getSignContent(cmtm.getPpmSign(), cmtm.getViewId(),
                cmtm.getSeqNum(), cmtm.getTimestamp(), cmtm.getIp(), cmtm.getPort()), cmtm.getSignature());
    }
}
