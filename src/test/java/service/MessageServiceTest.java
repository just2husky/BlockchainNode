package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Message;
import entity.PrePrepareMessage;
import entity.PrepareMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/11/29.
 */
public class MessageServiceTest {
    private final static Logger logger = LoggerFactory.getLogger(MessageServiceTest.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    @Test
    public void genPrepareMsg() throws Exception {
//        PrePrepareMessage ppm = MessageService.genPrePrepareMsg("1","1");
//        System.out.println(ppm.toString());
//        PrepareMessage pm = MessageService.genPrepareMsg(ppm.getSignature(), ppm.getViewId(), ppm.getSeqNum(),
//                "127.0.0.1", 9999);
//        System.out.println(pm.toString());
//        boolean rlt = MessageService.verifyPrepareMsg(pm);
//        System.out.println("验证结果为：" + rlt);

    }



    @Test
    public void genPrePrepareMsg() throws Exception {

    }

    @Test
    public void verifyPrePrepareMsg() throws Exception {
//        PrePrepareMessage ppm = MessageService.genPrePrepareMsg("1","1");
//        System.out.println(ppm.toString());
//        boolean rlt = MessageService.verifyPrePrepareMsg(ppm);
//        System.out.println("验证结果为：" + rlt);

    }

    @Test
    public void getPPMSignContent() throws Exception {
//        System.out.println(MessageService.getPPMSignContent("1","2","3","4"));
    }

}