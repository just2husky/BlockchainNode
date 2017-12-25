package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by chao on 2017/11/17.
 */
@org.msgpack.annotation.Message
@JsonIgnoreProperties(ignoreUnknown = true)
public class Block {
    private String blockId;
    private String preBlockId;
    private String treeHash;
    private String timestamp;
    private int txCount;
    private List<String> txIdList;
    private String pubKey;
    private String signature;

    public Block() {
    }

    public Block(String blockId, String preBlockId, String treeHash, String timestamp, int txCount, List<String> txIdList) {
        this.blockId = blockId;
        this.preBlockId = preBlockId;
        this.treeHash = treeHash;
        this.timestamp = timestamp;
        this.txCount = txCount;
        this.txIdList = txIdList;
    }

    public Block(String blockId, String preBlockId, String treeHash, String timestamp, int txCount, List<String> txIdList, String pubKey, String signature) {
        this.blockId = blockId;
        this.preBlockId = preBlockId;
        this.treeHash = treeHash;
        this.timestamp = timestamp;
        this.txCount = txCount;
        this.txIdList = txIdList;
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

    public List<String> getTxIdList() {
        return txIdList;
    }

    public void setTxIdList(List<String> txIdList) {
        this.txIdList = txIdList;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
