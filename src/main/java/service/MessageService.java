package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.*;

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
    private TransactionService txService = TransactionService.getInstance();
    private BlockService blockService = BlockService.getInstance();
    private CommittedMessageService cmtdmService = CommittedMessageService.getInstance();

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
    public static long getSeqNum(String collectionName) {
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
                logger.error("获取 seqNum 失败！");
                return -1;
            }
        }
    }

    /**
     * 到 collectionName 里去获取更新的序列号
     *
     * @param collectionName
     * @throws Exception
     */
    public static long updateSeqNum(String collectionName) {

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
    @SuppressWarnings("Duplicates")
    public void traversePPMAndSaveMsg(String ppmCollection, String traverseCollection, String saveCollection,
                                             String msgType, String ip, int port) {
        List<String> ppmList = new ArrayList<String>();
        while (true) {
            try {
                sleep(Const.SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.debug("开始遍历" + ppmCollection);
            ppmList = MongoUtil.findAll(ppmCollection);
            for (String ppmStr : ppmList) {
                PrePrepareMessage ppm = null;
                try {
                    ppm = objectMapper.readValue(ppmStr, PrePrepareMessage.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (ppm != null) {
                    logger.debug("开始统计 " + ppm.getSignature() + "在 " + traverseCollection + " 出现的次数");
                    // 1. 统计 ppmSign 出现的次数
                    int count = MongoUtil.countPPMSign(ppm.getSignature(), ppm.getViewId(), ppm.getSeqNum(), traverseCollection);
                    logger.debug(ppm.getSignature() + "在 " + traverseCollection + " 出现的次数为： " + count);

                    if (!MongoUtil.findByKV("cliMsgId", ppm.getClientMsg().getMsgId(), saveCollection)) {
                        if (2 * PeerUtil.getFaultCount() <= count) {
                            if (msgType.equals(Const.PDM)) {
                                logger.info("开始生成 PreparedMessage 并存入数据库");
                                PreparedMessage pdm = PreparedMessageService.genInstance(ppm.getClientMsg().getMsgId(), ppm.getViewId(),
                                        ppm.getSeqNum(), ip, port);
                                if (PreparedMessageService.save(pdm, saveCollection)) {
                                    logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存入数据库");
                                }
                            } else if (msgType.equals(Const.CMTDM)) {
                                logger.info("开始生成 CommittedMessage 并存入数据库");
                                CommittedMessage cmtdm = cmtdmService.genInstance(ppm.getClientMsg().getMsgId(),
                                        ppm.getViewId(), ppm.getSeqNum(), ip, port);
                                ClientMessage clientMsg = ppm.getClientMsg();
                                cmtdmService.procCMTDM(cmtdm, clientMsg, port);
                            }
                        }
                    } else {
                        logger.debug(msgType + "已存在，不需要存入");
                    }
                }
            }
        }
    }


}
