package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;

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

    private TxIdService tis = TxIdService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    private BlockerService blockerService = BlockerService.getInstance();
    private NetService netService = NetService.getInstance();

    private static class LazyHolder {
        private static final BlockMessageService INSTANCE = new BlockMessageService();
    }
    private BlockMessageService (){}
    public static BlockMessageService getInstance() {
        return LazyHolder.INSTANCE;
    }

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
     * 处理从 Validator 发往 Blocker 的 BlockMessage
     * @param blockMsg
     * @param blockMsgCollection
     * @param txIdCollection
     * @param blockChainCollection
     * @param lbiCollection
     */
    public void procBlockerBlockMsg(BlockMessage blockMsg, NetAddress netAddr, String blockMsgCollection,
                                    String txIdCollection, String blockChainCollection, String lbiCollection) {

        // 1. 将从客户端收到的 Block Message 存入到集合中
        if(BlockMessageService.save(blockMsg, blockMsgCollection)) {
            logger.info("Block Message 存入成功");
            Block block = blockMsg.getBlock();
            String blockId = block.getBlockId();
            if (blockService.save(block, blockChainCollection)) {
                logger.info("区块 " + blockId + " 存入成功");
                if (blockService.updateLastBlockId(blockId, lbiCollection)) {
                    logger.info("Last block Id: " + blockId + " 更新成功");

                } else {
                    logger.error("Last block Id: " + blockId + " 更新失败");
                }
            }
        } else {
            logger.info("Block Message 已存在");
        }

        // 2. 根据 block 中的 TxId，将 txIdCollection 中的相应 TxId 的 InBLock 为设为 true
        for (String txId : blockMsg.getBlock().getTxIdList()) {
            tis.setTrue(txId, txIdCollection);
        }

        // 3. 检查是否学要自己生成区块
        long blockLength = MongoUtil.countRecords(blockChainCollection);
        if(blockerService.isCurrentBlocker(netAddr, blockLength)) {
            // 如果下一个区块由url为netAddr的block生成，则生成 block，并发送给主节点
            String lastBlockId = blockService.getLastBlockId(lbiCollection);
            Block block = blockService.genBlock(lastBlockId, txIdCollection, Const.TX_ID_LIST_SIZE);
            if(block != null)
                this.sendBlock(block, NetUtil.getPrimaryNode());
            else {
                logger.info("目前没有可以打包的TxId");
            }
        }

    }

    public void sendBlock(Block block, NetAddress netAddr) {
        logger.info("开始向 [" + netAddr.getIp() + ":" + netAddr.getPort() + "] 发送 block: " + block.getBlockId());
        BlockMessage blockMessage = BlockMessageService.genInstance(block);
        logger.info("blockMessage in send block: " + blockMessage);
        String rcvMsg = netService.sendMsg(blockMessage.toString(), netAddr.getIp(),
                netAddr.getPort(), Const.TIME_OUT);
        logger.info("服务器响应： " + rcvMsg);
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
