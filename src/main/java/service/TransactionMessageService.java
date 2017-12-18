package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Transaction;
import entity.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.io.IOException;
import java.security.PrivateKey;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/18.
 */
public class TransactionMessageService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据 msgType 和 transaction 生成 message
     * @param transaction
     * @return
     */
    public static TransactionMessage genInstance(Transaction transaction) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(transaction, timestamp, pubKey));
        String msgId = getSha256Base64(signature);
        return new TransactionMessage(msgId, timestamp, pubKey, signature, transaction);
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
     * @param cliMsgStr
     * @param collectionName
     * @return
     */
    public static boolean save(String cliMsgStr, String collectionName) {
        TransactionMessage txMsg = null;
        try {
            txMsg = objectMapper.readValue(cliMsgStr, TransactionMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(txMsg, collectionName);
    }

    /**
     * 进行签名和验证的 content
     * @param transaction
     * @param timestamp
     * @param pubKey
     * @return
     */
    public static String getSignContent(Transaction transaction, String timestamp, String pubKey) {
        String msgType = Const.TXM;
        return transaction.toString() +msgType + timestamp + pubKey;
    }

    /**
     * 校验数字签名
     * @param txMsg
     * @return
     */
    public static boolean verify(TransactionMessage txMsg) {
        return SignatureUtil.verify(txMsg.getPubKey(), getSignContent(txMsg.getTransaction(), txMsg.getTimestamp(),
                txMsg.getPubKey()), txMsg.getSignature());
    }
}
