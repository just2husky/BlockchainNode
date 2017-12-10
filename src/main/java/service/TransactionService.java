package service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsonUtil;
import util.SignatureUtil;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;
import static util.TimeUtil.getNowTimeStamp;


/**
 * Created by chao on 2017/11/17.
 */
public class TransactionService {
    private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 根据 Transaction 的类型，和要存储在 Transaction 中的 content 来生成一个 Transaction 对象
     * @param txType
     * @param content
     * @return
     * @throws Exception
     */
    public static Transaction genTx(String txType, String content) throws Exception {
        if (content == null) {
            logger.error("content内容为null");
            throw new Exception("content内容为null");
        }

        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String timestamp = getNowTimeStamp();
        String sigContent = txType + content + timestamp;
        String signature = SignatureUtil.sign(privateKey, sigContent);
        String txId = getSha256Base64(signature);

        return new Transaction(txId, signature, txType, pubKey, content, timestamp);
    }

    /**
     * 根据 Transaction json 字符串生成 Transaction 对象
     * @param txJson
     * @return
     */
    public static Transaction genTx(String txJson) {
        try {
            return objectMapper.readValue(txJson, Transaction.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析 tx json list
     * @param txListJson
     * @return
     */
    public static List<Transaction> genTxList(String txListJson) {
        List<Transaction> txList = new ArrayList<Transaction>();
        try {
            JavaType javaType = JsonUtil.getCollectionType(ArrayList.class, Transaction.class);
            txList = objectMapper.readValue(txListJson, javaType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txList;
    }


    /**
     * 获取 Transaction List 的所有 id
     * @param list
     * @return
     */
    public static List<String> getTxList(List<Transaction> list) {
        List<String> txIdList = new ArrayList<String>();
        for(Transaction tx : list) {
            txIdList.add(tx.getTxId());
        }
        return txIdList;
    }


    public static void main(String[] args) throws Exception {
//        System.out.println(genTx("string", "测试"));
        String mapStr = "{\"age\":30}";
        genTx(mapStr);
    }
}
