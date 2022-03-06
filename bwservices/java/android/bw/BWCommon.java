package android.bw;

public class BWCommon {

//    public static final boolean BWDEBUG = true;

    public static final String TAG;

    /**
     * 根目录。
     */
    public static final String BW_ROOT_DIR;

    /**
     * 数据库目录。
     */
    public static final String BW_DB_DIR;

    /**
     * 日志目录。
     */
    public static final String BW_LOG_DIR;

    /**
     * BW数据库路径。
     */
    public static final String BW_DB_PATH;

    static {
        TAG = "BWDebug";
        BW_ROOT_DIR = "/data/local/bw";
        BW_DB_DIR = "/data/local/bw/db";
        BW_LOG_DIR = "/data/local/bw/log";
        BW_DB_PATH = BW_DB_DIR + "/bwdb.db3";
    }

    /**
     * 获得时间戳。
     * @return 返回时间戳字符串。
     */
//    public static String getTimeStamp(){
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//        return simpleDateFormat.format(new Date());
//    }

}
