package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.BlockMessageService;
import service.TxIdMessageService;
import util.Const;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by chao on 2017/12/25.
 */
public class BlockerServerHandler implements Runnable {
    private final Socket socket;
    private final static Logger logger = LoggerFactory.getLogger(BlockerServerHandler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private TxIdMessageService timSrv = TxIdMessageService.getInstance();
    private BlockMessageService blockMsgServ = BlockMessageService.getInstance();

    private NetAddress blockerUrl;

    public BlockerServerHandler(Socket socket, NetAddress blockerUrl) {

        this.socket = socket;
        this.blockerUrl = blockerUrl;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String rcvMsg = in.readUTF();
            Message myMsg = objectMapper.readValue(rcvMsg, Message.class);
            String msgType = myMsg.getMsgType();
            logger.debug("接收到 msgType 为 [" + msgType + "] 的 Msg");

            String url = blockerUrl.toString();

            String txIdMsgCollection = url + "." + Const.TIM;
            String txIdCollection = url + "." + Const.TX_ID;

            String blockMsgCollection = url + "." + Const.BM;
            String blockChainCollection = url + "." + Const.BLOCK_CHAIN;
            String lbiCollection = url + "." + Const.LAST_BLOCK_ID;


            if (msgType.equals(Const.TIM)) {
                TxIdMessage tim = (TxIdMessage) myMsg;
                out.writeUTF("接收到你的 TxIdMessage： " + tim.getMsgId());
                out.flush();
                socket.close();
                timSrv.procTxIdMsg(tim, txIdMsgCollection, txIdCollection);
                logger.info("完成对[" + msgType + "] msg: " + tim.getMsgId() + " 的处理");
            } else if (msgType.equals(Const.BM)) {
                BlockMessage blockMsg = (BlockMessage) myMsg;
                out.writeUTF("接收到你的 BlockMessage： " + blockMsg.getMsgId());
                out.flush();
                socket.close();
                blockMsgServ.procBlockerBlockMsg(blockMsg, this.blockerUrl, blockMsgCollection, txIdCollection,
                        blockChainCollection, lbiCollection);
                logger.info("完成对[" + msgType + "] msg: " + blockMsg.getMsgId() + " 的处理");

            } else
            {
                logger.error("服务器接收到尚不能处理类型的 msg: " + rcvMsg);
                out.writeUTF("服务器接收到尚不能处理类型的 msg: " + rcvMsg);
                out.flush();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
