package socket;

import entity.NetAddress;
import handler.BlockerServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.JsonUtil;
import util.NetUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chao on 2017/12/25.
 * blocker server 服务器，用以接收 Validator 发送来的消息并进行处理
 */
public class BlockerServer implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(BlockerServer.class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private NetAddress netAddr;

    public BlockerServer(NetAddress netAddr) throws IOException {
        this.netAddr = netAddr;
        this.serverSocket = new ServerSocket(netAddr.getPort());
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        String url = NetUtil.getRealIp() + ":" + serverSocket.getLocalPort();
        try {
            logger.info("启动 BlockerServer 服务器 " + url);
            while (true) {
                threadPool.execute(new BlockerServerHandler(serverSocket.accept(), netAddr));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据 netAddressList 启动对应端口的 TxIdCollector
     * @param netAddressList TxIdCollectorAddress 对象 list
     */
    public static void startBlockerServers(List<NetAddress> netAddressList) {
        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.
                newCachedThreadPool();
        for (NetAddress tic : netAddressList) {
            try {
                logger.info("开始启动端口为[" + tic.getPort() + "]的 Blocker Server");
                es.execute(new BlockerServer(tic));
                es.execute(new Blocker(tic));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        logger.info("验证节点终止运行");
    }

    public static void main(String[] args) {
        List<NetAddress> blockerList = JsonUtil.getBlockerAddressList(Const.BlockChainNodesFile);
        logger.info("Blocker 地址 list 为：" + blockerList);
        startBlockerServers(blockerList);
    }
}
