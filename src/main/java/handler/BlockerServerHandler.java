package handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.BlockMessage;
import entity.LastBlockIdMessage;
import entity.Message;
import entity.TxIdMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.TxIdMessageService;
import util.Const;
import util.NetUtil;

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

    public BlockerServerHandler(Socket socket) {
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
            logger.debug("接收到 msgType 为 [" + msgType + "] 的 Msg");
            String realIp = NetUtil.getRealIp();
            String url = realIp + ":" + socket.getLocalPort();
            String timCollection = "TxIdCollector" + url + "." + "TxIdMsgs";
            String txIdCollection = "TxIdCollector" + url + "." + "TxIds";

            if (msgType.equals(Const.TIM)) {
                TxIdMessage tim = (TxIdMessage) myMsg;
                out.writeUTF("接收到你的 TxIdMessage： " + tim.getMsgId());
                out.flush();
                socket.close();
                timSrv.procTxIdMsg(tim, timCollection, txIdCollection);
                logger.info("完成对[" + msgType + "] msg: " + tim.getMsgId() + " 的处理");
            } else if (msgType.equals(Const.BM)) {
                BlockMessage blockMsg = (BlockMessage) myMsg;
                out.writeUTF("接收到你的 BlockMessage： " + blockMsg.getMsgId());
                out.flush();
                socket.close();

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
