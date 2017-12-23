package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.Const;

/**
 * Created by chao on 2017/11/11.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "msgType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrePrepareMessage.class, name = Const.PPM),
        @JsonSubTypes.Type(value = PrepareMessage.class, name = Const.PM),
        @JsonSubTypes.Type(value = PreparedMessage.class, name = Const.PDM),
        @JsonSubTypes.Type(value = CommitMessage.class, name = Const.CMTM),
        @JsonSubTypes.Type(value = CommittedMessage.class, name = Const.CMTDM),
        @JsonSubTypes.Type(value = LastBlockIdMessage.class, name = Const.LBIM),
        @JsonSubTypes.Type(value = ClientMessage.class, name = Const.CM)})
public class Message {
    private String msgId;
    private String msgType;
    private String timestamp;
    private String pubKey;
    private String signature;

    public Message() {
    }

    public Message(String msgId, String msgType, String timestamp, String pubKey, String signature) {
        this.msgId = msgId;
        this.msgType = msgType;
        this.timestamp = timestamp;
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

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
