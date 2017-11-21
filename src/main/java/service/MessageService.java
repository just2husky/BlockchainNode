package service;

import entity.Message;
import entity.Transaction;
import entity.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SignatureUtil;
import util.TimeUtil;

import java.security.PrivateKey;

import static service.TransactionService.genTx;
import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/11/21.
 */
public class MessageService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);

    /**
     * 根据 msgType 和 transaction 生成 message
     * @param msgType
     * @param transaction
     * @return
     */
    public static Message genMsg(String msgType, Transaction transaction) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, transaction.toString());
        String msgId = getSha256Base64(signature);
        return new TransactionMessage(msgId, msgType, timestamp, pubKey, signature, transaction);
    }

    public static void main(String[] args) {
        try {
            logger.info(genMsg("测试msgType", genTx("string", "测试")).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
