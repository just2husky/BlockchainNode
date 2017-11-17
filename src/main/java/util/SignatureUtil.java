package util;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * Created by chao on 2017/11/17.
 */
public class SignatureUtil {
    /**
     * 对data进行sha256哈希运算，编码为16进制
     * @param data
     * @return
     */
    public static String getSha256Hex(String data) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(data);
    }

    /**
     * 根据使用的算法生成对应的密钥
     * @param algorithm https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyPairGenerator
     * @return
     */
    public static KeyPair genKeyPair(String algorithm) {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert keyPairGenerator != null;
        keyPairGenerator.initialize(256);
        return keyPairGenerator.generateKeyPair();
//        ECPublicKey ecPublicKey = (ECPublicKey)keyPair.getPublic();
//        ECPrivateKey ecPrivateKey = (ECPrivateKey)keyPair.getPrivate();
    }

    public static void saveKey(KeyPair keyPair){
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        String pubKeyStr = Base64.encodeBase64String(publicKey.getEncoded());
        String pvtKeyStr = Base64.encodeBase64String(privateKey.getEncoded());
        try {
            saveFile("publicKey.txt", pubKeyStr);
            saveFile("privateKey.txt", pvtKeyStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中读取私钥并返回 PrivateKey 对象
     * @param algorithm
     * @return
     */
    public static PrivateKey loadPvtKey(String algorithm) {
        PrivateKey privateKey = null;
        try {
            String pvtKeyStr = readFile("privateKey.txt");

            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            PKCS8EncodedKeySpec pvtSpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(pvtKeyStr));
            privateKey = keyFactory.generatePrivate(pvtSpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 从文件中读取私钥并返回字符串
     * @param algorithm
     * @return
     */
    public static String loadPvtKeyStr(String algorithm) {
        String pvtKeyStr= null;
        try {
            pvtKeyStr = readFile("privateKey.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pvtKeyStr;
    }

    /**
     * 从文件中读取公钥并返回 PublicKey 对象
     * @param algorithm
     * @return
     */
    public static PublicKey loadPubKey(String algorithm) {
        PublicKey publicKey = null;
        try {
            String pubKeyStr = readFile("publicKey.txt");
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.decodeBase64(pubKeyStr));
            publicKey = keyFactory.generatePublic(pubSpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    /**
     * 从文件中读取私钥并返回字符串
     * @param algorithm
     * @return
     */
    public static String loadPubKeyStr(String algorithm) {
        String pubKeyStr= null;
        try {
            pubKeyStr = readFile("publicKey.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pubKeyStr;
    }

    public static void saveFile(String path,String key)throws Exception{
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(key.getBytes());
        fos.flush();
        fos.close();
    }

    /**
     * 读文件，并返回字符串
     * @param path
     * @return
     * @throws Exception
     */
    public static String readFile(String path)throws Exception{

        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(path), "utf-8");
        BufferedReader reader = new BufferedReader(inputStreamReader);
        // 一次读入一行，直到读入null为文件结束
        StringBuilder sb = new StringBuilder();
        String tempString = null;
        while ((tempString = reader.readLine()) != null) {
            sb.append(tempString);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        KeyPair keyPair = genKeyPair("EC");
        saveKey(keyPair);
        System.out.println(loadPubKeyStr("EC"));
        System.out.println(loadPvtKeyStr("EC"));
}
}