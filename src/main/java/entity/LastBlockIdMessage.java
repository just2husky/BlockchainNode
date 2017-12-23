package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import util.Const;

/**
 * Created by chao on 2017/12/22.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LastBlockIdMessage extends Message {
    private String lastBlockId;
    private String preLastBlockId;
    private String ip;
    private int port;

    public LastBlockIdMessage() {
    }

    public LastBlockIdMessage(String msgId, String timestamp, String pubKey, String signature,
                              String lastBlockId, String preLastBlockId, String ip, int port) {
        super(msgId, Const.LAST_BLOCK_ID_MSG, timestamp, pubKey, signature);
        this.lastBlockId = lastBlockId;
        this.preLastBlockId = preLastBlockId;
        this.ip = ip;
        this.port = port;
    }

    public LastBlockIdMessage(String msgId, String msgType, String timestamp, String pubKey,
                              String signature, String lastBlockId, String preLastBlockId, String ip, int port) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.lastBlockId = lastBlockId;
        this.preLastBlockId = preLastBlockId;
        this.ip = ip;
        this.port = port;
    }

    public String getLastBlockId() {
        return lastBlockId;
    }

    public void setLastBlockId(String lastBlockId) {
        this.lastBlockId = lastBlockId;
    }

    public String getPreLastBlockId() {
        return preLastBlockId;
    }

    public void setPreLastBlockId(String preLastBlockId) {
        this.preLastBlockId = preLastBlockId;
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
