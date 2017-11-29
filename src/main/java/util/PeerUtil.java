package util;

import entity.ValidatorAddress;

import java.util.List;

import static util.Const.ValidatorListFile;
import static util.JsonUtil.getValidatorAddressList;

/**
 * Created by chao on 2017/11/30.
 */
public class PeerUtil {
    private static List<ValidatorAddress> list = getValidatorAddressList(ValidatorListFile);
    private static int peerCount = 0;
    private static int faultCount = 0;
    static {
        peerCount = list.size();
        faultCount = (peerCount - 1) / 3;
    }

    public static List<ValidatorAddress> getList() {
        return list;
    }

    public static void setList(List<ValidatorAddress> list) {
        PeerUtil.list = list;
    }

    public static int getPeerCount() {
        return peerCount;
    }

    public static void setPeerCount(int peerCount) {
        PeerUtil.peerCount = peerCount;
    }

    public static int getFaultCount() {
        return faultCount;
    }

    public static void setFaultCount(int faultCount) {
        PeerUtil.faultCount = faultCount;
    }
}
