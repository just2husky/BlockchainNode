package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.NetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 保存和 json 相关的操作
 * Created by chao on 2017/11/9.
 */
public class JsonUtil {

    private final static ObjectMapper objMapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * 将字符串转list对象
     *
     * @param <T>
     * @param jsonStr
     * @param cls
     * @return
     */
    public static <T> List<T> str2list(String jsonStr, Class<T> cls) {
        ObjectMapper mapper = new ObjectMapper();
        List<T> objList = null;
        try {
            JavaType t = mapper.getTypeFactory().constructParametricType(
                    List.class, cls);
            objList = mapper.readValue(jsonStr, t);
        } catch (Exception e) {
        }
        return objList;
    }

    public static Map jsonToMap(String jsonStr) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;
        JavaType javaType = mapper.getTypeFactory().constructParametricType(HashMap.class, String.class, Object.class);
        try {
            map = mapper.readValue(jsonStr, javaType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }


    /**
     * 从指定路径读取json文件，解析后返回json字符串
     *
     * @return
     */
    public static String getStrByJsonFile(String jsonFile) {
        String strResult = "";

        try {
            JsonNode rootNode = objMapper.readTree(new File(jsonFile));

            // 获得 json 字符串
            strResult = rootNode.toString();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strResult;
    }

    /**
     * 从指定路径读取json文件，解析后返回 ValidatorAddress list
     *
     * @return
     */
    public static List<NetAddress> getValidatorAddressList(String jsonFile) {
        String jsonStr = getStrByJsonFile(jsonFile);
        Map map = jsonToMap(jsonStr);
        //noinspection unchecked
        List<Map> list = (List<Map>) map.get("validators");
        List<NetAddress> addrList = new ArrayList<NetAddress>();
        for(Map tmpMap : list) {
            addrList.add(new NetAddress((String)tmpMap.get("ip"), (Integer) tmpMap.get("port")));
        }
        return addrList;
    }

    /**
     * 获取 Publisher 的地址
     * @param jsonFile
     * @return
     */
    public static NetAddress getPublisherAddress(String jsonFile) {
        String jsonStr = getStrByJsonFile(jsonFile);
        Map map = jsonToMap(jsonStr);
        Map pubMap = (HashMap) map.get("publisher");
        return new NetAddress((String)pubMap.get("ip"), (Integer) pubMap.get("port"));
    }
    /**
     * 判断json字符串是否是一个list
     *
     * @param jsonStr
     * @return
     */
    public static boolean isList(String jsonStr) {
        try {
            if (objMapper.writeValueAsString(jsonStr).substring(1, 2).equals("[")) {
                return true;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取泛型的Collection Type
     * @param collectionClass 泛型的Collection
     * @param elementClasses 元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static String writeValueAsString(Object object) {
        try {
            return objMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {
        String jsonFile = Const.BlockChainNodesFile;

        // 1. 从指定路径读取json文件，解析后返回json字符串
        logger.info(getStrByJsonFile(jsonFile));

        // 2.
        List<NetAddress> list = getValidatorAddressList(jsonFile);

        for (NetAddress netAddress : list) {
            logger.info(netAddress.toString());
        }
    }

}
