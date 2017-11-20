package util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
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
    public static String getSha256Base64(String data) {
        return Base64.encodeBase64String(DigestUtils.sha256(data));
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

    /**
     * 根据传入的 privateKey 对 content 进行数字签名，并以字符串的形式返回
     * @param privateKey
     * @param content
     * @return 数字签名
     */
    public static String sign(PrivateKey privateKey, String content) {
        Signature signature = null;
        String rtn = null;
        try {
            signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(content.getBytes());
            byte[] res = signature.sign();
            rtn = Base64.encodeBase64String(res);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return rtn;
    }

    /**
     * 根据 公钥 publicKey， 被签名的内容 content， 和签名 sig 来验证数字签名
     * @param publicKey
     * @param content
     * @param sig
     * @return boolean型，成功返回 true
     */
    public static boolean verify(PublicKey publicKey, String content, String sig) {
        Signature signature = null;
        boolean rtn = false;
        try {
            signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(content.getBytes());
            rtn = signature.verify(Base64.decodeBase64(sig));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public static void main(String[] args) {
        KeyPair keyPair = genKeyPair("EC");
        saveKey(keyPair);
        System.out.println(loadPubKeyStr("EC"));
        System.out.println(loadPvtKeyStr("EC"));

        String rtn = sign(loadPvtKey("EC"), "测试");
        if(rtn != null) {
            System.out.println("签名成功");
            System.out.println("签数字签名为：" + rtn);
        }
        else {
            System.out.println("rtn 值为null");
        }

        boolean verify_res = verify(loadPubKey("EC"), "测试", rtn);
        if (verify_res) {
            System.out.println("验证成功");
        } else {
            System.out.println("验证失败");
        }
}
}