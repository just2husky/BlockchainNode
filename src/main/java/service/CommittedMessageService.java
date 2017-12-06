package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.CommittedMessage;
import entity.CommittedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.security.PrivateKey;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;


/**
 * Created by chao on 2017/11/21.
 */
public class CommittedMessageService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static CommittedMessage genInstance(String cliMsgId, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getCMTDMSignContent(cliMsgId, viewId, seqNum, timestamp, ip, port));
        String msgId = getSha256Base64(signature);
        return new CommittedMessage(msgId, timestamp, pubKey, signature, cliMsgId, viewId, seqNum, ip, port);
    }

    private static String getCMTDMSignContent(String cliMsgId, String viewId, String seqNum, String timestamp, String ip, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append(cliMsgId).append(viewId).append(seqNum).append(timestamp).append(ip).append(port);
        return sb.toString();
    }

    public static boolean save(CommittedMessage cmtdm, String collectionName){
        if(MongoUtil.findByKV("cliMsgId", cmtdm.getCliMsgId(), collectionName)) {
            logger.info("cmtdm 消息 [" + cmtdm.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(cmtdm.toString(), collectionName);
            return true;
        }
    }
}
