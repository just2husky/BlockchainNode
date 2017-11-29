package util;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by chao on 2017/11/27.
 */
public class MongoUtil {
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    static {
        mongoClient = new MongoClient( "localhost" , 27017 );
        mongoDatabase = mongoClient.getDatabase("mycol");
    }

    /**
     * 将 json 数据插入到名字为 collectionName 的 collection（表） 中
     * @param jsonStr
     * @param collectionName
     */
    public static void insertJson(String jsonStr, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document documentn = Document.parse(jsonStr);
        collection.insertOne(documentn);
    }

    /**
     * 插入key-value
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
     * @param key
     * @param oldValue
     * @param newValue
     * @param collectionName
     */
    public static void updateKV(String key, String oldValue, String newValue, String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        //更新文档   将文档中likes=100的文档修改为likes=200
        collection.updateMany(eq(key, oldValue), new Document("$set",new Document(key, newValue)));
    }

    /**
     * 判断 collectionName 是否存在
     * @param collectionName
     * @return
     */
    public static boolean collectionExists(String collectionName) {
        return mongoDatabase.listCollectionNames()
                .into(new ArrayList<String>()).contains(collectionName);
    }

    /**
     * 查找集合 collectionName 中的第一条记录，以 json 形式返回
     * @param collectionName
     * @return
     */
    public static String findFirstDoc(String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = collection.find().first();
        return document.toJson();
    }

    /**
     * 根据 collectionName 遍历 collection
     * @param collectionName
     */
    public static void traverse(String collectionName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        //检索所有文档
        /**
         * 1. 获取迭代器FindIterable<Document>
         * 2. 获取游标MongoCursor<Document>
         * 3. 通过游标遍历检索出的文档集合
         * */
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while(mongoCursor.hasNext()){
            System.out.println(mongoCursor.next());
        }
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
     * @param key
     * @param value
     * @param collectionName
     */
    public static boolean findByKV(String key, String value, String collectionName) {
        Block<Document> printBlock = new Block<Document>() {
            public void apply(final Document document) {
                System.out.println(document.toJson());
            }
        };
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
//        collection.find(eq(key, value)).forEach(printBlock);
        return collection.find(eq(key, value)).iterator().hasNext();
    }



    public static void main( String args[] ){
        try{
            // 连接到 mongodb 服务
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );

            // 连接到数据库
            MongoDatabase mongoDatabase = mongoClient.getDatabase("mycol");
            System.out.println("Connect to database successfully");
//            mongoDatabase.createCollection("test");
//            System.out.println("集合创建成功");

            traverse("test");
        }catch(Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
}
