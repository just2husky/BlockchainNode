package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.Const;

import java.util.List;

/**
 * Created by chao on 2017/12/25.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TxIdMessage extends Message {
    private List<String> txIdList;
    private String treeHash;
    private String ip;
    private int port;

    public TxIdMessage() {
    }

    public TxIdMessage(String msgId, String timestamp, String pubKey, String signature,
                       List<String> txIdList, String treeHash, String ip, int port) {
        super(msgId, Const.TIM, timestamp, pubKey, signature);
        this.txIdList = txIdList;
        this.treeHash = treeHash;
        this.ip = ip;
        this.port = port;
    }


    public TxIdMessage(String msgId, String msgType, String timestamp, String pubKey, String signature,
                       List<String> txIdList, String treeHash, String ip, int port) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.txIdList = txIdList;
        this.treeHash = treeHash;
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

    public List<String> getTxIdList() {
        return txIdList;
    }

    public void setTxIdList(List<String> txIdList) {
        this.txIdList = txIdList;
    }

    public String getTreeHash() {
        return treeHash;
    }

    public void setTreeHash(String treeHash) {
        this.treeHash = treeHash;
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
