package socket;

import entity.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.JsonUtil;

import java.io.IOException;
import java.util.List;

import static util.Const.BlockChainNodesFile;
import static util.JsonUtil.getValidatorAddressList;

/**
 * Created by chao on 2017/12/25.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // 1.
        startTransactionIdCollector();
        // 2.
        startServerFrontEnd();
        // 3.
        startTransactionTransmitter();
    }

    public static void startTransactionIdCollector(){
        NetAddress na = JsonUtil.getTxIdCollectorAddress(Const.BlockChainNodesFile);
        try {
            new Thread(new TransactionIdCollector(na.getPort())).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startServerFrontEnd() {
        List<NetAddress> list = getValidatorAddressList(BlockChainNodesFile);
        logger.info("Validator 地址 list 为：" + list);
        ServerFrontEnd.startValidators(list);
    }

    public static void startTransactionTransmitter() {
        new Thread(new TransactionTransmitter()).start();
    }
}
