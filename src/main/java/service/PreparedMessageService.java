package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.PreparedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/12/11.
 */
@SuppressWarnings("Duplicates")
public class PreparedMessageService {
    private final static Logger logger = LoggerFactory.getLogger(PreparedMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static PreparedMessage genInstance(String cliMsgId, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(cliMsgId, viewId, seqNum, timestamp, ip, port));
        String msgId = getSha256Base64(signature);
        return new PreparedMessage(msgId, timestamp, pubKey, signature, cliMsgId, viewId, seqNum, ip, port);
    }

    public static boolean verify(PreparedMessage pdm) {
        if (!SignatureUtil.verify(pdm.getPubKey(), getSignContent(pdm.getCliMsgId(), pdm.getViewId(),
                pdm.getSeqNum(), pdm.getTimestamp(), pdm.getIp(), pdm.getPort()), pdm.getSignature())) {
            return false;
        }
        return true;
    }

    public static boolean save(PreparedMessage pdm, String collectionName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("viewId", pdm.getViewId());
        map.put("seqNum", pdm.getSeqNum());
        return MongoUtil.upSertJson(map, pdm.toString(), collectionName);
    }

    public static String getSignContent(String cliMsgId, String viewId, String seqNum, String timestamp, String ip, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append(cliMsgId).append(viewId).append(seqNum).append(timestamp).append(ip).append(port);
        return sb.toString();
    }
}
