package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Block;
import entity.BlockMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.io.IOException;
import java.security.PrivateKey;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/10.
 */
public class BlockMessageService {
    private final static Logger logger = LoggerFactory.getLogger(BlockMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据 block 对象，生成 BlockMessage 对象
     * @param block
     * @return
     */
    public static BlockMessage genInstance(Block block) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, block.toString());
        String msgId = getSha256Base64(signature);
        return new BlockMessage(msgId, timestamp, pubKey, signature, block);
    }

    /**
     * 将 BlockMessage json 字符串存入集合 collectionName 中。
     * @param blockMsgStr
     * @param collectionName
     * @return
     */
    public static boolean save(String blockMsgStr, String collectionName){
        BlockMessage blockMsg = null;
        try {
            blockMsg = objectMapper.readValue(blockMsgStr, BlockMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return save(blockMsg, collectionName);
    }

    /**
     * 将 BlockMessage 对象存入集合 collectionName 中。
     * @param blockMsg
     * @param collectionName
     * @return
     */
    public static boolean save(BlockMessage blockMsg, String collectionName){
        if(MongoUtil.findByKV("msgId", blockMsg.getMsgId(), collectionName)) {
            logger.info("blockMsg 消息 [" + blockMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(blockMsg.toString(), collectionName);
            return true;
        }
    }
}
