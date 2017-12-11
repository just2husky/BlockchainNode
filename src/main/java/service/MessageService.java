package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.Thread.sleep;
import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/11/21.
 */
public class MessageService {
    private final static Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据 msgType 和 transaction 生成 message
     *
     * @param msgType
     * @param transaction
     * @return
     */
    public static ClientMessage genTxMsg(String msgType, Transaction transaction) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, transaction.toString());
        String msgId = getSha256Base64(signature);
        return new ClientMessage(msgId, msgType, timestamp, pubKey, signature, transaction);
    }

    /**
     * 将 ClientMessage 对象存入集合 collectionName 中。
     *
     * @param cliMsg
     * @param collectionName
     * @return
     */
    public static boolean saveCliMsg(ClientMessage cliMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", cliMsg.getMsgId(), collectionName)) {
            logger.info("cliMsg 消息 [" + cliMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(cliMsg.toString(), collectionName);
            return true;
        }
    }

    /**
     * 将 ClientMessage json 字符串存入集合 collectionName 中。
     *
     * @param cliMsgStr
     * @param collectionName
     * @return
     */
    public static boolean saveCliMsg(String cliMsgStr, String collectionName) {
        ClientMessage cliMsg = null;
        try {
            cliMsg = objectMapper.readValue(cliMsgStr, ClientMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saveCliMsg(cliMsg, collectionName);
    }

    public static boolean savePMsg(PrepareMessage pMsg, String collectionName) {
        if (MongoUtil.findByKV("msgId", pMsg.getMsgId(), collectionName)) {
            logger.info("pMsg 消息 [" + pMsg.getMsgId() + "] 已存在");
            return false;
        } else {
            MongoUtil.insertJson(pMsg.toString(), collectionName);
            return true;
        }
//        return false;
    }

    public static boolean savePMsg(String pMsgStr, String collectionName) {
        ClientMessage cliMsg = null;
        try {
            cliMsg = objectMapper.readValue(pMsgStr, ClientMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saveCliMsg(cliMsg, collectionName);
    }

    /**
     * 根据传入的内容生成 pm 要签名的字符串
     *
     * @param ppmSign   ppm消息中的数字签名，相当于消息 m 的 digest d.
     * @param viewId
     * @param seqNum
     * @param timestamp
     * @return
     */
    public static String getPMSignContent(String ppmSign, String viewId, String seqNum, String timestamp, String ip, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append(ppmSign).append(viewId).append(seqNum).append(timestamp).append(ip).append(port);
        return sb.toString();
    }

    /**
     * 根据相关字段生成 PrepareMessage
     *
     * @param ppmSign ppm消息中的数字签名，相当于消息 m 的 digest d.
     * @param viewId
     * @param seqNum
     * @param ip
     * @param port
     * @return
     */
    public static PrepareMessage genPrepareMsg(String ppmSign, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getPMSignContent(ppmSign, viewId, seqNum, timestamp, ip, port));
        String msgId = getSha256Base64(signature);
        return new PrepareMessage(msgId, timestamp, pubKey, signature, viewId, seqNum, ppmSign, ip, port);

    }

    public static boolean verifyPrepareMsg(PrepareMessage pm) {
        if (!SignatureUtil.verify(pm.getPubKey(), getPMSignContent(pm.getPpmSign(), pm.getViewId(),
                pm.getSeqNum(), pm.getTimestamp(), pm.getIp(), pm.getPort()), pm.getSignature())) {
            return false;
        }
        return true;
    }

    /**
     * 生成 Message 类的对象
     *
     * @param msgType
     * @param sigContent
     * @return
     */
    public static Message genMessage(String msgType, String sigContent) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, sigContent);
        String msgId = getSha256Base64(signature);
        return new Message(msgId, msgType, timestamp, pubKey, signature);
    }

    /**
     * 到 collectionName 里去获取当前的序列号
     *
     * @param collectionName
     * @return
     * @throws Exception
     */
    public static long getSeqNum(String collectionName) throws Exception {
        if (!MongoUtil.collectionExists(collectionName)) {
            logger.info("集合" + collectionName + "不存在，开始创建");
            MongoUtil.insertKV("seqNum", "0", collectionName);
            return 0;
        } else {
            String record = MongoUtil.findFirstDoc(collectionName);
            if (record != null && !record.equals("")) {
                long seqNum = -1;
                try {
                    seqNum = Long.parseLong((String) objectMapper.readValue(record, Map.class).get("seqNum"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return seqNum;

            } else {
                throw new Exception("获取 seqNum 失败！");
            }
        }
    }

    /**
     * 到 collectionName 里去获取更新的序列号
     *
     * @param collectionName
     * @throws Exception
     */
    public static long updateSeqNum(String collectionName) throws Exception {

        long oldSeqNum = getSeqNum(collectionName);
        long newSeqNum = oldSeqNum + 1;
        MongoUtil.updateKV("seqNum", Long.toString(oldSeqNum), Long.toString(newSeqNum), collectionName);
        return newSeqNum;
    }

    /**
     * 每隔 Const.SLEEP_TIME 时间便查看一下 ppmCollection，检查 PreparedMessage 或 Committed Message 是否生成
     *
     * @param ppmCollection
     * @param traverseCollection
     * @param saveCollection
     * @param msgType            要生成的 msg 的类型，Const.PDM， Const.CMTDM
     */
    public static void traversePPMAndSaveMsg(String ppmCollection, String traverseCollection, String saveCollection,
                                             String msgType, String ip, int port, String blockChainCollection) {
        Set<String> ppmSet = new HashSet<String>();
        while (true) {
            logger.info("开始遍历" + ppmCollection);
            ppmSet = MongoUtil.traverse(ppmCollection);
            for (String ppmStr : ppmSet) {
                PrePrepareMessage ppm = null;
                try {
                    ppm = objectMapper.readValue(ppmStr, PrePrepareMessage.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (ppm != null) {
                    logger.info("开始统计 " + ppm.getSignature() + "在 " + traverseCollection + " 出现的次数");
                    // 1. 统计 ppmSign 出现的次数
                    int count = MongoUtil.countPPMSign(ppm.getSignature(), ppm.getViewId(), ppm.getSeqNum(), traverseCollection);

                    logger.info(ppm.getSignature() + "在 " + traverseCollection + " 出现的次数为： " + count);
                    if (!MongoUtil.findByKV("cliMsgId", ppm.getBlockMsg().getMsgId(), saveCollection)) {
                        if (2 * PeerUtil.getFaultCount() <= count) {
                            if (msgType.equals(Const.PDM)) {
//                                PrepareMessage pm = (PrepareMessage) MongoUtil.findPM(ppm.getSignature(), ppm.getViewId(),
//                                        ppm.getSeqNum(), traverseCollection, msgType);
                                logger.info("开始生成 PreparedMessage 并存入数据库");
                                PreparedMessage pdm = PreparedMessageService.genInstance(ppm.getBlockMsg().getMsgId(), ppm.getViewId(),
                                        ppm.getSeqNum(), ip, port);
                                if (PreparedMessageService.save(pdm, saveCollection)) {
                                    logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存入数据库");
                                }
                            } else if (msgType.equals(Const.CMTDM)) {
//                                CommittedMessage cmtMsg = (CommittedMessage) MongoUtil.findPM(ppm.getSignature(), ppm.getViewId(),
//                                        ppm.getSeqNum(), traverseCollection, msgType);
                                logger.info("开始生成 CommittedMessage 并存入数据库");
                                CommittedMessage cmtdm = CommittedMessageService.genInstance(ppm.getBlockMsg().getMsgId(),
                                        ppm.getViewId(), ppm.getSeqNum(), ip, port);
                                if (CommittedMessageService.save(cmtdm, saveCollection)) {
                                    logger.info("CommittedMessage [" + cmtdm.getMsgId() + "] 已存入数据库");
                                    if (MessageService.saveBlock(ppm.getBlockMsg().getBlock(), blockChainCollection)) {
                                        logger.info("区块 " + ppm.getBlockMsg().getBlock().getBlockId() + " 存入成功");
                                    }
                                }
                            }
                        }
                    } else {
                        logger.info(msgType + "已存在，不需要存入");
                    }
                }
            }

            try {
                sleep(Const.SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将区块 block 保存到集合 blockChainCollection 中
     *
     * @param block
     * @param blockChainCollection
     * @return
     */
    public static boolean saveBlock(Block block, String blockChainCollection) {
        String blockId = block.getBlockId();
        logger.info("开始保存区块：" + blockId);
        return MongoUtil.upSertJson("blockId", block.getBlockId(), block.toString(), blockChainCollection);
    }
}
