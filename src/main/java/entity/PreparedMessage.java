package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.Const;

/**
 * Created by chao on 2017/11/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PreparedMessage extends Message {
    private String cliMsgId;  // ClientMessage 的 id
    private String viewId;  // 当前视图编号
    private String seqNum;  // sequence number， 该请求是在视图v中被赋予了序号n
    private String ip;  //发送PrepareMessage的ip
    private int port;  // 发送PrepareMessage的端口

    public PreparedMessage() {
    }

    public PreparedMessage(String msgId, String timestamp, String pubKey, String signature,
                           String cliMsgId, String viewId, String seqNum, String ip, int port) {
        super(msgId, Const.PDM, timestamp, pubKey, signature);
        this.cliMsgId = cliMsgId;
        this.viewId = viewId;
        this.seqNum = seqNum;
        this.ip = ip;
        this.port = port;
    }

    public PreparedMessage(String msgId, String msgType, String timestamp, String pubKey, String signature,
                           String cliMsgId, String viewId, String seqNum, String ip, int port) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.cliMsgId = cliMsgId;
        this.viewId = viewId;
        this.seqNum = seqNum;
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

    public String getCliMsgId() {
        return cliMsgId;
    }

    public void setCliMsgId(String cliMsgId) {
        this.cliMsgId = cliMsgId;
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
