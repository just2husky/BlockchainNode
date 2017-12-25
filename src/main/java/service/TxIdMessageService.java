package service;

import dao.TxIdMessageDao;
import entity.TxIdMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.security.PrivateKey;
import java.util.List;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/25.
 */
public class TxIdMessageService {
    private final static Logger logger = LoggerFactory.getLogger(TxIdMessageService.class);
    private TxIdMessageDao timDao = TxIdMessageDao.getInstance();
    private TransactionService txSrv = TransactionService.getInstance();
    private TxIdService txIdSrv = TxIdService.getInstance();

    private static class LazyHolder {
        private static final TxIdMessageService INSTANCE = new TxIdMessageService();
    }

    private TxIdMessageService() {
    }

    public static TxIdMessageService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void procTxIdMsg(TxIdMessage tim, String timCollection, String txIdCollection) {
        // 1. 校验接收到的 TxIdMessage
        boolean verifyRes = this.verify(tim);
        logger.debug("校验结束，结果为：" + verifyRes);
        if (verifyRes) {
            // 2. 保存接收到的 TxIdMessage
            if (this.save(tim, timCollection)) {
                // 满足接收到 2f + 1 个来自不同节点的 TxId消息
                String txId = tim.getTxId();
                if (2 * PeerUtil.getFaultCount() + 1 <= this.count(txId, timCollection)) {
                    synchronized (this) {
                        if (!txSrv.exited(txId, txIdCollection)) {
                            if (txIdSrv.upSert(txIdSrv.genInstance(tim), txIdCollection)) {
                                txIdSrv.addTxIdToQueue(txId);
                                logger.info("发送 tx id [" + txId + "] 到 " + Const.TX_ID_QUEUE);
                            }
                        } else {
                            logger.debug("txId: " + txId + " 已存在集合 " + txIdCollection + " 中");
                        }
                    }
                }

            } else {
                logger.error("TxIdMessage: " + tim.getMsgId() + "已存在");
            }
        } else {
            logger.error("TxIdMessage: " + tim.getMsgId() + "校验失败");
        }
    }

    public TxIdMessage genInstance(String txId, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(txId, timestamp, ip, port));
        String msgId = getSha256Base64(signature);
        return new TxIdMessage(msgId, timestamp, pubKey, signature, txId, ip, port);
    }

    private String getSignContent(String txId, String timestamp, String ip, int port) {
        return txId + timestamp + ip + port;
    }

    public boolean verify(TxIdMessage tim) {
        return SignatureUtil.verify(tim.getPubKey(), getSignContent(tim.getTxId(), tim.getTimestamp(),
                tim.getIp(), tim.getPort()), tim.getSignature());
    }

    public boolean save(TxIdMessage tim, String collectionName) {
        return timDao.upSert(tim, collectionName);
    }

    /**
     * 统计 txIdMsg 在集合 collectionName 中的个数
     *
     * @param txId
     * @return
     */
    public int count(String txId, String collectionName) {
        List<String> list = timDao.findByTxId(txId, collectionName);
        // TODO 需要校验 TxIdMsg 是否是同一个节点发送的
        return list.size();
    }

}
