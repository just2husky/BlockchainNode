package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MessageService;
import util.Const;

/**
 * Created by chao on 2017/12/5.
 */
public class PreparedMsgHandler implements Runnable{
    private final static Logger logger = LoggerFactory.getLogger(PreparedMsgHandler.class);
    private String realIp;
    private int port;
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static int sleepTime = 60000;
    private MessageService msgService = new MessageService();

    public PreparedMsgHandler(String realIp, int port) {
        this.realIp = realIp;
        this.port = port;
    }

    public void run() {

        String url = realIp + ":" + port;
        String ppmCollection = url + "." + Const.PPM;
        String pmCollection = url + "." + Const.PM;
        String pdmCollection = url + "." + Const.PDM;
        msgService.traversePPMAndSaveMsg(ppmCollection, pmCollection, pdmCollection, Const.PDM, realIp, port);
    }
}
