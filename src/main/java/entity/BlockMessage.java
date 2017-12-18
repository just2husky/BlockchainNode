package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.Const;

/**
 * Created by chao on 2017/11/11.
 * client 向 Validator 发送的消息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockMessage extends ClientMessage {
    private Block block;

    public BlockMessage() {
    }

    public BlockMessage(String msgId, String timestamp, String pubKey, String signature, Block block) {
        super(msgId, Const.BM, timestamp, pubKey, signature);
        this.block = block;
    }

    @Override
    public String toString() {
        String rtn = null;
        try {
            rtn = (new ObjectMapper()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
