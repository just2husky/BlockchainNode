package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BlockMessageService;
import service.LastBlockIdMessageService;
import service.NetService;
import util.Const;
import util.NetUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by chao on 2017/12/23.
 */
public class LastBlockIdPublisherHandler implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(LastBlockIdPublisherHandler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Socket socket;
    private LastBlockIdMessageService lbmService = LastBlockIdMessageService.getInstance();
    private NetService netService = NetService.getInstance();

    public LastBlockIdPublisherHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String rcvMsg = in.readUTF();
            Message myMsg = objectMapper.readValue(rcvMsg, Message.class);
            String msgType = myMsg.getMsgType();
            logger.info("接收到 msgType 为 [" + msgType + "] 的 Msg");
            String realIp = NetUtil.getRealIp();
            String url = realIp + ":" + socket.getLocalPort();
            String lbiCollection = "Publisher" + url + "." + Const.LAST_BLOCK_ID;
            String lbiMsgCollection = "Publisher" + url + "." + Const.LAST_BLOCK_ID_MSG;
            String simpleBlockCollection = "Publisher" + url + "." + Const.SIMPLE_BLOCK;

            //noinspection Duplicates
            if (msgType.equals(Const.LBIM)) {
                LastBlockIdMessage lbiMsg = (LastBlockIdMessage) myMsg;
                out.writeUTF("接收到你的 LastBlockIdMessage： " + lbiMsg.getMsgId());
                out.flush();
                socket.close();
                lbmService.procLastBlockIdMsg(lbiMsg, lbiCollection, lbiMsgCollection, simpleBlockCollection);

            } else if (msgType.equals(Const.BM)) {
                BlockMessage blockMsg = (BlockMessage) myMsg;
                Block block = blockMsg.getBlock();
                logger.info("服务器接收到区块: " + block.getBlockId());
                out.writeUTF("接收到你的 BlockMessage： " + myMsg.getMsgId() + ", Block: " + block.getBlockId());
                out.flush();
                socket.close();
                this.procBlockMsg(blockMsg);

            } else {
                logger.error("服务器接收到尚不能处理类型的 msg: " + rcvMsg);
                out.writeUTF("服务器接收到尚不能处理类型的 msg: " + rcvMsg);
                out.flush();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void procBlockMsg(BlockMessage blockMsg) {
        NetAddress na = NetUtil.getPrimaryNode();
        String primaryNodeIp = na.getIp();
        int primaryNodePort = na.getPort();
        Block block = blockMsg.getBlock();
        logger.info("开始向主节点 [" + primaryNodeIp + ":" + primaryNodePort + "] 发送 block: " + block.getBlockId());
        String rcvMsg = netService.sendMsg(BlockMessageService.genInstance(block).toString(), primaryNodeIp,
                primaryNodePort);
        logger.info("主节点响应： " + rcvMsg);
    }
}
