package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.LastBlockIdMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SignatureUtil;
import util.TimeUtil;

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

    private static class LazyHolder {
        private static final LastBlockIdMessageService INSTANCE = new LastBlockIdMessageService();
    }
    private LastBlockIdMessageService (){}
    public static LastBlockIdMessageService getInstance() {
        return LazyHolder.INSTANCE;
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
}
