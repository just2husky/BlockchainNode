package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.security.PrivateKey;

import static util.SignatureUtil.*;

/**
 * Created by chao on 2017/11/21.
 */
public class CommitMessageService {
    private final static Logger logger = LoggerFactory.getLogger(CommitMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

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
