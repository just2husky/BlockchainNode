package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import service.BlockMessageService;
import service.BlockService;
import service.TransactionMessageService;
import service.TransactionService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao on 2017/12/18.
 */
public class Test {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    public static void main(String[] args) throws Exception {
        Transaction tx = TransactionService.genTx("test type", "test content");
        Message txMsg = TransactionMessageService.genInstance(tx);
        System.out.println(txMsg.getMsgId());
        System.out.println("tx: " + txMsg);

        List<Transaction> txList = new ArrayList<Transaction>();
        txList.add(tx);
        Block block = BlockService.genBlock("0", txList);
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
