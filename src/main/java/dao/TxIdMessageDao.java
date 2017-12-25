package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import entity.TxIdMessage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;

import java.util.List;

/**
 * Created by chao on 2017/12/25.
 */
public class TxIdMessageDao {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(TxIdMessageDao.class);

    private static class LazyHolder {
        private static final TxIdMessageDao INSTANCE = new TxIdMessageDao();
    }

    private TxIdMessageDao() {
    }

    public static TxIdMessageDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 保存 TxIdMessage 到数据库中
     * @param tim
     * @param collectionName
     * @return
     */
    public boolean upSert(TxIdMessage tim, String collectionName) {
        MongoCollection<Document> collection = MongoUtil.getCollection(collectionName);
        // 如果集合不存在，则创建唯一索引
        if(!MongoUtil.collectionExists(collectionName)) {
            Document index = new Document("msgId", 0);
            collection.createIndex(index, new IndexOptions().unique(true));
        }

        Document document = Document.parse(tim.toString());
        Bson filter = Filters.eq("msgId", tim.getMsgId());
        Bson update = new Document("$set", document);
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult updateResult = null;
        try {
            updateResult = collection.updateOne(filter, update, options);
        } catch (com.mongodb.MongoWriteException e) {
            logger.error(e.getMessage());
        }

        return updateResult != null && updateResult.wasAcknowledged();
    }

    /**
     * 根据 txId 查找文档
     * @param txId
     * @param collectionName
     * @return
     */
    public List<String> findByTxId(String txId, String collectionName) {
        return MongoUtil.find("txId", txId, collectionName);
    }
}
