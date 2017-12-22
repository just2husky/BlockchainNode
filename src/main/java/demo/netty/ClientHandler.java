package demo.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by chao on 2017/12/13.
 */
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Object> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String msg;

    public ClientHandler(String msg) {
        this.msg = msg;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
        System.out.println("Client received: " + obj);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("开始发送消息：" + this.msg);
        ctx.write(this.msg);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
