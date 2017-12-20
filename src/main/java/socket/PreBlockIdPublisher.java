package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.MongoUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Created by chao on 2017/12/19.
 * 用做 Pre Block Id 的发布中心
 */
public class PreBlockIdPublisher implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(PreBlockIdPublisher .class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private String PreBlockIdColl = Const.PRE_BLOCK_ID_COLLECTION;

    @Override
    public void run() {
        initPreBlockIdColl(PreBlockIdColl);
    }

    /**
     * 创建并初始化 Pre Block Id
     * @param collectionName
     */
    private void initPreBlockIdColl(String collectionName) {
        if (!MongoUtil.collectionExists(collectionName)) {
            logger.debug("集合" + collectionName + "不存在，开始创建");
            MongoUtil.insertKV(Const.PRE_BLOCK_ID, "0", collectionName);
        }
    }

    /**
     * 从 collectionName 中获取 PreBlockId
     * @param collectionName
     * @return
     */
    private String getPreBlockId(String collectionName){
        if (!MongoUtil.collectionExists(collectionName)) {
            initPreBlockIdColl(collectionName);
            return null;
        } else {
            String record = MongoUtil.findFirstDoc(collectionName);
            if (record != null && !record.equals("")) {
                try {
                    return (String) objectMapper.readValue(record, Map.class).get(Const.PRE_BLOCK_ID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.error("PreBlockId record 反序列化失败！");
                return null;

            } else {
                logger.error("获取 PreBlockId 失败！");
                return null;
            }
        }
    }

    /**
     * 到 collectionName 里去获取更新的PreBlockId
     *
     * @param collectionName
     * @throws Exception
     */
    public void updateSeqNum(String newPreBlockId, String collectionName) {

        String oldPreBlockId = getPreBlockId(collectionName);
        MongoUtil.updateKV(Const.PRE_BLOCK_ID, oldPreBlockId, newPreBlockId, collectionName);
    }

}
