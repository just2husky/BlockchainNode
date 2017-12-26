package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.TransactionDao;
import entity.PrePrepareMessage;
import entity.Transaction;
import entity.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import static service.MessageService.updateSeqNum;
import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/18.
 */
public class TransactionMessageService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private TransactionDao txDao = TransactionDao.getInstance();
    private NetService netService = NetService.getInstance();

    private static class LazyHolder {
        private static final TransactionMessageService INSTANCE = new TransactionMessageService();
    }
    private TransactionMessageService (){}
    public static TransactionMessageService getInstance() {
        return TransactionMessageService.LazyHolder.INSTANCE;
    }

    /**
     * 接收到 ClientMessage 为 TransactionMessage 时进行的一系列处理
     * @param rcvMsg
     * @param localPort
     */
    public static void procTxMsg(String rcvMsg, int localPort) {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        TransactionMessage txMsg = null;
        try {
            txMsg = objectMapper.readValue(rcvMsg, TransactionMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 校验 Tx Msg
        if(txMsg != null && verify(txMsg)) {
            // 1. 将从客户端收到的 Tx Message 存入到集合中
            String txCollection = url + "." + Const.TXM;
            if(save(txMsg, txCollection)) {
                logger.info("Tx Message: " + txMsg.getMsgId() + " 存入成功");
            } else {
                logger.info("Tx Message: " + txMsg.getMsgId() + " 已存在");
            }

            // 2. 从集合中取出给当前 PrePrepareMessage 分配的序列号
            long seqNum = updateSeqNum(url + ".seqNum");
            // 3. 根据 Block Message 生成 PrePrepareMessage，存入到集合中
            String ppmCollection = url + "." + Const.PPM;

            PrePrepareMessage ppm = PrePrepareMessageService.genInstance(Long.toString(seqNum), txMsg);
            PrePrepareMessageService.save(ppm, ppmCollection);

            // 4. 主节点向其他备份节点广播 PrePrepareMessage
            try {
                NetService.broadcastMsg(NetUtil.getRealIp(), localPort, objectMapper.writeValueAsString(ppm));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(txMsg != null){
            logger.error("Tx Message: " + txMsg.getMsgId() + " 未通过校验");
        } else {
            logger.error("Tx Message 为空");
        }
    }

    /**
     * 根据 transaction 生成 message
     * @param txList
     * @return
     */
    public TransactionMessage genInstance(List<Transaction> txList) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(txList, timestamp, pubKey));
        String msgId = getSha256Base64(signature);
        return new TransactionMessage(msgId, timestamp, pubKey, signature, txList);
    }

    /**
     * 从 tx list 中获取 tx，生成 TransactionMessage
     * @param queueName
     * @param limitTime
     * @param limitSize
     * @return
     */
    public TransactionMessage genInstance(String queueName, double limitTime, double limitSize) {
        RabbitmqUtil rmq = new RabbitmqUtil(queueName);
        List<Transaction> txList = txDao.pull(queueName, limitTime, limitSize);
        if (txList.size() > 0) {
            return this.genInstance(txList);
        } else {
            return null;
        }
    }

    /**
     * 将 TransactionMessage 对象存入集合 collectionName 中。
     *
     * @param txMsg
     * @param collectionName
     * @return
     */
    public static boolean save(TransactionMessage txMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", txMsg.getMsgId(), collectionName)) {
            logger.info("txMsg 消息 [" + txMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(txMsg.toString(), collectionName);
            return true;
        }
    }

    /**
     * 将 TransactionMessage json 字符串存入集合 collectionName 中。
     *
     * @param blockMsgStr
     * @param collectionName
     * @return
     */
    public static boolean save(String blockMsgStr, String collectionName) {
        TransactionMessage txMsg = null;
        try {
            txMsg = objectMapper.readValue(blockMsgStr, TransactionMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(txMsg, collectionName);
    }

    /**
     * 进行签名和验证的 content
     * @param txList
     * @param timestamp
     * @param pubKey
     * @return
     */
    public static String getSignContent(List<Transaction> txList, String timestamp, String pubKey) {
        String msgType = Const.TXM;
        return txList.toString() +msgType + timestamp + pubKey;
    }

    /**
     * 校验数字签名
     * @param txMsg
     * @return
     */
    public static boolean verify(TransactionMessage txMsg) {
        return SignatureUtil.verify(txMsg.getPubKey(), getSignContent(txMsg.getTxList(), txMsg.getTimestamp(),
                txMsg.getPubKey()), txMsg.getSignature());
    }
}
