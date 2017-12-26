package service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import entity.TxId;
import entity.TxIdMessage;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.MongoUtil;
import util.RabbitmqUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao on 2017/12/25.
 */
public class TxIdService {
    private final static Logger logger = LoggerFactory.getLogger(BlockService.class);

    private static class LazyHolder {
        private static final TxIdService INSTANCE = new TxIdService();
    }

    private TxIdService() {
    }

    public static TxIdService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean upSert(TxId txIdObj, String collection) {
        return MongoUtil.upSertJson("txId", txIdObj.getTxId(), txIdObj.toString(), collection, true);
    }

    public boolean upSertBatch(List<TxId> txIdList, String collectionName) {
        MongoCollection<Document> collection = MongoUtil.getCollection(collectionName);
        String key = "txId";
        // 如果集合不存在，并且需要创建唯一索引，则创建唯一索引
        if (!MongoUtil.collectionExists(collectionName)) {
            Document index = new Document(key, 0);
            collection.createIndex(index, new IndexOptions().unique(true));
        }

        Bson update = null;
        Bson filter = null;
        UpdateOptions options = new UpdateOptions().upsert(true);
        List<WriteModel<Document>> updates = new ArrayList<WriteModel<Document>>();
        for (TxId txIdObj : txIdList) {
            update = new Document("$set", Document.parse(txIdObj.toString()));
            filter = Filters.eq(key, txIdObj.getTxId());
            updates.add(new UpdateOneModel<Document>(filter, update, options));
        }
        com.mongodb.bulk.BulkWriteResult bulkWriteResult = collection.bulkWrite(updates);
        return bulkWriteResult.wasAcknowledged();
    }

    /**
     * 根据 TransactionIdMessage 生成 TxId
     * @param tim
     * @return
     */
    public List<TxId> genInstances(TxIdMessage tim) {
        List<TxId> txIdObjList = new ArrayList<TxId>();
        for (String txId : tim.getTxIdList()) {
            txIdObjList.add(new TxId(txId, false));
        }
        return txIdObjList;
    }

    public TxId genInstance(String txId) {
        return new TxId(txId, false);
    }

    /**
     * 将 tx id push到消息队列里
     *
     * @param txId
     */
    public void addTxIdToQueue(String txId) {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_ID_QUEUE);
        rmq.push(txId);
    }

    public void addTxIdsToQueue(List<String> txIdList) {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_ID_QUEUE);
        rmq.push(txIdList);
    }
}
