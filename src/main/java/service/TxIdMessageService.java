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
                List<String> txIdList = tim.getTxIdList();
                synchronized (this) {
                    if (!txSrv.allExited(txIdList, txIdCollection)) {
                        if (txIdSrv.upSertBatch(txIdSrv.genInstances(tim), txIdCollection)) {
                            logger.info("保存 tx id list 成功");
                        }
                    } else {
                        logger.debug("txId: " + txIdList + " 已存在集合 " + txIdCollection + " 中");
                    }
                }
            } else {
                logger.error("TxIdMessage: " + tim.getMsgId() + "已存在");
            }
        } else {
            logger.error("TxIdMessage: " + tim.getMsgId() + "校验失败");
        }
    }

    public TxIdMessage genInstance(List<String> txIdList, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        String treeHash = new MerkleTree(txIdList).getRoot();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(txIdList, timestamp, treeHash, ip, port));
        String msgId = getSha256Base64(signature);
        return new TxIdMessage(msgId, timestamp, pubKey, signature, txIdList, treeHash, ip, port);
    }

    private String getSignContent(List<String> txId, String timestamp, String treeHash, String ip, int port) {
        return txId + timestamp + ip + port;
    }

    public boolean verify(TxIdMessage tim) {
        return SignatureUtil.verify(tim.getPubKey(), getSignContent(tim.getTxIdList(), tim.getTimestamp(), tim.getTreeHash(),
                tim.getIp(), tim.getPort()), tim.getSignature());
    }

    public boolean save(TxIdMessage tim, String collectionName) {
        return timDao.upSert(tim, collectionName);
    }

    /**
     * 统计 txIdMsg 在集合 collectionName 中的个数
     *
     * @param treeHash
     * @return
     */
    public int count(String treeHash, String collectionName) {
        List<String> list = timDao.findByTreeHash(treeHash, collectionName);
        // TODO 需要校验 TxIdMsg 是否是同一个节点发送的
        return list.size();
    }

}
