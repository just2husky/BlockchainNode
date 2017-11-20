package service;

import entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SignatureUtil;

import java.security.PrivateKey;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;
import static util.TimeUtil.getNowTimeStamp;


/**
 * Created by chao on 2017/11/17.
 */
public class TransactionService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);

    /**
     * 根据 Transaction 的类型，和要存储在 Transaction 中的 content 来生成一个 Transaction 对象
     * @param txType
     * @param content
     * @return
     * @throws Exception
     */
    public static Transaction genTx(String txType, String content) throws Exception {
        if (content == null) {
            logger.error("content内容为null");
            throw new Exception("content内容为null");
        }

        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String timestamp = getNowTimeStamp();
        String signature = SignatureUtil.sign(privateKey, content);
        String txId = getSha256Base64(signature);

        return new Transaction(txId, signature, txType, pubKey, content, timestamp);
    }


    public static void main(String[] args) throws Exception {
        System.out.println(genTx("string", "测试"));
    }
}
