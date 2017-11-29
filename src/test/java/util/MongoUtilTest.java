package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import service.TransactionService;

/**
 * Created by chao on 2017/11/28.
 */
public class MongoUtilTest {
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
        System.out.println(MongoUtil.findByKV("seqNum", "12", ppmCollection));
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
        MongoUtil.traverse("seq");
    }

    @Test
    public void insertJson() throws Exception {
        String jsonStr = (new ObjectMapper()).writeValueAsString(TransactionService.genTx("string", "test"));
        MongoUtil.insertJson(jsonStr, "test");
    }

}