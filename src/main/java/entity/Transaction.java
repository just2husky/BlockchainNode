package entity;

import java.sql.Timestamp;

/**
 * Created by chao on 2017/11/11.
 */
public class Transaction {
    private String txId;
    private String signature;  //客户端给该交易单的签名, 为string类型
    private String tx_type; //tx_type 用以标志当前 Transaction 对象所存储的数据的类型，如 Patient、Doctor、Record等
    private String pub_key;  // 客户端该交易单签名的客户端的私钥所对应的公钥, 为string类型
    private String content;  // 该交易单实际存储的内容
    private String timestamp;  // 交易单生成时的时间

    public Transaction() {
    }

    public Transaction(String txId, String signature, String tx_type, String pub_key, String content, String timestamp) {
        this.txId = txId;
        this.signature = signature;
        this.tx_type = tx_type;
        this.pub_key = pub_key;
        this.content = content;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "txId='" + txId + '\'' +
                ", signature='" + signature + '\'' +
                ", tx_type='" + tx_type + '\'' +
                ", pub_key='" + pub_key + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTx_type() {
        return tx_type;
    }

    public void setTx_type(String tx_type) {
        this.tx_type = tx_type;
    }

    public String getPub_key() {
        return pub_key;
    }

    public void setPub_key(String pub_key) {
        this.pub_key = pub_key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}


