package util;

import entity.NetAddress;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/18.
 */
public class NetUtilTest {
    @Test
    public void getPrimaryNode() throws Exception {
        NetAddress na = NetUtil.getPrimaryNode();
        assertEquals("127.0.0.1", na.getIp());
        assertEquals(8000, na.getPort());
    }

    @Test
    public void getPrimaryNodUrl() throws Exception {
        assertEquals("127.0.0.1:8000", NetUtil.getPrimaryNodUrl());
    }

}