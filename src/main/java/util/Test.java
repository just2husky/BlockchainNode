package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import service.BlockMessageService;
import service.BlockService;
import service.TransactionMessageService;
import service.TransactionService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by chao on 2017/12/18.
 */
public class Test {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @org.junit.Test
    public void getList() {
        Set<String> set = MongoUtil.findValuesByKey("txIdList", "TxIdCollector202.115.52.132:9001.TxIdMsgs");
        Iterator it = set.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

    public static void main(String[] args) throws Exception {
        TransactionMessageService txMsgService = TransactionMessageService.getInstance();
        BlockService blockService = BlockService.getInstance();
        Transaction tx = TransactionService.genTx("test type", "test content");
        List<Transaction> txList = new ArrayList<Transaction>();
        txList.add(tx);
        Message txMsg = txMsgService.genInstance(txList);
        System.out.println(txMsg.getMsgId());
        System.out.println("tx: " + txMsg);

        List<String> txIdList = new ArrayList<String>();
        txIdList.add(tx.getTxId());
        Block block = blockService.genBlock(Const.GENESIS_BLOCK_ID, txIdList);
        Message blockMsg = BlockMessageService.genInstance(block);
        System.out.println(blockMsg.getMsgId());

        String txMsgStr = objectMapper.writeValueAsString(txMsg);
        String blockMsgStr = objectMapper.writeValueAsString(blockMsg);
        System.out.println(txMsgStr);
        System.out.println(blockMsgStr);

        Message msg = objectMapper.readValue(txMsgStr, Message.class);
        System.out.println("tx: " + msg);

        System.out.println(txMsg.getClass().getSimpleName());
        System.out.println(blockMsg.getClass().getName());
    }
}
