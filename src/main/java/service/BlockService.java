package service;

import entity.Block;
import entity.Transaction;

import java.util.List;

import static util.SignatureUtil.getSha256Hex;

/**
 * Created by chao on 2017/11/17.
 */
public class BlockService {
    /**
     * 根据如下参数算出当前区块的id，并构造Block对象
     * @param preBlockId
     * @param treeHash
     * @param timestamp
     * @param txCount
     * @param txList
     * @return
     */
    public Block genBlock(String preBlockId, String treeHash, String timestamp, int txCount, List<Transaction> txList) {
        String hash_content = preBlockId + treeHash + timestamp;
        String blockId = getSha256Hex(hash_content);
        return new Block(blockId, preBlockId, treeHash, timestamp, txCount, txList);
    }
}
