package socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Block;
import entity.Transaction;
import entity.Vedio;
import util.Const;
import util.MongoUtil;
import util.NetUtil;

import java.io.IOException;
import java.util.List;

/**
 * Created by chao on 2018/4/12.
 */
public class VedioSearch {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        String realIp = NetUtil.getRealIp();
        int port = 8000;
        String url = realIp + ":" + port;
        String txCollection = url + "." + Const.TX;
        System.out.println(txCollection);
        List<String> list = MongoUtil.find("txId", "21DRfqElCue2QoEKnO5C7SL0T3xPH70TH5zJzRpkato=", txCollection);
        Transaction tx = objectMapper.readValue(list.get(0), Transaction.class);
        List<String> blockList = MongoUtil.find("txIdList", tx.getTxId(), "127.0.0.1:9000.BlockChain");
        Block block = objectMapper.readValue(blockList.get(0), Block.class);
        Vedio vedio = objectMapper.readValue(tx.getContent(), Vedio.class);
        System.out.println("\n\nhashcode 为 " + vedio.getHashcode() + " 的视频信息为：" + vedio + "\n");
        System.out.println("该视频信息被存储在区块 [" + block.getBlockId() + "] 交易单 [" + tx.getTxId() + "]");
    }
}
