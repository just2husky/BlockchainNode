package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.LastBlockIdMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private SimpleLastBlockService slbService = SimpleLastBlockService.getInstance();

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
    public void procLastBlockIdMSg(LastBlockIdMessage lbiMsg, String lbiCollection, String lbiMsgCollection,
                                   String simpleBlockCollection) {
        String lastBlockId = lbiMsg.getLastBlockId();
        // 1. 校验接收到的 lastBlockIdMessage
        boolean verifyRes = this.verify(lbiMsg);
        logger.debug("校验结束，结果为：" + verifyRes);
        if(verifyRes) {
            // 2. 保存接收到的 LastBlockIdMessage
            this.save(lbiMsg, lbiMsgCollection);
            // 满足接收到 2f + 1 个来自不同节点的 lastBLockId消息
            if(2 * PeerUtil.getFaultCount() + 1 <= this.count(lastBlockId, lbiMsgCollection)) {
                synchronized (this) {
                    if (!slbService.findByBlockId(lastBlockId, simpleBlockCollection)) {
                        slbService.upSert(slbService.genInstance(lbiMsg), simpleBlockCollection);
                        // 3. 满足条件后，更新 LastBlockIdMessage
                        if (blockService.updateLastBlockId(lastBlockId, lbiCollection)) {
                            logger.info("成功更新 last block id 为：" + lastBlockId);
                            // 4. 将 last block id 推送到消息队列上
                            blockService.addLastBlockIdToQueue(lastBlockId);

                        } else {
                            logger.error("更新 last block id: " + lastBlockId + "失败");
                        }
                    }
                }
            }
        } else {
            logger.error("LastBlockIdMessage: " + lbiMsg.getMsgId() + "校验失败");
        }
    }

    public LastBlockIdMessage genInstance(String lastBlockId, String preLastBlockId, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(lastBlockId, timestamp, preLastBlockId, ip, port));
        String msgId = getSha256Base64(signature);
        return new LastBlockIdMessage(msgId, timestamp, pubKey, signature, lastBlockId, preLastBlockId, ip, port);
    }

    private String getSignContent(String lastBlockId, String timestamp, String preLastBlockId, String ip, int port) {
        return lastBlockId + timestamp + preLastBlockId + ip + port;
    }

    public boolean verify(LastBlockIdMessage lbm) {
        return SignatureUtil.verify(lbm.getPubKey(), getSignContent(lbm.getLastBlockId(), lbm.getTimestamp(),
                lbm.getPreLastBlockId(), lbm.getIp(), lbm.getPort()),
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

    /**
     * 统计 lastBlockId 在数据库中的个数
     * @param lastBlockId
     * @return
     */
    public int count(String lastBlockId, String collectionName) {
        List<String> list = MongoUtil.find("lastBlockId", lastBlockId, collectionName);
        // TODO
//        List<LastBlockIdMessage> lbiMsgList = new ArrayList<LastBlockIdMessage>();
//        Iterator<String> it = list.iterator();
//        while (it.hasNext()) {
//
//        }
        return list.size();
    }
}