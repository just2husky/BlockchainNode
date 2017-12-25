package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.NetAddress;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/10.
 */
public class JsonUtilTest {
    @Test
    public void getTxIdCollectorAddress() throws Exception {
        System.out.println(JsonUtil.getTxIdCollectorAddress(Const.BlockChainNodesFile));
    }

    private final static ObjectMapper objMapper = new ObjectMapper();

    @Test
    public void getValidatorAddressList() throws Exception {
        List<NetAddress> list = JsonUtil.getValidatorAddressList(Const.BlockChainNodesFile);
        for (NetAddress na : list) {
            System.out.println(na);
        }
    }

    @Test
    public void getPublisherAddress() throws Exception {
        System.out.println(JsonUtil.getPublisherAddress(Const.BlockChainNodesFile));
    }

    @Test
    public void getStrByJsonFile() throws Exception {
        JsonUtil.getValidatorAddressList(Const.BlockChainNodesFile);
    }

    @Test
    public void isList() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        String listStr = objMapper.writeValueAsString(list);
        System.out.println(JsonUtil.isList(listStr));
        assertEquals(JsonUtil.isList(listStr), true);

    }

}