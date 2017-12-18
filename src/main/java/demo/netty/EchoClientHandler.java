package demo.netty;

import entity.Block;
import entity.Transaction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import service.BlockService;
import service.TransactionService;
import util.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chao on 2017/12/13.
 */
@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
        System.out.println("Client received: " + obj);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String test = "hello";
        UserInfo userInfo = new UserInfo("chao", 123);
        double limitTime = 10000; // 单位毫秒
        double limitSize = 1.0 / 1024; // 单位 MB
        Block block = BlockService.genBlock("0", Const.QUEUE_NAME, limitTime, limitSize);
//        List<Transaction> txList = new ArrayList<Transaction>();
//        txList.add(TransactionService.genTx("test","test"));
//        Block block = BlockService.genBlock("0", txList);
        System.out.println("block: " + block.toString());
        System.out.println(Integer.MAX_VALUE);
////        ctx.writeAndFlush("1111111");
//       ctx.write(test);
//        ctx.write(userInfo);
        ctx.write(block);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
