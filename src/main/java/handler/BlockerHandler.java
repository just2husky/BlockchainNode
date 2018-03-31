package handler;

import entity.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockerHandler implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(BlockerHandler.class);
    private long timeInterval; //生成区块并发送的频率
    private int timeout; // Blocker 连接 Validator 的超时时间
    private NetAddress primaryNodeAddr;

    public BlockerHandler() {
    }

    public BlockerHandler(long timeInterval, NetAddress primaryNodeAddr) {
        this.timeInterval = timeInterval;
        this.primaryNodeAddr = primaryNodeAddr;
    }

    public BlockerHandler(long timeInterval, int timeout, NetAddress primaryNodeAddr) {
        this.timeInterval = timeInterval;
        this.timeout = timeout;
        this.primaryNodeAddr = primaryNodeAddr;
    }

    @Override
    public void run() {

    }
}
