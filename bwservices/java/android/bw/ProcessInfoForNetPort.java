package android.bw;

/**
 * 进程信息，用于网络端口。
 * Created by asherli on 16/3/16.
 */
public class ProcessInfoForNetPort {

    public int uid;
    public int pid;
    public String packageName;

    public int port;
    public String processName;
    public boolean isApp;

    public ProcessInfoForNetPort(
            int uid, int pid, String packageName,
            int port, String processName, boolean isApp) {
        if (null == packageName) {
            throw new NullPointerException("packageName == null");
        }
        if (null == processName) {
            throw new NullPointerException("processName == null");
        }
        this.uid = uid;
        this.pid = pid;
        this.packageName = packageName;
        this.port = port;
        this.processName = processName;
        this.isApp = isApp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessInfoForNetPort that = (ProcessInfoForNetPort) o;

        return uid == that.uid && pid == that.pid && port == that.port && isApp == that.isApp &&
                packageName != null && packageName.equals(that.packageName) &&
                processName != null && processName.equals(that.processName);

    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + pid;
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (processName != null ? processName.hashCode() : 0);
        result = 31 * result + (isApp ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProcessInfoForNetPort{" +
                "uid=" + uid +
                ", pid=" + pid +
                ", packageName='" + packageName + '\'' +
                ", port=" + port +
                ", processName='" + processName + '\'' +
                ", isApp=" + isApp +
                '}';
    }
}
