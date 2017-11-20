package util;

/**
 * Created by chao on 2017/11/17.
 */
public class TimeUtil {
    public static String getNowTimeStamp() {
        return System.currentTimeMillis() + "";
    }

    public static void main(String[] args) {
        System.out.println(getNowTimeStamp());
    }
}
