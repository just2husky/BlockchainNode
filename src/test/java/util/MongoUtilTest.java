package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import service.TransactionService;

/**
 * Created by chao on 2017/11/28.
 */
public class MongoUtilTest {
    @Test
    public void countPPMSign() throws Exception {
        String url = "192.168.43.153:8001";
        String pmCollection = url + "." + Const.PM;
        String ppmSign = "MEUCIA6hScboSC7SXaAUncyTlpAqcjJ5QgWAbl3ILBvm+NQBAiEA6tXLVU+BjG8Z9I2laN1LDbAklamp5TYZkrG4fWY5TuM=";
        String viewId = "1";
        String seqNum = "1";
        int count = MongoUtil.countPPMSign(ppmSign, viewId, seqNum, pmCollection);
        System.out.println("count: " + count);
    }

    @Test
    public void countByKV() throws Exception {
        System.out.println(MongoUtil.countByKV("ip", "192.168.43.153", "192.168.43.153:8001.PrepareMsg"));
    }

    @Test
    public void insertJson2() throws Exception {
    }

    @Test
    public void insertKV2() throws Exception {
    }

    @Test
    public void updateKV() throws Exception {
    }

    @Test
    public void collectionExists() throws Exception {
    }

    @Test
    public void findFirstDoc() throws Exception {
    }

    @Test
    public void traverse1() throws Exception {
    }

    @Test
    public void findByKV() throws Exception {
        String url = "202.115.53.99:8000";
        String ppmCollection = url + ".seqNum";
        System.out.println(MongoUtil.findByKV("msgType", "ClientMsg", "192.168.43.153:8000.ClientMsg"));
    }

    @Test
    public void insertJson1() throws Exception {
    }

    @Test
    public void insertKV1() throws Exception {
    }

    @Test
    public void insertKV() throws Exception {
        String jsonStr = (new ObjectMapper()).writeValueAsString(TransactionService.genTx("string", "test"));
        MongoUtil.insertKV("msg", jsonStr, "test");
    }

    @Test
    public void traverse() throws Exception {
        MongoUtil.traverse("192.168.43.153:8000.ClientMsg");
    }

    @Test
    public void insertJson() throws Exception {
        String jsonStr = (new ObjectMapper()).writeValueAsString(TransactionService.genTx("string", "test"));
        MongoUtil.insertJson(jsonStr, "test");
    }

}