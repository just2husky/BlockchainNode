package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Transaction;
import org.junit.Test;
import service.TransactionService;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 junit 以单独运行一个方法
 * Created by chao on 2017/12/11.
 */
public class RunUtil {
    private final static ObjectMapper objMapper = new ObjectMapper();

    /**
     * 统计各个集合中记录的数量
     */
    @Test
    public void countRecordQuantity() {
        String realIp = NetUtil.getRealIp();
        String url;
        String ppmCollection;
        String pmCollection;
        String pdmCollection;
        String cmtmCollection;
        String cmtdmCollection;
        String blockChainCollection;
        String txCollection;

        for (int port = 8000; port < 8004; port ++) {
            url = realIp + ":" + port;
            ppmCollection = url + "." + Const.PPM;
            pmCollection = url + "." + Const.PM;
            pdmCollection = url + "." + Const.PDM;
            cmtmCollection = url + "." + Const.CMTM;
            cmtdmCollection = url + "." + Const.CMTDM;
            blockChainCollection = url + "." + Const.BLOCK_CHAIN;
            txCollection = url + "." + Const.TX;

            long ppmCount = MongoUtil.countRecords(ppmCollection);
            long pmCount = MongoUtil.countRecords(pmCollection);
            long pdmCount = MongoUtil.countRecords(pdmCollection);
            long cmtmCount = MongoUtil.countRecords(cmtmCollection);
            long cmtdmCount = MongoUtil.countRecords(cmtdmCollection);
            long blockChainCount = MongoUtil.countRecords(blockChainCollection);
            long txCount = MongoUtil.countRecords(txCollection);
            int blockIdCount = MongoUtil.countValuesByKey("blockId", blockChainCollection);

            System.out.println("主机 [ " + url + " ] < ppmCount: " + ppmCount
                    + ", pmCount: " + pmCount
                    + ", pdmCount: " + pdmCount
                    + ", cmtmCount: " + cmtmCount
                    + ", cmtdmCount: " + cmtdmCount
                    + ", blockChainCount: " + blockChainCount
                    + ", txCount: " + txCount
                    + ", blockIdCount: " + blockIdCount);
        }
    }

    /**
     * 清空所有集合
     * @throws Exception
     */
    @Test
    public void dropAllCollections() throws Exception {
        MongoUtil.dropAllCollections();
    }

    /**
     * 向队列中添加 tx
     */
    @Test
    public void addTxToQueue() {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE);
        List<Transaction> txList = new ArrayList<Transaction>();
        try {
            for (int i = 0; i < 50; i++) {
                Transaction tx = TransactionService.genTx("string" + i, "测试" + i);
//                if(i<4) {
//                    txList.add(tx);
//                }
                rmq.push(tx.toString());
            }
//            rmq.push(objectMapper.writeValueAsString(txList));
//            logger.info(objectMapper.writeValueAsString(txList).substring(0,1));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        List<String> msgList = rmq.pull(100000, 4.0/1024.0);
//        for(String msg : msgList) {
//            System.out.println(msg);
//        }
    }

    @Test
    public void addTxIdToQueue() {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE);
        try {
            for (int i = 0; i < 50; i++) {
                Transaction tx = TransactionService.genTx("string" + i, "测试" + i);
                rmq.push(tx.getTxId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void countBlocks(){
        String realIp = NetUtil.getRealIp();
        String url;
        String blockChainCollection;
        for (int port = 8000; port < 8004; port ++) {
            url = realIp + ":" + port;
            blockChainCollection = url + "." + Const.BLOCK_CHAIN;
            MongoUtil.findValuesByKey("blockId", blockChainCollection);
        }
    }

}
