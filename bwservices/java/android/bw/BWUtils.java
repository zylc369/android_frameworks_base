package android.bw;

import android.bw.db.base.TraceMethodInfoBase;
import android.bw.service.IBWService;
import android.bw.socket.BWAppAnalysisControlServer;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dalvik.system.DexClassLoader;

/**
 * BW工具类。
 * Created by asherli on 16/1/11.
 */
public class BWUtils {

    /**
     * 类描述符转换为类名。
     * @param classDesc 类描述符。
     * @return 转换成功，则返回类名；转换失败，则返回null。
     */
    public static String classDescToClassName(String classDesc) {
        int classDescLength = classDesc.length();
        if (classDescLength < 3) {
            return null;
        }
        if ('L' != classDesc.charAt(0) || ';' != classDesc.charAt(classDescLength - 1)) {
            return null;
        }
        String tmp = classDesc.substring(1, classDescLength - 1);
        return tmp.replaceAll("/", ".");
    }

    /**
     * 根据UID获得包名。
     * @param uid uid。
     * @return 获得成功，则返回包名；获得失败，则返回null；
     */
    public static String getPackageNameByUID(int uid) {
        String packageName = null;
        IBWService ibwService = getBWService();
        try {
            // ibwService是有可能等于null的。
            if (null != ibwService) {
                packageName = ibwService.getPackageNameByUID(uid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return packageName;
    }

    /**
     * 初始化一个App进程的跟踪信息。
     * @param packageName App的包名。
     */
    public static void initAppProcessTraceMethodInfo(String packageName) {
        // BWLog.i(BWCommon.TAG, "initAppProcessTraceMethodInfo - 1");
        IBWService ibwService = getBWService();
        // BWLog.i(BWCommon.TAG, "initAppProcessTraceMethodInfo - 2");
        try {
            // ibwService是有可能等于null的。
            if (null != ibwService) {
                // BWLog.i(BWCommon.TAG, "initAppProcessTraceMethodInfo - 3");
                List<TraceMethodInfoBase> traceMethodInfoBases = ibwService.queryAllTraceMethodInfo(packageName);
                // BWLog.i(BWCommon.TAG, "initAppProcessTraceMethodInfo - 4");
                for (TraceMethodInfoBase traceMethodInfoBase : traceMethodInfoBases) {
                    nativeInsertOrUpdateTraceMethodInfo(traceMethodInfoBase);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static long getMethodID(Class clazz, String methodName, String methodSig, boolean isStatic) {
        return nativeGetMethodID(clazz, methodName, methodSig, isStatic);
    }

    public static long getMethodID(DexClassLoader dexClassLoader,
            String className, String methodName, String methodSig, boolean isStatic) throws ClassNotFoundException {
        Class clazz = dexClassLoader.loadClass(className);
        return getMethodID(clazz, methodName, methodSig, isStatic);
    }

    /**
     * 获得BW日志标志。
     * @param packageName 包名。
     * @return 获得成功，则返回bw标志；否则，返回-1。
     */
    public static int getBWDumpFlags(String packageName) {
        int result = -1;
        IBWService ibwService = getBWService();
        try {
            // ibwService是有可能等于null的。
            if (null != ibwService) {
                result = ibwService.getBWDumpFlags(packageName);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获得bw service。
     * @return 获得成功，则返回bw service；否则，返回null。
     */
    public static IBWService getBWService() {
        IBWService ibwService = null;
        try {
            IBinder iBinder = ServiceManager.getService("bwservice");
            if (null == iBinder) {
                BWLog.e(BWCommon.TAG, "[-] BWUtils.getBWService - 获得bwservice失败！");
            } else {
                ibwService = IBWService.Stub.asInterface(iBinder);
            }
        } catch (Exception e) {
            ibwService = null;
            e.printStackTrace();
        }
        return ibwService;
    }

    /**
     * 获得时间
     * @return 返回时间。
     */
    public static String getTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINESE);
        return simpleDateFormat.format(new Date());
    }

    public static boolean startBWAppAnalysisControlServer() {
        return BWAppAnalysisControlServer.startServer();
    }

    public static boolean needsInterpreter(int uid) {
        IBWService ibwService = getBWService();
        if (null == ibwService) {
            BWLog.e(BWCommon.TAG, "[-] BWUtils.needsInterpreter - 获得bwservice失败！");
            return false;
        }
        try {
            return ibwService.needsInterpreter(uid);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean needsRoot(int uid) {
        IBWService ibwService = getBWService();
        if (null == ibwService) {
            BWLog.e(BWCommon.TAG, "[-] BWUtils.needsRoot - 获得bwservice失败！");
            return false;
        }
        try {
            return ibwService.needsRoot(uid);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static native boolean nativeInsertOrUpdateTraceMethodInfo(TraceMethodInfoBase traceMethodInfoBase);
    // TODO: 这个本地函数好像还没有实现：1.要验证一下谁调用了它；2.实现这个函数。
    private static native long nativeGetMethodID(Class javaClass, String methodName, String methodSig, boolean isStatic);

    public native static String nativeGetProcessName(int pid);

}
