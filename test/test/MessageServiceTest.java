package test;

import entity.PrepareMessage;
import org.junit.Test;
import service.MessageService;
import util.Const;

import static org.junit.Assert.*;

/**
 * Created by m on 2017/11/27.
 */
public class MessageServiceTest {
    @Test
    public void genPrepareMsg() throws Exception {
        PrepareMessage pm = MessageService.genPrepareMsg(Const.PM, util.NetUtil.getRealIp(), 8000);
        System.out.println(pm.toString());
    }

}