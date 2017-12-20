package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import entity.Transaction;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;
import util.RabbitmqUtil;

import java.io.IOException;

/**
 * Created by chao on 2017/12/19.
 */
public class TransactionDao {
    private final static Logger logger = LoggerFactory.getLogger(TransactionDao.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    private static class LazyHolder {
        private static final TransactionDao INSTANCE = new TransactionDao();
    }
    private TransactionDao (){}
    public static TransactionDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 向数据库插入 tx
     *
     * @param tx
     * @param collectionName
     * @return
     */
    public boolean upSert(Transaction tx, String collectionName) {
        MongoCollection<Document> collection = MongoUtil.getCollection(collectionName);
        // 如果集合不存在，则创建唯一索引
        if (!MongoUtil.collectionExists(collectionName)) {
            Document index = new Document("txId", 0);
            collection.createIndex(index, new IndexOptions().unique(true));
        }

        Document document = Document.parse(tx.toString());
        Bson filter = Filters.eq("txId", tx.getTxId());
        Bson update = new Document("$set", document);
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult updateResult = collection.updateOne(filter, update, options);
        return updateResult.wasAcknowledged();
    }

    /**
     * 从消息队列 queueName 中获取 Transaction，若失败则返回 null
     * @param queueName
     * @return
     */
    public Transaction pull(String queueName) {
        RabbitmqUtil rmq = new RabbitmqUtil(queueName);
        String txStr = rmq.pull();
        if (txStr != null) {
            try {
                return objectMapper.readValue(txStr, Transaction.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将 tx push 到 消息队列 queueName 上
     * @param tx
     * @param queueName
     */
    public void push(Transaction tx, String queueName) {
        RabbitmqUtil rmq = new RabbitmqUtil(queueName);
        rmq.push(tx.toString());
    }
}
