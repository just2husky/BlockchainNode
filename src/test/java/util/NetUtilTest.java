package util;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/18.
 */
public class NetUtilTest {
    @Test
    public void getPrimaryNode() throws Exception {
        Map<String, String> map = NetUtil.getPrimaryNode();
        assertEquals("127.0.0.1", map.get("ip"));
        assertEquals("8000", map.get("port"));
    }

    @Test
    public void getPrimaryNodUrl() throws Exception {
        assertEquals("127.0.0.1:8000", NetUtil.getPrimaryNodUrl());
    }

}