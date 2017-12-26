package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.Const;

import java.util.List;

/**
 * Created by chao on 2017/11/11.
 * client 向 Validator 发送的消息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionMessage extends ClientMessage {
    private List<Transaction> txList;

    public TransactionMessage() {
    }

    public TransactionMessage(String msgId, String timestamp, String pubKey, String signature, List<Transaction> txList) {
        super(msgId, Const.TXM, timestamp, pubKey, signature);
        this.txList = txList;
    }

    public TransactionMessage(String msgId, String msgType, String timestamp, String pubKey, String signature, List<Transaction> txList) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.txList = txList;
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

    public List<Transaction> getTxList() {
        return txList;
    }

    public void setTxList(List<Transaction> txList) {
        this.txList = txList;
    }
}
