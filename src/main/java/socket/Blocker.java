package socket;

import entity.Block;
import entity.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BlockMessageService;
import service.BlockService;
import service.NetService;
import util.Const;
import util.JsonUtil;
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
    private String pbiPublisherIP;
    private int pbiPublisherPort;

    public Blocker() {
        this.timeInterval = 1000;
        this.timeout = 5000;
        NetAddress na = JsonUtil.getPublisherAddress(Const.BlockChainNodesFile);
        this.pbiPublisherIP = na.getIp();
        this.pbiPublisherPort = na.getPort();
    }

    public Blocker(String pbiPublisherIP, int pbiPublisherPort) {
        this.pbiPublisherIP = pbiPublisherIP;
        this.pbiPublisherPort = pbiPublisherPort;
    }

    public Blocker(long timeInterval, int timeout, String pbiPublisherIP, int pbiPublisherPort) {
        this.timeInterval = timeInterval;
        this.timeout = timeout;
        this.pbiPublisherIP = pbiPublisherIP;
        this.pbiPublisherPort = pbiPublisherPort;
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
        logger.info("开始向 [" + pbiPublisherIP + ":" + pbiPublisherPort + "] 发送 block: " + block.getBlockId());
        String rcvMsg = netService.sendMsg(BlockMessageService.genInstance(block).toString(), pbiPublisherIP,
                pbiPublisherPort, timeout);
        logger.info("服务器响应： " + rcvMsg);
    }

    public static void main(String[] args) {
        logger.info("启动 Blocker 服务器");
        new Thread(new Blocker()).start();
    }
}
