package service;

import org.junit.Test;
import util.MongoUtil;

import static org.junit.Assert.*;

public class TxIdServiceTest {

    private TxIdService tis = TxIdService.getInstance();
    @Test
    public void setTrue() {
        String searchValue="6EPYnRIPgsf8SbxiBqWuXxogbRS1wE3xzBJogXC3CLE=";
        String collectionName = "TxIdCollector202.115.53.194:9000.TxIds";
        tis.setTrue(searchValue, collectionName);
    }

    @Test
    public void setFalse() {
        String searchValue="6EPYnRIPgsf8SbxiBqWuXxogbRS1wE3xzBJogXC3CLE=";
        String collectionName = "TxIdCollector202.115.53.194:9000.TxIds";
        tis.setFalse(searchValue, collectionName);
    }
}