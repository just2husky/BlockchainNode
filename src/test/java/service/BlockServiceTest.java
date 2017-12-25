package service;

import entity.Block;
import org.junit.Test;
import util.Const;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/10.
 */
public class BlockServiceTest {
    private BlockService blockService = BlockService.getInstance();
    @Test
    public void genBlock() throws Exception {
    }

    @Test
    public void genBlock1() throws Exception {
    }

    @Test
    public void genBlock2() throws Exception {
        Block block = blockService.genBlock(Const.GENESIS_BLOCK_ID, Const.TX_ID_QUEUE, 100000, 2.0/1024.0);
        System.out.println(block);
    }

}