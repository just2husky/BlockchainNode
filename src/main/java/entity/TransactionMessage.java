package entity;

/**
 * Created by chao on 2017/11/11.
 */
public class TransactionMessage extends Message {
    private Transaction transaction;

    public TransactionMessage() {
    }

    public TransactionMessage(String msg_type, String timestamp, Transaction transaction) {
        super(msg_type, timestamp);
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return "TransactionMessage{" +
                "transaction=" + transaction +
                '}';
    }
}
