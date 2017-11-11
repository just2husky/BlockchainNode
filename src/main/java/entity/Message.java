package entity;

/**
 * Created by chao on 2017/11/11.
 */
public class Message {
    private String msg_type;
    private String timestamp;

    public Message() {
    }

    public Message(String msg_type, String timestamp) {
        this.msg_type = msg_type;
        this.timestamp = timestamp;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
