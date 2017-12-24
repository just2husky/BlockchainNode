package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import entity.Block;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;

/**
 * Created by chao on 2017/12/19.
 */
public class BlockDao {
    private final static Logger logger = LoggerFactory.getLogger(BlockDao.class);

    private static class LazyHolder {
        private static final BlockDao INSTANCE = new BlockDao();
    }
    private BlockDao (){}
    public static BlockDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 保存 block 到数据库中
     * @param block
     * @param collectionName
     * @return
     */
    public boolean upSert(Block block, String collectionName) {
        MongoCollection<Document> collection = MongoUtil.getCollection(collectionName);
        // 如果集合不存在，则创建唯一索引
        if(!MongoUtil.collectionExists(collectionName)) {
            Document index = new Document("blockId", 0);
            collection.createIndex(index, new IndexOptions().unique(true));
        }

        Document document = Document.parse(block.toString());
        Bson filter = Filters.eq("blockId", block.getBlockId());
        Bson update = new Document("$set", document);
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult updateResult = null;
        try {
            updateResult = collection.updateOne(filter, update, options);
        } catch (com.mongodb.MongoWriteException e) {
            logger.error(e.getMessage());
        }

        return updateResult.wasAcknowledged();
    }
}
