package socket;

import entity.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BlockMessageService;
import service.BlockService;
import service.NetService;
import util.Const;
import util.NetUtil;

/**
 * Created by chao on 2017/12/10.
 * 用于从 RabbitMQ 中获取 Transaction Id，打包成区块，发送到 Validator 主节点上
 */
public class Blocker implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Blocker.class);
    private NetService netService = NetService.getInstance();
    private long timeInterval; //生成区块并发送的频率
    private int timeout; // Blocker 连接 Validator 的超时时间
    private String primaryValidatorIP;
    private int primaryValidatorPort;

    public Blocker() {
        this.timeInterval = 1000;
        this.timeout = 5000;
        this.primaryValidatorIP = NetUtil.getPrimaryNode().get("ip");
        this.primaryValidatorPort = Integer.valueOf(NetUtil.getPrimaryNode().get("port"));
    }

    public Blocker(String primaryValidatorIP, int primaryValidatorPort) {
        this.primaryValidatorIP = primaryValidatorIP;
        this.primaryValidatorPort = primaryValidatorPort;
    }

    public Blocker(long timeInterval, int timeout, String primaryValidatorIP, int primaryValidatorPort) {
        this.timeInterval = timeInterval;
        this.timeout = timeout;
        this.primaryValidatorIP = primaryValidatorIP;
        this.primaryValidatorPort = primaryValidatorPort;
    }

    public void run() {
        String queueName = Const.VERIFIED_TX_QUEUE;
        String preBlockId = "0";
        double limitTime = 10000; // 单位毫秒
        double limitSize = 2.0 / 1024.0; // 单位 MB
        Block block;
        while (true) {
            block = BlockService.genBlock(preBlockId, queueName, limitTime, limitSize);
            if (block != null) {
                logger.info("生成 block：" + block.getBlockId());
                preBlockId = block.getBlockId();
                sendBlock(block);
            }

            try {
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendBlock(Block block) {
        logger.info("开始发送 block: " + block.getBlockId());
        String rcvMsg = netService.sendMsg(BlockMessageService.genInstance(block).toString(), primaryValidatorIP,
                primaryValidatorPort, timeout);
        logger.info("服务器响应： " + rcvMsg);
    }

    public static void main(String[] args) {
        new Thread(new Blocker()).start();
    }
}
