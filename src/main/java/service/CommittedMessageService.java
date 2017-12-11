package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.CommittedMessage;
import entity.CommittedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.SignatureUtil;
import util.TimeUtil;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;


/**
 * Created by chao on 2017/11/21.
 */
public class CommittedMessageService {
    private final static Logger logger = LoggerFactory.getLogger(CommittedMessageService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static CommittedMessage genInstance(String cliMsgId, String viewId, String seqNum, String ip, int port) {
        String timestamp = TimeUtil.getNowTimeStamp();
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, getSignContent(cliMsgId, viewId, seqNum, timestamp, ip, port));
        String msgId = getSha256Base64(signature);
        return new CommittedMessage(msgId, timestamp, pubKey, signature, cliMsgId, viewId, seqNum, ip, port);
    }

    private static String getSignContent(String cliMsgId, String viewId, String seqNum, String timestamp, String ip, int port) {
        return cliMsgId + viewId + seqNum + timestamp + ip + port;
    }

    public static boolean save(CommittedMessage cmtdm, String collectionName){
        Map<String, String> map = new HashMap<String, String>();
        map.put("viewId", cmtdm.getViewId());
        map.put("seqNum", cmtdm.getSeqNum());
        return MongoUtil.upSertJson(map, cmtdm.toString(), collectionName);
    }

    public static boolean verify(CommittedMessage cm) {
        return SignatureUtil.verify(cm.getPubKey(), getSignContent(cm.getCliMsgId(), cm.getViewId(),
                cm.getSeqNum(), cm.getTimestamp(), cm.getIp(), cm.getPort()), cm.getSignature());
    }

    public static void main(String[] args) {
        String cm1 = "{\"msgId\":\"BGT/af6pamjrgkbBdrq/e4oiUdkqUpMUi4Eanb69AyI=\",\"msgType\":\"CommittedMsg\",\"timestamp\":\"1512975185325\",\"pubKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKp7wRlLhTDte3wC4Sd7pj42fBCMtNmUQ8cRsSTkY6Pw+qPuytjn4455A6/p7xOHJO8RIO9TRrGLLAnFJGHoZqg==\",\"signature\":\"MEYCIQD3APVH3QK6uOLfj68g3cocZF4GQ4ETYGf/4e0sfrAn/QIhAPSVimBJB9NOtkLfEfMaSGjXZWO5x78AEr4I+hh6bDLa\",\"cliMsgId\":\"VhK1/i9rMcERHy1Ajk8QIYBvTTwGYeRn9A5zdsjb+bA=\",\"viewId\":\"1\",\"seqNum\":\"7\",\"ip\":\"202.115.53.57\",\"port\":8003}";
        String cm2 = "{\"msgId\":\"qKjlWKs3tvAbv4WJqcqmSr5FOgyMmpMvlg9nhc5QT/E=\",\"msgType\":\"CommittedMsg\",\"timestamp\":\"1512975185325\",\"pubKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKp7wRlLhTDte3wC4Sd7pj42fBCMtNmUQ8cRsSTkY6Pw+qPuytjn4455A6/p7xOHJO8RIO9TRrGLLAnFJGHoZqg==\",\"signature\":\"MEUCIQDe/ZguVvVtj0d+7xYBz0B8vENNDPI9aUXrqNfbzxOQ5QIgPj9JiHNgNLL2No36NGG7zM+LDbKGBotFwm5Dlv8mH+4=\",\"cliMsgId\":\"VhK1/i9rMcERHy1Ajk8QIYBvTTwGYeRn9A5zdsjb+bA=\",\"viewId\":\"1\",\"seqNum\":\"7\",\"ip\":\"202.115.53.57\",\"port\":8003}";
        try {
            CommittedMessage committedMessage1 = objectMapper.readValue(cm1, CommittedMessage.class);
            CommittedMessage committedMessage2 = objectMapper.readValue(cm2, CommittedMessage.class);
            System.out.println(verify(committedMessage1));
            System.out.println(verify(committedMessage2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
