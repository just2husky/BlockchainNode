package service;

import entity.*;
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
     *
     * @param msgType
     * @param transaction
     * @return
     */
    public static ClientSendMessage genTxMsg(String msgType, Transaction transaction) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, transaction.toString());
        String msgId = getSha256Base64(signature);
        return new ClientSendMessage(msgId, msgType, timestamp, pubKey, signature, transaction);
    }

    public static PrePrepareMessage genPrePrepareMsg(String msgType, String txId) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, txId);
        String msgId = getSha256Base64(signature);
        String txIdHash = getSha256Base64(txId);
        return new PrePrepareMessage(msgId, msgType, timestamp, pubKey, signature, "1", "1", txId, txIdHash);
    }

    /**
     * 检验 PrePrepareMessage的正确性
     *
     * @param ppm
     * @return
     */
    public static String verifyPrePrepareMsg(PrePrepareMessage ppm) {
        if (!ppm.getTxIdHash().equals(getSha256Base64(ppm.getTxId()))) {
            return "txIdHashError";
        } else if (!SignatureUtil.verify(ppm.getPubKey(), ppm.getTxId(), ppm.getSignature())) {
            return "sigError";
        }
        return "true";
    }

    public static PrepareMessage genPrepareMsg(String msgType, String ip, int port) {
        String url = ip + ":" + port;
        return new PrepareMessage(genMessage(msgType, url), "1", "1", ip, port);
    }

    public static Message genMessage(String msgType, String sigContent) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, sigContent);
        String msgId = getSha256Base64(signature);
        return new Message(msgId, msgType, timestamp, pubKey, signature);
    }
    public static void main(String[] args) {
        try {
            ClientSendMessage txMsg = genTxMsg("cliMsg", genTx("string", "测试"));
            logger.info(txMsg.toString());
            PrePrepareMessage ppm = genPrePrepareMsg("PrePrepare", txMsg.getTransaction().getTxId());
            logger.info(ppm.toString());
            logger.info(verifyPrePrepareMsg(ppm));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
