package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Block;
import entity.BlockMessage;
import entity.ClientMessage;
import entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.SignatureUtil;
import util.TimeUtil;

import java.security.PrivateKey;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/10.
 */
public class BlockMessageService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static BlockMessage genInstance(Block block) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, block.toString());
        String msgId = getSha256Base64(signature);
        return new BlockMessage(msgId, timestamp, pubKey, signature, block);
    }
}
