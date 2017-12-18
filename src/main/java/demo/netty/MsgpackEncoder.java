package demo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

public class MsgpackEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {
        MessagePack msgPack = new MessagePack();
        //将object类型的pojo对象的转为字节数组,并写入ByteBuf中.
        byte[] raw = msgPack.write(obj);
        byteBuf.writeBytes(raw);
    }
}
