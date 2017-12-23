package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import util.Const;

/**
 * Created by chao on 2017/12/22.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LastBlockIdMessage extends Message {
    private String lastBlocId;

    public LastBlockIdMessage() {
    }

    public LastBlockIdMessage(String msgId, String timestamp, String pubKey, String signature, String lastBlocId) {
        super(msgId, Const.LBIM, timestamp, pubKey, signature);
        this.lastBlocId = lastBlocId;
    }

    public LastBlockIdMessage(String msgId, String msgType, String timestamp, String pubKey, String signature, String lastBlocId) {
        super(msgId, msgType, timestamp, pubKey, signature);
        this.lastBlocId = lastBlocId;
    }

    public String getLastBlocId() {
        return lastBlocId;
    }

    public void setLastBlocId(String lastBlocId) {
        this.lastBlocId = lastBlocId;
    }
}
