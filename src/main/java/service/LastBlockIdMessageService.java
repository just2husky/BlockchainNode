package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.LastBlockIdMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.security.PrivateKey;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/23.
 */
public class LastBlockIdMessageService {
    private final static Logger logger = LoggerFactory.getLogger(LastBlockIdMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private BlockService blockService = BlockService.getInstance();
    private LastBlockIdMessageService lbmService = LastBlockIdMessageService.getInstance();

    private static class LazyHolder {
        private static final LastBlockIdMessageService INSTANCE = new LastBlockIdMessageService();
    }
    private LastBlockIdMessageService (){}
    public static LastBlockIdMessageService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 处理接收到的 Last Block Id
     * @param lbiMsg
     * @param lbiCollection
     */
    public void procLastBlockIdMSg(LastBlockIdMessage lbiMsg, String lbiCollection, String lbiMsgCollection) {
        String lastBlocId = lbiMsg.getLastBlocId();
        // 1. 校验接收到的 lastBlocIdMessage
        boolean verifyRes = this.verify(lbiMsg);
        logger.debug("校验结束，结果为：" + verifyRes);
        if(verifyRes) {
            // 2. 保存接收到的 LastBlockIdMessage
            this.save(lbiMsg, lbiMsgCollection);
            // 3. 满足条件后，更新 LastBlockIdMessage
            if(blockService.updateLastBlockId(lastBlocId, lbiCollection)) {
                logger.info("成功更新 last block id 为：" + lastBlocId);
                // 4. 将 last block id 推送到消息队列上
                blockService.addLastBlockIdToQueue(lastBlocId);
            } else {
                logger.error("更新 last block id: " + lastBlocId + "失败");
            }
        } else {
            logger.error("LastBlockIdMessage: " + lbiMsg.getMsgId() + "校验失败");
        }
    }

    public LastBlockIdMessage genInstance(String lastBlockId) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(lastBlockId, timestamp));
        String msgId = getSha256Base64(signature);
        return new LastBlockIdMessage(msgId, timestamp, pubKey, signature, lastBlockId);
    }

    private String getSignContent(String lastBlockId, String timestamp) {
        return lastBlockId + timestamp;
    }

    public boolean verify(LastBlockIdMessage lbm) {
        return SignatureUtil.verify(lbm.getPubKey(), getSignContent(lbm.getLastBlocId(), lbm.getTimestamp()),
                lbm.getSignature());
    }

    /**
     * 保存 LastBlockIdMessage 到集合 collectionNam 中
     * @param lbm
     * @param collectionName
     */
    public void save(String lbm, String collectionName) {
        MongoUtil.insertJson(lbm, collectionName);
    }

    public void save(LastBlockIdMessage lbm, String collectionName) {
        MongoUtil.insertJson(lbm.toString(), collectionName);
    }
}
