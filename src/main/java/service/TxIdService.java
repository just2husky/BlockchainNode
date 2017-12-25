package service;

import entity.TxId;
import entity.TxIdMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;
import util.MongoUtil;
import util.RabbitmqUtil;

/**
 * Created by chao on 2017/12/25.
 */
public class TxIdService {
    private final static Logger logger = LoggerFactory.getLogger(BlockService.class);

    private static class LazyHolder {
        private static final TxIdService INSTANCE = new TxIdService();
    }

    private TxIdService() {
    }

    public static TxIdService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public boolean upSert(TxId txIdObj, String collection) {
        return MongoUtil.upSertJson("txId", txIdObj.getTxId(), txIdObj.toString(), collection, true);
    }
    public TxId genInstance(TxIdMessage tim) {
        return new TxId(tim.getTxId(), false);
    }

    /**
     * 将 tx id push到消息队列里
     * @param txId
     */
    public void addTxIdToQueue(String txId) {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_ID_QUEUE);
        rmq.push(txId);
    }
}
