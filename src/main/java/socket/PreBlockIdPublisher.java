package socket;

import demo.netty.NettyServer;
import demo.netty.PublisherHandler;
import entity.NetAddress;
import handler.Handler;
import handler.PreBlockIdPublisherHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.JsonUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by chao on 2017/12/19.
 * 用做 Pre Block Id 的发布中心
 */
public class PreBlockIdPublisher implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(PreBlockIdPublisher .class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public PreBlockIdPublisher(int port, int poolSize) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public void run() {
//        NetAddress na = JsonUtil.getPublisherAddress(Const.BlockChainNodesFile);
//        NettyServer publisherServer = new NettyServer(na.getPort(), new PublisherHandler());
//        try {
//            logger.info("启动 PreBlockPublisher 服务器");
//            publisherServer.start();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            logger.info("启动 PreBlockPublisher 服务器");
            //noinspection InfiniteLoopStatement
            while (true) {
                threadPool.execute(new PreBlockIdPublisherHandler(serverSocket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        try {
            new Thread(new PreBlockIdPublisher(9000, availableProcessors)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
