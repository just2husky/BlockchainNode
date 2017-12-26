package socket;

import entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.NetService;
import service.TransactionMessageService;
import service.TransactionService;
import util.Const;
import util.NetUtil;

import java.util.List;

/**
 * Created by chao on 2017/12/19.
 * 用于从 RabbitMQ 中获取 Transaction，发送到 Validator 主节点上
 */
public class TransactionTransmitter implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(TransactionTransmitter.class);
    private TransactionService txService = TransactionService.getInstance();
    private TransactionMessageService txMsgService = TransactionMessageService.getInstance();
    private NetService netService = NetService.getInstance();
    private long timeInterval; //发送Tx的频率
    private int timeout; // Blocker 连接 Validator 的超时时间
    private String primaryValidatorIP;
    private int primaryValidatorPort;

    public TransactionTransmitter() {
        this.timeInterval = 500;
        this.timeout = 5000;
        this.primaryValidatorIP = NetUtil.getPrimaryNode().getIp();
        this.primaryValidatorPort = NetUtil.getPrimaryNode().getPort();
    }

    public TransactionTransmitter(String primaryValidatorIP, int primaryValidatorPort) {
        this.primaryValidatorIP = primaryValidatorIP;
        this.primaryValidatorPort = primaryValidatorPort;
    }

    public TransactionTransmitter(int timeout, String primaryValidatorIP, int primaryValidatorPort) {
        this.timeout = timeout;
        this.primaryValidatorIP = primaryValidatorIP;
        this.primaryValidatorPort = primaryValidatorPort;
    }

    @Override
    public void run() {
        String queueName = Const.TX_QUEUE;
        double limitTime = 5000; // 单位毫秒
        double limitSize = 20 / 1024.0; // 单位 MB
        List<Transaction> txList ;
        while (true) {
            txList = txService.pullTxList(queueName, limitTime, limitSize);
            if (txList != null && txList.size() > 0) {
                logger.info("获得 Transaction：" + txService.getTxIdList(txList));
                sendTxMsg(txList);
            }
            try {
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void sendTxMsg(List<Transaction> txList) {
        logger.info("开始发送 Transaction List");
        String rcvMsg = netService.sendMsg(txMsgService.genInstance(txList).toString(), primaryValidatorIP,
                primaryValidatorPort, timeout);
        logger.info("服务器响应： " + rcvMsg);
    }

    public static void main(String[] args) {
        logger.info("启动 TransactionTransmitter 服务器");
        new Thread(new TransactionTransmitter()).start();
    }

}
