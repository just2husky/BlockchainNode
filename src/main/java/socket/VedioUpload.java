package socket;

import entity.Transaction;
import entity.Vedio;
import service.TransactionService;
import util.Const;
import util.RabbitmqUtil;
import util.TimeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by chao on 2018/4/10.
 */
public class VedioUpload {
    public static String getMD5Three(String path) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = new File(path);
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi.toString(16);
    }

    public static void main(String[] args) {
        RabbitmqUtil rmq = new RabbitmqUtil(Const.TX_QUEUE);
        String vedioHash = getMD5Three("C:\\Users\\chao\\Pictures\\天天向上.mp4");
        Vedio vedio = new Vedio("天天向上20180410", TimeUtil.getNowTimeStamp(), vedioHash, "湖南卫视");
        System.out.println("视频信息为：" + vedio + "\n");
        try {
            Transaction tx = TransactionService.genTx("string", vedio.toString());
            System.out.println("将视频保存在交易单：" + tx.getTxId() + "中\n");
            System.out.println(tx.getTxId() + " 的详细信息为：" + tx + "\n");
            System.out.println("开始将 " + tx.getTxId() + " 发送到交易单消息队列上\n");
            rmq.push(tx.toString());
            System.out.println("发送 " + tx.getTxId() + " 成功\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
