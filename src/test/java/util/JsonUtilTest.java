package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chao on 2017/12/10.
 */
public class JsonUtilTest {
    private final static ObjectMapper objMapper = new ObjectMapper();
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