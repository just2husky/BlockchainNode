package handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MessageService;
import util.Const;

/**
 * Created by chao on 2017/12/5.
 */
public class CommittedMsgHandler implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(CommittedMsgHandler.class);
    private String realIp;
    private int port;
    private MessageService msgService = new MessageService();

    public CommittedMsgHandler(String realIp, int port) {
        this.realIp = realIp;
        this.port = port;
    }

    public void run() {

        String url = realIp + ":" + port;
        String ppmCollection = url + "." + Const.PPM;
        String cmtmCollection = url + "." + Const.CMTM;
        String cmtdmCollection = url + "." + Const.CMTDM;
//        String blockChainCollection = url + "." + Const.BLOCK_CHAIN;
        msgService.traversePPMAndSaveMsg(ppmCollection, cmtmCollection, cmtdmCollection, Const.CMTDM, realIp, port);
    }
}
