package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.PrepareMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.io.IOException;
import java.security.PrivateKey;

import static util.SignatureUtil.*;

/**
 * Created by chao on 2017/12/18.
 */
public class PrepareMessageService {
    private final static Logger logger = LoggerFactory.getLogger(PrepareMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据相关字段生成 PrepareMessage
     *
     * @param ppmSign ppm消息中的数字签名，相当于消息 m 的 digest d.
     * @param viewId
     * @param seqNum
     * @param ip
     * @param port
     * @return
     */
    public static PrepareMessage genInstance(String ppmSign, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(timestamp, pubKey, viewId, seqNum, ppmSign, ip, port));
        String msgId = getSha256Base64(signature);
        return new PrepareMessage(msgId, timestamp, pubKey, signature, viewId, seqNum, ppmSign, ip, port);

    }

    public static boolean verify(PrepareMessage pm) {
        if (!SignatureUtil.verify(pm.getPubKey(), getSignContent(pm.getTimestamp(), pm.getPubKey(), pm.getViewId(),
                pm.getSeqNum(), pm.getPpmSign(), pm.getIp(), pm.getPort()), pm.getSignature())) {
            return false;
        }
        return true;
    }

    /**
     * 根据传入的内容生成 pm 要签名的字符串
     *
     * @param ppmSign   ppm消息中的数字签名，相当于消息 m 的 digest d.
     * @param viewId
     * @param seqNum
     * @param timestamp
     * @return
     */
    public static String getSignContent(String timestamp, String pubKey, String viewId, String seqNum, String ppmSign, String ip, int port) {
        return timestamp + pubKey + viewId + seqNum + ppmSign + ip + port;
    }

    public static boolean save(PrepareMessage pMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", pMsg.getMsgId(), collectionName)) {
            logger.info("pMsg 消息 [" + pMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(pMsg.toString(), collectionName);
            return true;
        }
    }

    public static boolean save(String pMsgStr, String collectionName) {
        PrepareMessage pMsg = null;
        try {
            pMsg = objectMapper.readValue(pMsgStr, PrepareMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(pMsg, collectionName);
    }
}
