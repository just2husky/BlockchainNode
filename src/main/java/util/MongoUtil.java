package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import entity.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

/**
 * Created by chao on 2017/11/27.
 */
public class MongoUtil {
    private final static Logger logger = LoggerFactory.getLogger(MongoUtil.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private static MongoClient mongoClient = new MongoClient("localhost", 27017);
    private static MongoDatabase mongoDatabase;

    static {
        MongoClientOptions.Builder options = new MongoClientOptions.Builder();
        // options.autoConnectRetry(true);// 自动重连true
        // options.maxAutoConnectRetryTime(10); // the maximum auto connect retry time
        options.connectionsPerHost(300);// 连接池设置为300个连接,默认为100
        options.connectTimeout(15000);// 连接超时，推荐>3000毫秒
        options.maxWaitTime(5000); //
        options.socketTimeout(0);// 套接字超时时间，0无限制
        options.threadsAllowedToBlockForConnectionMultiplier(5000);// 线程队列数，如果连接线程排满了队列就会抛出“Out of semaphores to get db”错误。
        options.writeConcern(WriteConcern.ACKNOWLEDGED);//
        options.build();
        mongoDatabase = mongoClient.getDatabase("BlockChain");
    }

    /**
     * 获取集合
     * @param collectionName
     * @return
     */
    public static MongoCollection<Document> getCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName);
    }

    /**
     * 将 json 数据插入到名字为 collectionName 的 collection（表） 中
     *
     * @param jsonStr
     * @param collectionName
     */
    public static void insertJson(String jsonStr, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document documentn = Document.parse(jsonStr);
        collection.insertOne(documentn);
    }

    /**
     * 根据 key，value，将 json 数据插入到名字为 collectionName 的 collection（表） 中
     *
     * @param jsonStr
     * @param collectionName
     */
    public static boolean upSertJson(String key, String value, String jsonStr, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = Document.parse(jsonStr);
        Bson filter = Filters.eq(key, value);
        Bson update =  new Document("$set", document);
        UpdateOptions options = new UpdateOptions().upsert(true);
        UpdateResult updateResult = collection.updateOne(filter, update, options);
        return updateResult.wasAcknowledged();
    }

    public static boolean upSertJson(Map<String, String> map, String jsonStr, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = Document.parse(jsonStr);
        Bson filter = null;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            filter = Filters.and(Filters.eq(entry.getKey(), entry.getValue()));
        }
        if(filter != null) {
            Bson update = new Document("$set", document);
            UpdateOptions options = new UpdateOptions().upsert(true);
            UpdateResult updateResult = collection.updateOne(filter, update, options);
            return updateResult.wasAcknowledged();
        }
        logger.error("filter 为 null");
        return false;
    }

    /**
     * 插入key-value
     *
     * @param key
     * @param value
     * @param collectionName
     */
    public static void insertKV(String key, String value, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = new Document(key, value);
        collection.insertOne(document);
    }

    /**
     * 修改键为 key 值为 oldValue 的值为 newValue
     *
     * @param key
     * @param oldValue
     * @param newValue
     * @param collectionName
     */
    public static boolean updateKV(String key, String oldValue, String newValue, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        //更新文档   将文档中likes=100的文档修改为likes=200
        UpdateResult updateResult = collection.updateMany(eq(key, oldValue), new Document("$set", new Document(key, newValue)));
        return updateResult.wasAcknowledged();
    }

    /**
     * 判断 collectionName 是否存在
     *
     * @param collectionName
     * @return
     */
    public static boolean collectionExists(String collectionName) {
        return mongoDatabase.listCollectionNames()
                .into(new ArrayList<String>()).contains(collectionName);
    }

    /**
     * 查找集合 collectionName 中的第一条记录，以 json 形式返回
     *
     * @param collectionName
     * @return
     */
    public static String findFirstDoc(String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = collection.find().first();
        return document.toJson();
    }

    /**
     * 获取集合 collectionName 中所有的数据
     *
     * @param collectionName
     */
    public static List<String> findAll(String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        //检索所有文档
        /**
         * 1. 获取迭代器FindIterable<Document>
         * 2. 获取游标MongoCursor<Document>
         * 3. 通过游标遍历检索出的文档集合
         * */
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        List<String> list = new ArrayList<String>();
        String record;
        while (mongoCursor.hasNext()) {
            record = mongoCursor.next().toJson();
            list.add(record);
            logger.debug("record: " + record);
        }
        return list;
    }

    public static List<String> findAllSort(String collectionName, String sortKey, String sortForm) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        FindIterable<Document> findIterable = null;
        if(sortForm.equals(Const.DESC)) {
            findIterable = collection.find().sort(descending(sortKey));
        } else if(sortForm.equals(Const.ASC)) {
            findIterable = collection.find().sort(ascending(sortKey));
        }
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        List<String> list = new ArrayList<String>();
        String record;
        while (mongoCursor.hasNext()) {
            record = mongoCursor.next().toJson();
            list.add(record);
            logger.debug("record: " + record);
        }
        return list;
    }

    private static void insertTest(MongoDatabase mongoDatabase) {
        MongoCollection<Document> collection = mongoDatabase.getCollection("test");
        System.out.println("集合 test 选择成功");
        //插入文档
        /**
         * 1. 创建文档 org.bson.Document 参数为key-value的格式
         * 2. 创建文档集合List<Document>
         * 3. 将文档集合插入数据库集合中 mongoCollection.insertMany(List<Document>) 插入单个文档可以用 mongoCollection.insertOne(Document)
         * */
        Document document = new Document("title", "MongoDB").
                append("description", "database").
                append("likes", 100).
                append("by", "Fly");
        List<Document> documents = new ArrayList<Document>();
        documents.add(document);
        collection.insertMany(documents);
        System.out.println("文档插入成功");
    }

    /**
     * 在名字为 collectionName 的集合里查找同时匹配k,v的值，若查找到则返回 True
     *
     * @param key
     * @param value
     * @param collectionName
     */
    public static boolean findByKV(String key, String value, String collectionName) {
//        Block<Document> printBlock = new Block<Document>() {
//            public void apply(final Document document) {
//                System.out.println(document.toJson());
//            }
//        };
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
//        collection.find(eq(key, value)).forEach(printBlock);
        return collection.find(eq(key, value)).iterator().hasNext();
    }

    /**
     * 获取所有满足 key = value 的文档
     * @param key
     * @param value
     * @param collectionName
     * @return
     */
    public static List<String> find(String key, String value, String collectionName) {
        List<String> result = new ArrayList<String>();
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        for (Document document : collection.find(eq(key, value))) {
            result.add(document.toJson());
        }
        return result;
    }

    /**
     * 统计 key, value 出现的次数
     *
     * @param key
     * @param value
     * @param collectionName
     * @return
     */
    public static int countByKV(String key, String value, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Iterator it = collection.find(eq(key, value)).iterator();
        int count = 0;
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

    /**
     * 副本节点通过检查它们是否具有相同的视图，序列号和摘要来验证准备消息是否与预准备消息相匹配。
     *
     * @param ppmSign
     * @param viewId
     * @param seqNum
     * @param collectionName
     * @return
     */
    public static int countPPMSign(String ppmSign, String viewId, String seqNum, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Iterator it = collection.find(eq("ppmSign", ppmSign)).iterator();
        int count = 0;
        while (it.hasNext()) {

            Document document = (Document) it.next();
            String pmStr = document.toJson();
            logger.debug("pmStr: " + pmStr);
            Message msg = null;
            try {
                msg = objectMapper.readValue(pmStr, Message.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(msg != null && msg.getClass().getSimpleName().equals("PrepareMessage")) {
                PrepareMessage pm = (PrepareMessage) msg;
                if (pm.getViewId().equals(viewId) && pm.getSeqNum().equals(seqNum)) {
                    count++;
                }
            } else if(msg != null && msg.getClass().getSimpleName().equals("CommitMessage")) {
                CommitMessage cm = (CommitMessage) msg;
                if (cm.getViewId().equals(viewId) && cm.getSeqNum().equals(seqNum)) {
                    count++;
                }
            } else {
                logger.info("message 类型不为 PrepareMessage 或 CommitMessage");
            }

        }
        return count;
    }

    /**
     * 根据 ppmSign, viewId, seqNum 在 pmCollection 中查找一个 PrepareMessage 或 CommittedMessage
     *
     * @param ppmSign
     * @param viewId
     * @param seqNum
     * @param pmCollection
     * @return
     */
    public static Message findPM(String ppmSign, String viewId, String seqNum, String pmCollection, String msgType) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(pmCollection);
        BasicDBObject query = new BasicDBObject("ppmSign", new BasicDBObject("$eq", ppmSign))
                .append("viewId", new BasicDBObject("$eq", viewId))
                .append("seqNum", new BasicDBObject("$eq", seqNum));
//        String pmStr = collection.find(query).first().toJson();
        try {
            if (msgType.equals(Const.PM)) {
                return objectMapper.readValue(collection.find(query).first().toJson(), PrepareMessage.class);
            } else if (msgType.equals(Const.CMTM)) {
                return objectMapper.readValue(collection.find(query).first().toJson(), CommittedMessage.class);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据 ppm 的id查找 ppm，若存在则返回 PrePrepareMessage，否则就返回null
     *
     * @param ppmId
     * @param collectionName
     * @return
     */
    public static PrePrepareMessage findPPMById(String ppmId, String collectionName) throws IOException {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        FindIterable<Document> findIterable = collection.find(eq("msgId", ppmId));
        String ppmStr = null;
        if (findIterable.iterator().hasNext()) {
            logger.info("msgId 存在于集合：" + collectionName);
            ppmStr = findIterable.first().toJson();
            return objectMapper.readValue(ppmStr, PrePrepareMessage.class);
        } else {
            return null;
        }
    }

    public static void dropAllCollections() {
        MongoIterable<String> colls = mongoDatabase.listCollectionNames();
        for (String s : colls) {
            System.out.println(s);
            // 先删除所有Collection(类似于关系数据库中的"表")
            if (!s.equals("system.indexes")) {
                mongoDatabase.getCollection(s).drop();
            }
        }
    }

    /**
     * 获取集合 collectionName 中的记录数
     * @param collectionName
     * @return
     */
    public static long countRecords(String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        return collection.count();
    }

    /**
     * 根据 key 获取所有对应的value
     * @param key
     * @param collectionName
     * @return
     */
    public static Set<String> findValuesByKey(String key, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        MongoCursor<String> mongoCursor = collection.distinct(key, String.class).iterator();
        Set<String> blockIdSet = new HashSet<String>();
        while (mongoCursor.hasNext()) {
            blockIdSet.add(mongoCursor.next());
        }
//        System.out.println("blockIdSet长度为： " + blockIdSet.size());
        return blockIdSet;
    }

    /**
     * 根据 key 统计去重统计所有value的个数
     * @param key
     * @param collectionName
     * @return
     */
    public static int countValuesByKey(String key, String collectionName) {
        return findValuesByKey(key, collectionName).size();
    }

    public static void main(String args[]) {
        try {
            // 连接到 mongodb 服务
            MongoClient mongoClient = new MongoClient("localhost", 27017);

            // 连接到数据库
            MongoDatabase mongoDatabase = mongoClient.getDatabase("mycol");
            System.out.println("Connect to database successfully");
//            mongoDatabase.createCollection("test");
//            System.out.println("集合创建成功");

            findAll("test");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
