package entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by chao on 2017/11/17.
 */
@org.msgpack.annotation.Message
public class Block {
    private String blockId;
    private String preBlockId;
    private String treeHash;
    private String timestamp;
    private int txCount;
    private List<Transaction> txList;
    private String pubKey;
    private String signature;

    public Block() {
    }

    public Block(String blockId, String preBlockId, String treeHash, String timestamp, int txCount, List<Transaction> txList) {
        this.blockId = blockId;
        this.preBlockId = preBlockId;
        this.treeHash = treeHash;
        this.timestamp = timestamp;
        this.txCount = txCount;
        this.txList = txList;
    }

    public Block(String blockId, String preBlockId, String treeHash, String timestamp, int txCount, List<Transaction> txList, String pubKey, String signature) {
        this.blockId = blockId;
        this.preBlockId = preBlockId;
        this.treeHash = treeHash;
        this.timestamp = timestamp;
        this.txCount = txCount;
        this.txList = txList;
        this.pubKey = pubKey;
        this.signature = signature;
    }

    @Override
    public String toString() {
        String rtn = null;
        try {
            rtn = (new ObjectMapper()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getPreBlockId() {
        return preBlockId;
    }

    public void setPreBlockId(String preBlockId) {
        this.preBlockId = preBlockId;
    }

    public String getTreeHash() {
        return treeHash;
    }

    public void setTreeHash(String treeHash) {
        this.treeHash = treeHash;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public List<Transaction> getTxList() {
        return txList;
    }

    public void setTxList(List<Transaction> txList) {
        this.txList = txList;
    }
}
