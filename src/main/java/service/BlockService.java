package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Block;
import entity.Transaction;
import util.*;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import static util.SignatureUtil.getSha256Base64;
import static util.SignatureUtil.loadPubKeyStr;
import static util.SignatureUtil.loadPvtKey;

/**
 * Created by chao on 2017/11/17.
 */
public class BlockService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 根据如下参数算出当前区块的id，并构造Block对象
     *
     * @param preBlockId
     * @param treeHash
     * @param timestamp
     * @param txCount
     * @param txList
     * @return
     */
    public static Block genBlock(String preBlockId, String treeHash, String timestamp, int txCount, List<Transaction> txList) {
        String sigContent = preBlockId + treeHash + timestamp;
        PrivateKey privateKey = loadPvtKey("EC");
        String pubKey = loadPubKeyStr("EC");
        String signature = SignatureUtil.sign(privateKey, sigContent);
        String blockId = getSha256Base64(signature);
        return new Block(blockId, preBlockId, treeHash, timestamp, txCount, txList, pubKey, signature);
    }

    /**
     * 根据 preBlockId, tx list 生成一个区块
     *
     * @param preBlockId
     * @param txList
     * @return
     */
    public static Block genBlock(String preBlockId, List<Transaction> txList) {
        List<String> txIdList = TransactionService.getTxList(txList);
        String treeHash = new MerkleTree(txIdList).getRoot();
        String timestamp = TimeUtil.getNowTimeStamp();
        int txCount = txList.size();
        return genBlock(preBlockId, treeHash, timestamp, txCount, txList);
    }

    /**
     * 从 RabbitMQ 队列中获取 tx list，生成区块
     * @param preBlockId
     * @param queueName tx list 所在队列的名字
     * @param limitTime 接收一个区块里 tx list 所等待的最长时间
     * @param limitSize 区块大小限制
     * @return
     */
    public static Block genBlock(String preBlockId, String queueName, double limitTime, double limitSize){
        RabbitmqUtil rmq = new RabbitmqUtil(queueName);
        List<String> txJsonList = rmq.pull(limitTime, limitSize);
        List<Transaction> txList = new ArrayList<Transaction>();
        Transaction tx;
        for (String txJson : txJsonList) {
            // 判断 json 是 tx 对象还是 tx list
            if(JsonUtil.isList(txJson)) {
                for (Transaction tmpTx : TransactionService.genTxList(txJson)) {
                    if (tmpTx != null) {
                        txList.add(tmpTx);
                    }
                }
            } else {
                tx = TransactionService.genTx(txJson);
                if (tx != null) {
                    txList.add(tx);
                }
            }
        }
        return genBlock(preBlockId, txList);
    }

    public static void main(String[] args) {
        List<Transaction> txList = new ArrayList<Transaction>();
        for (int i = 0; i < 10; i++) {
            try {
                txList.add(TransactionService.genTx("string", "测试" + i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Block block = genBlock("0", txList);
        try {
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(block));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
