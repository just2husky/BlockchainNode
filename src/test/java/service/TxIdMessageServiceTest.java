package service;

import entity.TxIdMessage;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/25.
 */
public class TxIdMessageServiceTest {
    private TxIdMessageService timSrv = TxIdMessageService.getInstance();
    @Test
    public void verify() throws Exception {
        TxIdMessage tim = timSrv.genInstance("111", "127.0.0.1", 8000);
        System.out.println(tim);
        assertEquals(true, timSrv.verify(tim));
    }

}