package service;

import entity.Block;
import entity.BlockMessage;
import entity.Transaction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/18.
 */
public class BlockMessageServiceTest {
    private final static Logger logger = LoggerFactory.getLogger(BlockMessageServiceTest.class);
    private BlockService blockService = BlockService.getInstance();
    @Test
    public void verify() throws Exception {
        Transaction tx = TransactionService.genTx("test type", "test content");
        List<String> txList = new ArrayList<String>();
        txList.add(tx.getTxId());
        Block block = blockService.genBlock("0", txList);
        BlockMessage blockMsg = BlockMessageService.genInstance(block);
        boolean rlt = BlockMessageService.verify(blockMsg);
        logger.info(rlt + "");
        assertEquals(true, rlt);
    }

}