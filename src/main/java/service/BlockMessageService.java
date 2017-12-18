package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Block;
import entity.BlockMessage;
import entity.PrePrepareMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.IOException;
import java.security.PrivateKey;

import static service.MessageService.updateSeqNum;
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
     * 处理客户端发送的消息
     * @param rcvMsg 接收到的消息
     * @param localPort 本机的端口
     * @throws IOException
     */
    public static void procBlockMsg(String rcvMsg, int localPort) throws Exception {
        String realIp = NetUtil.getRealIp();
        String url = realIp + ":" + localPort;
        logger.info("本机地址为：" + url);
        // 1. 将从客户端收到的 Block Message 存入到集合中
        String blockMsgCollection = url + "." + Const.BM;
        if(BlockMessageService.save(rcvMsg, blockMsgCollection)) {
            logger.info("Block Message 存入成功");
        } else {
            logger.info("Block Message 已存在");
        }

        // 2. 从集合中取出给当前 PrePrepareMessage 分配的序列号
        long seqNum = updateSeqNum(url + ".seqNum");

        // 3. 根据 Block Message 生成 PrePrepareMessage，存入到集合中
        String ppmCollection = url + "." + Const.PPM;
        BlockMessage blockMsg = objectMapper.readValue(rcvMsg, BlockMessage.class);
        PrePrepareMessage ppm = PrePrepareMessageService.genInstance(Long.toString(seqNum), blockMsg);
        PrePrepareMessageService.save(ppm, ppmCollection);

        // 4. 主节点向其他备份节点广播 PrePrepareMessage
        NetService.broadcastMsg(NetUtil.getRealIp(), localPort, objectMapper.writeValueAsString(ppm));
    }

    /**
     * 根据 block 对象，生成 BlockMessage 对象
     *
     * @param block
     * @return
     */
    public static BlockMessage genInstance(Block block) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(block, timestamp, pubKey));
        String msgId = getSha256Base64(signature);
        return new BlockMessage(msgId, timestamp, pubKey, signature, block);
    }

    /**
     * 将 BlockMessage json 字符串存入集合 collectionName 中。
     *
     * @param blockMsgStr
     * @param collectionName
     * @return
     */
    public static boolean save(String blockMsgStr, String collectionName) {
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
     *
     * @param blockMsg
     * @param collectionName
     * @return
     */
    public static boolean save(BlockMessage blockMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", blockMsg.getMsgId(), collectionName)) {
            logger.info("blockMsg 消息 [" + blockMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(blockMsg.toString(), collectionName);
            return true;
        }
    }

    /**
     * 进行签名和验证的 content
     *
     * @param block
     * @param timestamp
     * @param pubKey
     * @return
     */
    public static String getSignContent(Block block, String timestamp, String pubKey) {
        String msgType = Const.BM;
        return block.toString() + msgType + timestamp + pubKey;
    }

    /**
     * 校验数字签名
     *
     * @param blockMsg
     * @return
     */
    public static boolean verify(BlockMessage blockMsg) {
        return SignatureUtil.verify(blockMsg.getPubKey(), getSignContent(blockMsg.getBlock(), blockMsg.getTimestamp(),
                blockMsg.getPubKey()), blockMsg.getSignature());
    }
}
