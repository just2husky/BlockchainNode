package service;

import entity.TxIdMessage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/25.
 */
public class TxIdMessageServiceTest {
    private TxIdMessageService timSrv = TxIdMessageService.getInstance();
    @Test
    public void verify() throws Exception {
        List<String> txIdList = new ArrayList<String>();
        txIdList.add("111");
        txIdList.add("222");
        TxIdMessage tim = timSrv.genInstance(txIdList, "127.0.0.1", 8000);
        System.out.println(tim);
        assertEquals(true, timSrv.verify(tim));
    }

}