package util;

/**
 * Created by chao on 2017/11/9.
 */
public class Const {
    public final static String ValidatorListFile =
            "src/main/resources/validator-list.json";
    public final static String CM = "ClientMsg";
    public final static String PPM = "PrePrepareMsg";
    public final static String PM = "PrepareMsg";
    public final static String PDM = "PreparedMsg";
    public final static String CMTM = "CommitMsg";
    public final static String CMTDM = "CommittedMsg";
    public final static String BM = "BlockMsg";

    public final static String QUEUE_NAME = "TxQueue";
    public final static String CHAR_SET = "UTF-8";
    public final static String HASH_ALG = "SHA-256";

    public final static long SLEEP_TIME = 60000;
}
