package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entity.ValidatorAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static util.Const.ValidatorListFile;

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
     * 从指定路径读取json文件，解析后返回对象 list
     *
     * @return
     */
    public static List<ValidatorAddress> getValidatorAddressList(String jsonFile) {
        String jsonStr = getStrByJsonFile(jsonFile);
        return str2list(jsonStr, ValidatorAddress.class);
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


    public static void main(String[] args) {
        String jsonFile = ValidatorListFile;

        // 1. 从指定路径读取json文件，解析后返回json字符串
        logger.info(getStrByJsonFile(jsonFile));

        // 2.
        List<ValidatorAddress> list = getValidatorAddressList(jsonFile);

        for (ValidatorAddress validatorAddress : list) {
            logger.info(validatorAddress.toString());
        }
    }

}
