package socket;

import entity.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static util.Const.BlockChainNodesFile;
import static util.JsonUtil.getValidatorAddressList;

/**
 * Created by chao on 2017/11/10.
 */
public class ServerFrontEnd {

    private final static Logger logger = LoggerFactory.getLogger(ServerFrontEnd.class);

    /**
     * 根据 netAddressList 启动对应端口的 Validator
     * @param netAddressList validatorAddress 对象 list
     */
    public static void startValidators(List<NetAddress> netAddressList) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        logger.info("当前节点可用处理器数量为：" + availableProcessors);

        ThreadPoolExecutor es = (ThreadPoolExecutor) Executors.
                newCachedThreadPool();
        logger.info("availableProcessors: " + availableProcessors);
        for (NetAddress va : netAddressList) {
            try {
                logger.info("开始启动端口为[" + va.getPort() + "]的 Validator");
                es.execute(new ValidatorServer(va.getPort(), availableProcessors));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        logger.info("验证节点终止运行");
    }

    public static void main(String[] args) {

        List<NetAddress> list = getValidatorAddressList(BlockChainNodesFile);
        logger.info("Validator 地址 list 为：" + list);

        startValidators(list);
    }
}
