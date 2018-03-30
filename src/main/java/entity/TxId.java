package entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2017/12/25.
 */
public class TxId {
    private String txId;
    // 是否被发送到 Blocker
    private boolean inBlock;

    public TxId() {
    }

    public TxId(String txId, boolean inBlock) {
        this.txId = txId;
        this.inBlock = inBlock;
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

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public boolean isInBlock() {
        return inBlock;
    }

    public void setInBlock(boolean inBlock) {
        this.inBlock = inBlock;
    }
}
