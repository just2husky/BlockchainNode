package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.BlockMessage;
import entity.PrePrepareMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.io.IOException;
import java.security.PrivateKey;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/10.
 */
public class PrePrepareMessageService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("Duplicates")
    public static boolean save(PrePrepareMessage ppMsg, String collectionName){
        if(MongoUtil.findByKV("msgId", ppMsg.getMsgId(), collectionName)) {
            logger.info("ppMsg 消息 [" + ppMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(ppMsg.toString(), collectionName);
            return true;
        }
    }

    public static boolean save(String ppMsgStr, String collectionName){
        PrePrepareMessage ppMsg = null;
        try {
            ppMsg = objectMapper.readValue(ppMsgStr, PrePrepareMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(ppMsg, collectionName);
    }

    /**
     * 根据 序列号 与 block 对象生成预准备消息对象
     * @param seqNum
     * @param blockMsg
     * @return
     */
    public static PrePrepareMessage genInstance(String seqNum, BlockMessage blockMsg) {
        String timestamp = TimeUtil.getNowTimeStamp();
        // TODO
        String viewId = "1";

        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(blockMsg.getMsgId(), viewId, seqNum, timestamp));
        String msgId = getSha256Base64(signature);
        return new PrePrepareMessage(msgId, timestamp, pubKey, signature, viewId, seqNum, blockMsg);
    }

    /**
     * 检验 PrePrepareMessage的正确性
     *
     * @param ppm
     * @return
     */
    public static boolean verify(PrePrepareMessage ppm) {
        if (!SignatureUtil.verify(ppm.getPubKey(), getSignContent(ppm.getBlockMsg().getMsgId(), ppm.getViewId(),
                ppm.getSeqNum(), ppm.getTimestamp()), ppm.getSignature())) {
            return false;
        }
        return true;
    }

    /**
     * 根据传入的内容生成 ppm 要签名的字符串
     * @param blockMsgId
     * @param viewId
     * @param seqNum
     * @param timestamp
     * @return
     */
    public static String getSignContent(String blockMsgId, String viewId, String seqNum, String timestamp) {
        StringBuilder sb = new StringBuilder();
        sb.append(blockMsgId).append(viewId).append(seqNum).append(timestamp);
        return sb.toString();
    }
}
