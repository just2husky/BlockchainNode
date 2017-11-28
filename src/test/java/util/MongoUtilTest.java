package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import service.TransactionService;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/11/28.
 */
public class MongoUtilTest {

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