package android.bw;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class WriteBWLog {

    private static final File logFile = new File(BWCommon.BW_ROOT_DIR, "bwlog");
    private static FileWriter out = null;

    /**
     * 初始化文件。
     * @return
     */
    private static boolean initFile() {
        boolean result = false;

        // 文件不存在则创建文件。
        if (!logFile.exists()) {
            synchronized (Object.class) {
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 文件存在则打开一个写文件器。
        if (logFile.exists()) {
            if (null == out) {
                synchronized (Object.class) {
                    if (null == out) {
                        try {
                            // 构造函数中的第二个参数true表示以追加形式写文件
                            out = new FileWriter(logFile, true);
                            result = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * 写文件
     * @param tag 标签。
     * @param content 写入内容。
     * @return
     */
    private static boolean write(String tag, String content) {
        boolean result = false;
        if (initFile()) {
            StringBuilder str = new StringBuilder();
            // 时间戳。
            str.append(BWUtils.getTime());
            str.append("    ");
            // PID与TID。
            str.append(Integer.toString(android.os.Process.myPid()));
            str.append("-");
            str.append(Integer.toString(android.os.Process.myTid()));
            str.append(" ");
            // 标签。
            str.append(tag);
            str.append(": ");
            // 内容。
            str.append(content);
            str.append(System.getProperty("line.separator"));

            // 同步写文件。
            synchronized (Object.class) {
                try {
                    out.write(content);
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public static boolean d(String content) {
        return write("[DEBUG]", content);
    }

    public static boolean i(String content) {
        return write("[INFO]", content);
    }

    public static boolean w(String content) {
        return write("[WARN]", content);
    }

    public static boolean e(String content) {
        return write("[ERROR]", content);
    }

    public static boolean exp(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String message = sw.toString();
        try {
            sw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        pw.close();
        return e(message);
    }

}
