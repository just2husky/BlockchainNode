package service;

import entity.Transaction;
import entity.TransactionMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/18.
 */
public class TransactionMessageServiceTest {
    private final static Logger logger = LoggerFactory.getLogger(TransactionMessageServiceTest.class);
    private TransactionMessageService txMsgService = TransactionMessageService.getInstance();
    @Test
    public void verify() throws Exception {
        Transaction tx = TransactionService.genTx("test type", "test content");
        TransactionMessage txMsg = txMsgService.genInstance(tx);
        boolean rlt = TransactionMessageService.verify(txMsg);
        logger.info(rlt + "");
        assertEquals(true, rlt);
    }

}