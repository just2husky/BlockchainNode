package demo.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Block;
import entity.BlockMessage;
import entity.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Const;

/**
 * Created by chao on 2017/12/13.
 */

@ChannelHandler.Sharable
public class PublisherHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(PublisherHandler.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("Duplicates")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String msgStr = new Converter((Value) msg).read(Templates.TString);
        Message myMsg = objectMapper.readValue(msgStr, Message.class);
        String msgType = myMsg.getMsgType();
        logger.info("msgType: " + msgType);
        if (msgType.equals(Const.BM)) {
            Block block = ((BlockMessage) myMsg).getBlock();
            logger.info("服务器接收到区块: " + block.getBlockId());
            ctx.write("服务器接收到区块: " + block.getBlockId());
        } else {
            logger.error("服务器接收到未知类型的 msg: " + msg);
            ctx.write("未知类型的 msg:" + msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
