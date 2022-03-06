package android.bw.db.helper;

import android.bw.db.base.AppIDBase;
import android.bw.db.base.MethodIDBase;
import android.bw.db.base.MethodLocation;
import android.bw.db.base.TraceMethodInfoBase;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * TraceMethodInfoBase类的帮助函数。
 * Created by asherli on 16/2/18.
 */
public class TraceMethodInfoBaseHelper {

    public static TraceMethodInfoBase traceInstruction(Context context,
                                                       String packageName, String classDesc, String methodName, String methodSig) {
        return traceInstruction(context, packageName, classDesc, methodName,
                methodSig, false);
    }

    /**
     * 创建TraceMethodInfoBase对象，用以跟踪指令。
     * @param context 上下文。
     * @param packageName 包名。
     * @param classDesc 类描述符。
     * @param methodName 方法名。
     * @param methodSig 方法签名。
     * @param isTransitive 是否传递。
     * @return 创建成功，则返回TraceMethodInfoBase对象；创建失败，则返回null。
     */
    public static TraceMethodInfoBase traceInstruction(Context context,
           String packageName, String classDesc, String methodName, String methodSig,
           boolean isTransitive) {
        TraceMethodInfoBase traceMethodInfoBase = null;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);

            traceMethodInfoBase = new TraceMethodInfoBase();
            AppIDBase appIDBase = new AppIDBase(ai.uid, packageName,
                    AppIDBase.APP_TYPE_ANDROID_APP,
                    AppIDBase.START_FLAG_UNSET);
            MethodIDBase methodIDBase = new MethodIDBase(classDesc, methodName, methodSig);
            traceMethodInfoBase.methodLocation = new MethodLocation(appIDBase, methodIDBase);

            traceMethodInfoBase.traceMethodFlags = TraceMethodInfoBase.TRACE_METHOD_INFO_FLAG_ENABLE;
            if (isTransitive) {
                traceMethodInfoBase.traceMethodFlags |= TraceMethodInfoBase.TRACE_METHOD_INFO_FLAG_TRANSITIVE;
            }
            traceMethodInfoBase.granularity = TraceMethodInfoBase.GRANULARITY_LEVEL_INSTRUCTION;
            traceMethodInfoBase.promptMethodType = TraceMethodInfoBase.PROMPT_METHOD_TYPE_ARY;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return traceMethodInfoBase;
    }

    /**
     * 方法提示。
     * @param context 上下文。
     * @param packageName 包名。
     * @param classDesc 类描述符。
     * @param methodName 方法名。
     * @param methodSig 方法签名。
     * @param isTransitive 是否传递。
     * @return 创建成功，则返回TraceMethodInfoBase对象；创建失败，则返回null。
     */
    public static TraceMethodInfoBase methodPrompt(Context context,
            String packageName, String classDesc, String methodName, String methodSig, boolean isTransitive) {
        TraceMethodInfoBase traceMethodInfoBase = null;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);

            traceMethodInfoBase = new TraceMethodInfoBase();
            AppIDBase appIDBase = new AppIDBase(ai.uid, packageName,
                    AppIDBase.APP_TYPE_ANDROID_APP, AppIDBase.START_FLAG_UNSET);
            MethodIDBase methodIDBase = new MethodIDBase(classDesc, methodName, methodSig);
            traceMethodInfoBase.methodLocation = new MethodLocation(appIDBase, methodIDBase);

            traceMethodInfoBase.traceMethodFlags = TraceMethodInfoBase.TRACE_METHOD_INFO_FLAG_ENABLE;
            if (isTransitive) {
                traceMethodInfoBase.traceMethodFlags |= TraceMethodInfoBase.TRACE_METHOD_INFO_FLAG_TRANSITIVE;
            }

            traceMethodInfoBase.granularity = TraceMethodInfoBase.GRANULARITY_LEVEL_METHOD;
            traceMethodInfoBase.promptMethodType = TraceMethodInfoBase.PROMPT_METHOD_TYPE_ARY;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return traceMethodInfoBase;
    }

    /**
     * 打印跟踪的方法的调用堆栈。
     * @param context 上下文。
     * @param packageName 包名。
     * @param classDesc 类描述符。
     * @param methodName 方法名。
     * @param methodSig 方法签名。
     * @return 创建成功，则返回TraceMethodInfoBase对象；创建失败，则返回null。
     */
    public static TraceMethodInfoBase traceMethodPrintCallStack(Context context,
        String packageName, String classDesc, String methodName, String methodSig) {
        TraceMethodInfoBase traceMethodInfoBase = null;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);

            traceMethodInfoBase = new TraceMethodInfoBase();
            AppIDBase appIDBase = new AppIDBase(ai.uid, packageName,
                    AppIDBase.APP_TYPE_ANDROID_APP, AppIDBase.START_FLAG_UNSET);
            MethodIDBase methodIDBase = new MethodIDBase(classDesc, methodName, methodSig);
            traceMethodInfoBase.methodLocation = new MethodLocation(appIDBase, methodIDBase);

            traceMethodInfoBase.traceMethodFlags = TraceMethodInfoBase.TRACE_METHOD_INFO_FLAG_ENABLE;
            traceMethodInfoBase.traceMethodFlags |= TraceMethodInfoBase.TRACE_METHOD_INFO_FLAG_PRINTCALLSTACK;

            traceMethodInfoBase.granularity = TraceMethodInfoBase.GRANULARITY_LEVEL_METHOD;
            traceMethodInfoBase.promptMethodType = TraceMethodInfoBase.PROMPT_METHOD_TYPE_ARY;
        } catch (PackageManager.NameNotFoundException e) {
            traceMethodInfoBase = null;
            e.printStackTrace();
        }
        return traceMethodInfoBase;
    }

}
