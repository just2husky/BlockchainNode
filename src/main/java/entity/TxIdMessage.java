package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.Const;

/**
 * Created by chao on 2017/12/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TxIdMessage extends Message {
    private String txId;
    private String ip;
    private int port;

    public TxIdMessage() {
    }

    public TxIdMessage(String msgId, String timestamp, String pubKey, String signature, String txId, String ip, int port) {
        super(msgId, Const.TIM, timestamp, pubKey, signature);
        this.txId = txId;
        this.ip = ip;
        this.port = port;
    }

    public TxIdMessage(String msgId, String msgType, String timestamp, String pubKey, String signature, String txId, String ip, int port) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.txId = txId;
        this.ip = ip;
        this.port = port;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
