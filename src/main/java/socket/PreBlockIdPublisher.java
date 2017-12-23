package socket;

import demo.netty.NettyServer;
import demo.netty.PublisherHandler;
import entity.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.JsonUtil;


/**
 * Created by chao on 2017/12/19.
 * 用做 Pre Block Id 的发布中心
 */
public class PreBlockIdPublisher implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(PreBlockIdPublisher .class);

    @Override
    public void run() {
        NetAddress na = JsonUtil.getPublisherAddress(Const.BlockChainNodesFile);
        NettyServer publisherServer = new NettyServer(na.getPort(), new PublisherHandler());
        try {
            logger.info("启动 PreBlockPublisher 服务器");
            publisherServer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Thread(new PreBlockIdPublisher()).start();
    }
}
