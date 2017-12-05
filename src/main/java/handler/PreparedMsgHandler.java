package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.PrePrepareMessage;
import entity.PreparedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MessageService;
import util.Const;
import util.MongoUtil;
import util.NetUtil;
import util.PeerUtil;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chao on 2017/12/5.
 */
public class PreparedMsgHandler implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(PreparedMsgHandler.class);
    private String realIp;
    private int port;
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static int sleepTime = 60000;

    public PreparedMsgHandler(String realIp, int port) {
        this.realIp = realIp;
        this.port = port;
    }

    public void run() {

        String url = realIp + ":" + port;
        String ppmCollection = url + "." + Const.PPM;
        String pmCollection = url + "." + Const.PM;
        String pdmCollection = url + "." + Const.PDM;
        Set<String> ppmSet = new HashSet<String>();
        while (true) {
            logger.info("开始遍历" + ppmCollection);
            ppmSet = MongoUtil.traverse(ppmCollection);
            for(String ppmStr : ppmSet) {
                PrePrepareMessage ppm = null;
                try {
                    ppm = objectMapper.readValue(ppmStr, PrePrepareMessage.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.info("开始统计 " + ppm.getSignature() + "在 " + pmCollection + " 出现的次数");
                int count = MongoUtil.countPPMSign(ppm.getSignature(), ppm.getViewId(), ppm.getSeqNum(), pmCollection);
                logger.info(ppm.getSignature() + "在 " + pmCollection + " 出现的次数为： " + count);
                if(!MongoUtil.findByKV("cliMsgId", ppm.getCliMsgId(), pdmCollection)) {
                    if (2 * PeerUtil.getFaultCount() <= count) {
                        logger.info("开始生成 PreparedMessage 并存入数据库");
                        PreparedMessage pdm = MessageService.genPreparedMsg(ppm.getCliMsgId(), ppm.getViewId(),
                                ppm.getSeqNum(), NetUtil.getRealIp(), port);
                        if (MessageService.savePDMsg(pdm, pdmCollection)) {
                            logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存入数据库");
                        }
//                    else {
//                        logger.info("PreparedMessage [" + pdm.getMsgId() + "] 已存在");
//                    }
                    }
                } else {
                    logger.info("pdm已存在，不需要存入");
                }
            }

            try {
                Thread.currentThread().sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
