package service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import entity.LastBlockIdMessage;
import entity.SimpleLastBlock;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MongoUtil;

/**
 * Created by chao on 2017/12/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleLastBlockService {
    private final static Logger logger = LoggerFactory.getLogger(BlockService.class);

    private static class LazyHolder {
        private static final SimpleLastBlockService INSTANCE = new SimpleLastBlockService();
    }

    private SimpleLastBlockService() {
    }

    public static SimpleLastBlockService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public SimpleLastBlock genInstance(LastBlockIdMessage lbiMsg) {
        return new SimpleLastBlock(lbiMsg.getLastBlockId(), lbiMsg.getPreLastBlockId());
    }

    public void save(SimpleLastBlock lastBlock, String collectionName) {
        MongoUtil.insertJson(lastBlock.toString(), collectionName);
        logger.debug("保存" + lastBlock + "成功");
    }


    @SuppressWarnings("Duplicates")
    public boolean upSert(SimpleLastBlock block, String collectionName) {
        MongoCollection<Document> collection = MongoUtil.getCollection(collectionName);
        // 如果集合不存在，则创建唯一索引
        if (!MongoUtil.collectionExists(collectionName)) {
            Document index = new Document("blockId", 0);
            collection.createIndex(index, new IndexOptions().unique(true));
        }

        Document document = Document.parse(block.toString());
        Bson filter = Filters.eq("blockId", block.getBlockId());
        Bson update = new Document("$set", document);
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult updateResult = collection.updateOne(filter, update, options);
        return updateResult.wasAcknowledged();
    }

    public boolean findByBlockId(String blockId, String collectionName) {
        return MongoUtil.findByKV("blockId", blockId, collectionName);
    }
}
