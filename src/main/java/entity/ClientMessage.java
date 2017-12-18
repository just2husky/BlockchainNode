package entity;

/**
 * Created by chao on 2017/12/18.
 */
public class ClientMessage extends Message {
    public ClientMessage() {
    }

    public ClientMessage(String msgId, String msgType, String timestamp, String pubKey, String signature) {
        super(msgId, msgType, timestamp, pubKey, signature);
    }
}
