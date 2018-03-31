package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.JsonUtil;

import java.util.List;

public class BlockerService {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(BlockerService.class);

    private static class LazyHolder {
        private static final BlockerService INSTANCE = new BlockerService();
    }
    private BlockerService (){}
    public static BlockerService getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 根据 blocker 的 addr 判断是否为打包节点
     * @param blockerAddr
     * @return
     */
    public boolean isCurrentBlocker(NetAddress blockerAddr, long blockLength){
        List<NetAddress> list = JsonUtil.getBlockerAddressList(Const.BlockChainNodesFile);
        int len = list.size();

        //若是主节点，则返回null
        // TODO
        if(list.get(0).equals(blockerAddr))
            return false;

        // 判断当前节点的序号，默认第一个节点为主节点，区块只由备份节点生成
        int seqNum = -1;
        for(int index = 1; index < len; index++) {
            if(list.get(index).equals(blockerAddr)) {
                seqNum = index - 1;
            }
        }
        int currentBlocker = (int) (blockLength % (len - 1));
        return currentBlocker == seqNum;
    }
}
