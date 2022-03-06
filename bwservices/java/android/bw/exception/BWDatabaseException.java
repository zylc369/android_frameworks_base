package android.bw.exception;

/**
 * BW数据库异常。
 * Created by 不歪 on 16/7/25.
 */
public class BWDatabaseException extends Exception {

    public BWDatabaseException() {
    }

    public BWDatabaseException(String detailMessage) {
        super(detailMessage);
    }

    public BWDatabaseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BWDatabaseException(Throwable throwable) {
        super(throwable);
    }
}
