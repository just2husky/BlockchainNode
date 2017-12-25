package socket;

import entity.NetAddress;
import handler.TxIdCollectorHandler;
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
 */
public class TransactionIdCollector implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(TransactionIdCollector.class);
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public TransactionIdCollector(int port, int poolSize) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public void run() {
        String url = NetUtil.getRealIp() + ":" + serverSocket.getLocalPort();
        try {
            logger.info("启动 TransactionIdCollector 服务器");
            while (true) {
                threadPool.execute(new TxIdCollectorHandler(serverSocket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        NetAddress na = JsonUtil.getTxIdCollectorAddress(Const.BlockChainNodesFile);
        try {
            new Thread(new TransactionIdCollector(na.getPort(), availableProcessors)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
