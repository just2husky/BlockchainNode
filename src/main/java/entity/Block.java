package entity;

import java.util.List;

/**
 * Created by chao on 2017/11/17.
 */
public class Block {
    private String blockId;
    private String preBlockId;
    private String treeHash;
    private String timestamp;
    private int txCount;
    private List<Transaction> txList;

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

    @Override
    public String toString() {
        return "Block{" +
                "blockId='" + blockId + '\'' +
                ", preBlockId='" + preBlockId + '\'' +
                ", treeHash='" + treeHash + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", txCount=" + txCount +
                ", txList=" + txList +
                '}';
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
