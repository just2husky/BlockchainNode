package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.netty.NettyServer;
import demo.netty.PublisherHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;


/**
 * Created by chao on 2017/12/19.
 * 用做 Pre Block Id 的发布中心
 */
public class PreBlockIdPublisher implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(PreBlockIdPublisher .class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private String PreBlockIdColl = Const.PRE_BLOCK_ID_COLLECTION;

    @Override
    public void run() {
        NettyServer publisherServer = new NettyServer(9000, new PublisherHandler());
        try {
            logger.info("启动 PreBlockPublisher 服务器");
            publisherServer.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
