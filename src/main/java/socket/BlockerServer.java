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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                threadPool.execute(new BlockerServerHandler(serverSocket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        NetAddress na = JsonUtil.getTxIdCollectorAddress(Const.BlockChainNodesFile);
        try {
            new Thread(new BlockerServer(na)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
