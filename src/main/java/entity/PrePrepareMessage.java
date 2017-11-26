package entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2017/11/24.
 */
public class PrePrepareMessage extends Message{
    String viewId;  // 当前视图编号
    String seqNum;  // sequence number， 该请求是在视图v中被赋予了序号n
    String txId;  // 预准备消息所要发送的消息内容，为当前所要提交的 Transaction 的内容
    String txIdHash;  // 预准备消息所要发送的消息内容的摘要，即哈希值

    public PrePrepareMessage() {
    }

    public PrePrepareMessage(String viewId, String seqNum, String txId, String txIdHash) {
        this.viewId = viewId;
        this.seqNum = seqNum;
        this.txId = txId;
        this.txIdHash = txIdHash;
    }

    public PrePrepareMessage(String msgId, String msgType, String timestamp, String pubKey, String signature, String viewId, String seqNum, String txId, String txIdHash) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.viewId = viewId;
        this.seqNum = seqNum;
        this.txId = txId;
        this.txIdHash = txIdHash;
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

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxIdHash() {
        return txIdHash;
    }

    public void setTxIdHash(String txIdHash) {
        this.txIdHash = txIdHash;
    }
}
