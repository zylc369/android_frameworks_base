package android.bw;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 这个类用仅于记录与"BW*"相关的操作，请不要广泛使用。
 * Created by asherli on 16/1/10.
 */
public class BWLog {

    public static final int MODE_QUIET = 0;
    public static final int MODE_PRINT = 1;
//    public static final int MODE_DUMP = 1 << 1;

    private static volatile int mode = MODE_PRINT;

//    private static BWLog bwLog = null;
//    private BufferedWriter writer = null;
//    private File dumpFile = null;

//    /**
//     * 每个进程只能有一个BWLog对象。
//     * 这是因为如果一个进程有多个BWLog对象，并且每个BWLog对象都
//     * 同时dump到一个文件中那么可能会乱。
//     * @return 返回BWLog对象。
//     */
//    public static BWLog getInstance() {
//        if (null == bwLog) {
//            synchronized (Object.class) {
//                if (null == bwLog) {
//                    bwLog = new BWLog();
//                }
//            }
//        }
//        return bwLog;
//    }

    private BWLog() {}

    /**
     * 设置为安静模式。
     * 在这个模式下将不再输出日志。
     */
    public static void setQuietMode() {
        mode = MODE_QUIET;
    }

    /**
     * 设置为打印输出模式。
     * 这个模式下将在logcat中输出日志。
     */
    public static void setPrintMode() {
        mode |= MODE_PRINT;
    }

    public static int d(String tag, String msg) {
        int result = -1;
        if (0 != (MODE_PRINT & mode)) {
            result = Log.d(tag, msg);
        }
        return result;
    }

    public static int i(String tag, String msg) {
        int result = -1;
        if (0 != (MODE_PRINT & mode)) {
            result = Log.i(tag, msg);
        }
        return result;
    }

    public static int w(String tag, String msg) {
        int result = -1;
        if (0 != (MODE_PRINT & mode)) {
            result = Log.w(tag, msg);
        }
        return result;
    }

    public static int e(String tag, String msg) {
        int result = -1;
        if (0 != (MODE_PRINT & mode)) {
            result = Log.e(tag, msg);
        }
        return result;
    }

    public static int e(String tag, Exception exception) {
        int result = -1;
        if (0 != (MODE_PRINT & mode)) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String message = sw.toString();
            try {
                sw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            pw.close();
            result = e(tag, message);
        }

        return result;
    }

//    /**
//     * 设置dump模式。
//     * @param mode 模式标志。在这个方法中无论mode参数是否设置了MODE_DUMP标志位都
//     *             默认被设置了MODE_DUMP标志位，所以当仅需要dump模式时可以输入0。
//     *             为什么还要输入模式参数哪？因为有时候可能还需要其他的模式，如打印模式。
//     * @param dumpFile dump文件。
//     * @throws IOException
//     */
//    public void setDumpMode(int mode, File dumpFile) throws IOException {
//        openOrCreateDumpFile(dumpFile);
//        this.mode = mode | MODE_DUMP;
//    }

//    public int d(String tag, String msg) {
//        int result = -1;
//        if (0 != (MODE_PRINT & mode)) {
//            result = Log.d(tag, msg);
//        }
//        if (0 != (MODE_PRINT & mode)) {
//            try {
//                dump("[DEBUG] " + msg);
//                result = 0;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return result;
//    }

//    private void dump(String content) throws IOException {
//        writer.write(content);
//        writer.newLine();
//        writer.flush();
//    }

    /**
     * 打开或创建dump文件。
     * @param dumpFile dump文件。当这个文件不存在时，将创建这个文件。
     * @throws IOException
     */
//    private void openOrCreateDumpFile(File dumpFile) throws IOException {
//        if (null == dumpFile) {
//            throw new InvalidParameterException("参数dumpFile为null。");
//        }
//        if (!dumpFile.exists()) {
//            if (!dumpFile.createNewFile()) {
//                throw new IOException("创建文件失败：" + dumpFile.getAbsolutePath() + "。");
//            }
//        }
//        // 当没有打开过文件，或当文件路径发生变化时，打开文件。
//        if (null == this.dumpFile || !dumpFile.getAbsolutePath().equals(this.dumpFile.getAbsolutePath())) {
//            if (null != this.writer) {
//                this.writer.close();
//                this.writer = null;
//            }
//            this.writer = new BufferedWriter(new FileWriter(dumpFile));
//            this.dumpFile = dumpFile;
//        }
//    }

    // 我不知道在finalize中关闭文件句柄到底对不对。
//    @Override
//    protected void finalize() throws Throwable {
//        if (null != writer) {
//            writer.close();
//        }
//        super.finalize();
//    }
}
