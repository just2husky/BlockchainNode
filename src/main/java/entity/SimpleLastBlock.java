package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by chao on 2017/12/24.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleLastBlock {
    private String blockId;
    private String preBlockId;

    public SimpleLastBlock() {
    }

    public SimpleLastBlock(String blockId, String preBlockId) {
        this.blockId = blockId;
        this.preBlockId = preBlockId;
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

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getPreBlockId() {
        return preBlockId;
    }

    public void setPreBlockId(String preBlockId) {
        this.preBlockId = preBlockId;
    }
}
