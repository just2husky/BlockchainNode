package demo.netty;

import entity.Block;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import org.msgpack.MessagePack;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import java.util.List;

/**
 * Created by chao on 2017/12/13.
 */

@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (msg instanceof  UserInfo) {
//            UserInfo userInfo = (UserInfo) msg;
//            System.out.println("Server received user: " + userInfo.getUserName());
//            ctx.write(msg);
//        }  else if(msg instanceof Block) {
//            Block block = (Block) msg;
//            System.out.println("Server received block: " + block.getBlockId());
//            ctx.write(block.getBlockId());
//        }
//        else {
//            System.out.println("Server received string: " + msg);
//            System.out.println(msg.getClass());
//            ctx.write(msg);
//        }
        System.out.println("进入  channelRead 方法");
        @SuppressWarnings("unchecked")
        Object object = new Converter((Value) msg).read(Object.class);
        System.out.println(object.getClass());
//        System.out.println("block: " + block.getBlockId());
//        ctx.write("block: " + block.getBlockId());

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
