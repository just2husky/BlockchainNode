package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.CommittedMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.TransactionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chao on 2017/11/28.
 */
public class MongoUtilTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(MongoUtil.class);
    @Test
    public void upSertJson() throws Exception {
        String cmtdmStr = "{\"msgType\":\"CommittedMsg\",\"msgId\":\"ylnT4cxqunxpqdoDc61V9hEtzmksDZwwmJ6W082GZvs=\",\"msgType\":\"CommittedMsg\",\"timestamp\":\"1513652580955\",\"pubKey\":\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKp7wRlLhTDte3wC4Sd7pj42fBCMtNmUQ8cRsSTkY6Pw+qPuytjn4455A6/p7xOHJO8RIO9TRrGLLAnFJGHoZqg==\",\"signature\":\"MEUCIQCvEOXX90YcX7Iwcdd2C4XcUzvoJdwnDtjOYyCGSo2J5wIgUh0vtxTNwXn8xWrA03EOQU3YEsQ7sKDZF0kqHPOvNOw=\",\"cliMsgId\":\"aM6HTB9Z67sJoB2p+VKIOyiWFxL/UQX68LAZ7NJfGl0=\",\"viewId\":\"1\",\"seqNum\":\"11\",\"ip\":\"202.115.52.239\",\"port\":8003}";
        CommittedMessage cmtdm = objectMapper.readValue(cmtdmStr, CommittedMessage.class);
        System.out.println(cmtdm);
        cmtdm.setPort(123);
        String url = "202.115.52.239:8003";
        String coll = url + "." + Const.CMTDM;
        Map<String, String> map = new HashMap<String, String>();
        map.put("viewId","1");
        map.put("seqNum","11");
        MongoUtil.upSertJson(map, cmtdm.toString(), coll);
    }

    @Test
    public void upSertJson1() throws Exception {

    }

    @Test
    public void countRecords() throws Exception {
        System.out.println(MongoUtil.countRecords("202.115.53.57:8001.CommittedMsg"));
    }

    @Test
    public void findPM() throws Exception {

    }

    @Test
    public void dropAllCollections() throws Exception {
        MongoUtil.dropAllCollections();
    }

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
        MongoUtil.findAll("192.168.43.153:8000.ClientMsg");
    }

    @Test
    public void insertJson() throws Exception {
        String jsonStr = (new ObjectMapper()).writeValueAsString(TransactionService.genTx("string", "test"));
        MongoUtil.insertJson(jsonStr, "test");
    }

    @Test
    public void update() {
        String searchKey= "txId";
        String searchValue="6EPYnRIPgsf8SbxiBqWuXxogbRS1wE3xzBJogXC3CLE=";
        String upKey= "inBlock";
        boolean upValue = true;
        String collectionName = "TxIdCollector202.115.53.194:9000.TxIds";
        MongoUtil.update(searchKey, searchValue, upKey, upValue, collectionName);
    }

    @Test
    public void find() {
        double limitSize = 88 / 1024.0/1024;
        List<String> list = MongoUtil.find("TxIdCollector202.115.53.194:9003.TxIds", limitSize);
        for(String txId : list) {
            System.out.println(txId);
        }
    }
}