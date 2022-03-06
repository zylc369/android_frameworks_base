package com.android.server.bw;

import android.bw.BWCommon;
import android.bw.BWLog;
import android.bw.ProcessInfoForNetPort;
import android.bw.db.BWDatabase;
import android.bw.db.base.AppIDBase;
import android.bw.db.base.BWDumpBase;
import android.bw.db.base.HookMethodInstInfoBase;
import android.bw.db.base.TraceMethodInfoBase;
import android.bw.exception.BWDatabaseException;
import android.bw.service.IBWService;
import android.bw.socket.BWAppAnalysisControlClient;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import com.android.server.SystemServer;
import com.android.server.pm.PackageManagerService;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 写BWService的代码时，参照了系统service的代码。
 * 如：ActivityManagerService、PackageManagerService。
 * Created by buwai on 15/11/11.
 */
// TODO: 操作APP ContentProvider时，判断一下该APP是否启动。
public class BWService extends IBWService.Stub {

    public static final String SERVICE_NAME = "bwservice";

    private static BWService mBWService = null;
    private Context mContext = null;
    private SystemServer mSystemServer = null;
    private BWDatabase mBWDatabase = null;
    private boolean isInitSuccess = false;
    private BWAppAnalysisControlClientSet bwAppAnalysisControlClientSet;

    public static BWService getInstance(Context systemContext) {
        if (null == mBWService) {
            synchronized (Object.class) {
                if (null == mBWService) {
                    mBWService = new BWService(systemContext);
                }
            }
        }
        return mBWService;
    }

    private BWService(Context systemContext) {
        isInitSuccess = false;
        mContext = systemContext;
        // 判断根目录是否存在。
        File rootDir = new File(BWCommon.BW_ROOT_DIR);
        if (!rootDir.exists()) {
            BWLog.e(BWCommon.TAG, "目录不存在：" + BWCommon.BW_ROOT_DIR);
            return;
        }

        // 判断配置文件是否存在。
        File file = new File(rootDir, "app-config.xml");
        if (!file.exists()) {
            boolean isCreateNewFileSuccess = false;
            try {
                // 如果不存在，则创建一个文件。
                if (!file.createNewFile()) {
                    BWLog.e(BWCommon.TAG, "文件创建失败：" + file.getAbsolutePath());
                } else {
                    isCreateNewFileSuccess = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                BWLog.e(BWCommon.TAG, e);
            }

            if (!isCreateNewFileSuccess) {
                return;
            }

            // 如果依旧不存在，则打印Log并返回。
            if (!file.exists()) {
                BWLog.e(BWCommon.TAG, "文件不存在：" + file.getAbsolutePath());
                return;
            }
        }

        mBWDatabase = new BWDatabase();

        bwAppAnalysisControlClientSet = new BWAppAnalysisControlClientSet(this);
        isInitSuccess = true;
    }

    /**
     * 在SystemServer类中调用这个方法。
     * @param systemServer SystemServer类对象。
     */
    public void setSystemServer(SystemServer systemServer) {
        this.mSystemServer = systemServer;
    }

    /**
     * 用来判断初始化是否成功。
     * @return 初始化成功，则返回true；初始化失败，则返回false。
     */
    @Override
    public boolean isInitSuccess() {
        return this.isInitSuccess;
    }

    /**
     * 根据UID获得包名。
     * @param uid uid。
     * @return 获得成功，则返回包名；获得失败，则返回null；
     * @throws RemoteException
     */
    @Override
    public String getPackageNameByUID(int uid) throws RemoteException {
        PackageManagerService packageManagerService = mSystemServer.getPackageManagerService();
        return packageManagerService.getPackageNameByUID(uid);
    }

    /**
     * 插入或更新TraceMethodInfoBase。
     * @param traceMethodInfoBase TraceMethodInfoBase对象。
     * @return 执行成功（仅对APP发送数据成功时就认为执行成功，对数据库插入失败则不认为执行失败），则返回true；
     *         执行失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean insertOrUpdateTraceMethodInfo(TraceMethodInfoBase traceMethodInfoBase) throws RemoteException {
        if (!bwAppAnalysisControlClientSet.insertOrUpdateTraceMethodInfo(traceMethodInfoBase)) {
            BWLog.w(BWCommon.TAG, "[!] insertOrUpdateTraceMethodInfo - 向应用中插入数据失败。");
        }

        if (!mBWDatabase.insertOrUpdateTraceMethodInfo(traceMethodInfoBase)) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateTraceMethodInfo - " +
                    "向数据库中插入TraceMethodInfoBase失败，不对插入APP的数据进行回滚操作，" +
                    "方法将返回true。App包名：" +
                    traceMethodInfoBase.methodLocation.appIDBase.getPackageName() + "。");
            return false;
        }
        return true;

    }

    /**
     * 删除TraceMethodInfo数据。
     * @param packageName App包名。
     * @param hashCode TraceMethodInfo对应的方法哈希。
     * @return 执行成功（仅对APP中的数据删除成功时就认为执行成功，对数据库删除数据失败则不认为执行失败），则返回true；
     *         执行失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean deleteTraceMethodInfo(String packageName, int hashCode) throws RemoteException {
        if (!bwAppAnalysisControlClientSet.deleteTraceMethodInfo(packageName, hashCode)) {
            BWLog.e(BWCommon.TAG, "[-] deleteTraceMethodInfo(String,int) - " +
                    "APP中删除TraceMethodInfoBase失败。App包名：" + packageName + "。");
            return false;
        }

        // 数据库删除。
        if (!mBWDatabase.deleteTraceMethodInfo(packageName, hashCode)) {
            BWLog.e(BWCommon.TAG, "[-] deleteTraceMethodInfo(String,int) - " +
                    "数据库中删除TraceMethodInfoBase失败，不对APP删除的数据进行回滚操作，方法将返回true。" +
                    "App包名：" + packageName + "。");
        }
        return true;
    }

    @Override
    public TraceMethodInfoBase queryTraceMethodInfo(String packageName, int hashCode) throws RemoteException {
        // 数据库查询。
        TraceMethodInfoBase tmieDatabase = mBWDatabase.queryTraceMethodInfo(packageName, hashCode);
        if (null == tmieDatabase) {
            BWLog.w(BWCommon.TAG, "[!] queryTraceMethodInfo(String,int) - " +
                    "从数据库中查询TraceMethodInfoBase失败！");
            return null;
        }

        // App中查询。
        TraceMethodInfoBase fromApp = bwAppAnalysisControlClientSet.queryTraceMethodInfo(packageName, hashCode);
        if (null == fromApp) {
            BWLog.w(BWCommon.TAG, "[!] queryTraceMethodInfo - 从APP中查询方法跟踪信息失败。包名：" + packageName);
            return null;
        }

        if (fromApp.equals(tmieDatabase)) {
            return tmieDatabase;
        } else {
            BWLog.e(BWCommon.TAG, "[-] queryTraceMethodInfo - 数据库中的数据与App中的数据不同！" +
                    "fromApp=" + fromApp + ", tmieDatabase=" + tmieDatabase);
            return null;
        }
    }

    @Override
    public List<TraceMethodInfoBase> queryAllTraceMethodInfo(String packageName) throws RemoteException {
        return mBWDatabase.queryAllTraceMethodInfo(packageName);
    }

    /**
     * 插入或更新HookMethodInstInfoBase。
     * @param hookMethodInstInfoBase HookMethodInstInfoBase对象。
     * @return 执行成功（仅对APP发送数据成功时就认为执行成功，对数据库插入失败则不认为执行失败），则返回true；
     *         执行失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase hookMethodInstInfoBase) throws RemoteException {
        if (!bwAppAnalysisControlClientSet.insertOrUpdateHookMethodInstInfo(hookMethodInstInfoBase)) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase) - " +
                    "向APP中插入HookMethodInstInfoBase失败。App包名：" +
                    hookMethodInstInfoBase.instructionLocation.appIDBase.getPackageName() + "。");
            return false;
        }
        // 数据库插入数据。
        if (!mBWDatabase.insertOrUpdateHookMethodInstInfo(hookMethodInstInfoBase)) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase) - " +
                    "向数据库中插入HookMethodInstInfoBase失败，不对插入APP的数据进行回滚操作，" +
                    "方法将返回true。App包名：" + hookMethodInstInfoBase.instructionLocation.appIDBase.getPackageName() + "。");
        }
        return true;
    }

    /**
     * 删除HookMethodInstInfoBase数据。
     * @param packageName App包名。
     * @param hashCode HookMethodInstInfoBase对应的方法哈希。
     * @param instLineNum 被hook的指令行号。
     * @return 执行成功（仅对APP中的数据删除成功时就认为执行成功，对数据库删除数据失败则不认为执行失败），则返回true；
     *         执行失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean deleteHookMethodInstInfo(String packageName, int hashCode, long instLineNum) throws RemoteException {
        if (!bwAppAnalysisControlClientSet.deleteHookMethodInstInfo(packageName, hashCode, instLineNum)) {
            BWLog.e(BWCommon.TAG, "[-] deleteHookMethodInstInfo(String,int,long) - " +
                    "APP中删除HookMethodInstInfoBase失败。App包名：" + packageName + "。");
            return false;
        }
        // 数据库删除。
        if (!mBWDatabase.deleteHookMethodInstInfo(packageName, hashCode, instLineNum)) {
            BWLog.e(BWCommon.TAG, "[-] deleteHookMethodInstInfo(String,int,long) - " +
                    "数据库中删除HookMethodInstInfoBase失败，不对APP删除的数据进行回滚操作，" +
                    "方法将返回true。App包名：" + packageName + "。");
        }
        return true;
    }

    @Override
    public boolean deleteHookMethodInstInfoInMethod(String packageName, int hashCode) throws RemoteException {
        return false;
    }

    @Override
    public boolean deleteHookMethodInstInfoInPackage(String packageName) throws RemoteException {
        return false;
    }

    @Override
    public HookMethodInstInfoBase queryHookMethodInstInfo(
            String packageName, int hashCode, long instLineNum) throws RemoteException {
        // 数据库查询。
        HookMethodInstInfoBase base = mBWDatabase.queryHookMethodInstInfo(packageName, hashCode, instLineNum);
        if (null == base) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String,int,long) - " +
                    "从数据库中查询HookMethodInstInfoBase失败！");
            return null;
        }

        HookMethodInstInfoBase fromApp = bwAppAnalysisControlClientSet.queryHookMethodInstInfo(
                packageName, hashCode, instLineNum);
        if (null == fromApp) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String,int,long) - " +
                    "App游标转换到HookMethodInstInfoBase对象失败！");
            return null;
        }

        if (-1 == base.instructionLocation.dexPC && -1 != fromApp.instructionLocation.dexPC) {
            base.instructionLocation.dexPC = fromApp.instructionLocation.dexPC;
            // TODO: 更新数据库中的pc。
        }
        if (!fromApp.equals(base)) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String,int,long) - " +
                    "数据库中的数据与App中的数据不同！fromApp=" + fromApp.toString() + ", base=" + base.toString());
            return null;
        }
        return fromApp;
    }

    @Override
    public List<HookMethodInstInfoBase> queryHookMethodInstInfoInMethod(String packageName, int hashCode) throws RemoteException {
        boolean isQueryDatabaseSuccess = false;
        boolean isQueryAppSuccess = false;

        // 数据库查询。
        List<HookMethodInstInfoBase> list = mBWDatabase.queryHookMethodInstInfo(packageName, hashCode);
        if (null == list || 0 == list.size()) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInMethod(String,int) - " +
                    "从数据库中查询HookMethodInstInfoBase失败");
        } else {
            isQueryDatabaseSuccess = true;
        }

        // App中查询。
        List<HookMethodInstInfoBase> fromApp = bwAppAnalysisControlClientSet.queryHookMethodInstInfoInMethod(
                packageName, hashCode);
        if (null == fromApp || 0 == fromApp.size()) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInMethod(String,int) - " +
                    "从App中查询HookMethodInstInfoBase失败");
        } else {
            isQueryAppSuccess = true;
        }

        if (!isQueryDatabaseSuccess && !isQueryAppSuccess) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInMethod(String,int) - " +
                    "数据库查询和App查询均失败了。");
            return null;
        }
        if ( (isQueryDatabaseSuccess && !isQueryAppSuccess) || (!isQueryDatabaseSuccess && isQueryAppSuccess) ) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInMethod(String,int) - " +
                    "数据库查询和App查询，一个成功了，一个没有成功，这是异常情况！");
            return isQueryDatabaseSuccess ? list : fromApp;
        }

        int sizeFromDB = list.size();
        int sizeFromApp = fromApp.size();
        if (sizeFromDB != sizeFromApp) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInMethod(String,int) - " +
                    "数据库中的数据与App中的数据不同！sizeFromDB=" + sizeFromDB + ", sizeFromApp=" + sizeFromApp);
            return fromApp;
        }
        for (int i = 0; i < sizeFromDB; i++) {
            HookMethodInstInfoBase db = list.get(i);
            HookMethodInstInfoBase app = fromApp.get(i);
            if (-1 == db.instructionLocation.dexPC && -1 != app.instructionLocation.dexPC) {
                db.instructionLocation.dexPC = app.instructionLocation.dexPC;
                // TODO: 更新数据库中的pc。
            }
            if (!db.equals(app)) {
                BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInMethod(String,int) - " +
                        "数据库中的数据与App中的数据不同！db=" + db.toString() + ", app=" + app.toString());
                break;
            }
        }
        return fromApp;
    }

    @Override
    public List<HookMethodInstInfoBase> queryHookMethodInstInfoInPackage(String packageName) throws RemoteException {
        boolean isQueryAppSuccess = false;

        // 数据库查询。
        List<HookMethodInstInfoBase> list = mBWDatabase.queryHookMethodInstInfo(packageName);
        if (null == list || 0 == list.size()) {
            return null;
        }

        List<HookMethodInstInfoBase> fromApp = bwAppAnalysisControlClientSet.queryHookMethodInstInfoInPackage(packageName);
        if (null == fromApp || 0 == fromApp.size()) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInPackage(String,int) - " +
                    "从App中查询HookMethodInstInfoBase失败");
        }
        // TODO: 代码还未完成。
        return null;
    }

    /**
     * 从数据库中获得BWDump的标志。
     * @param packageName 包名。
     * @return 获得成功，则返回一个非零值；获得失败，则返回BWDumpBase.BW_DUMP_FLAGS_INVALID。
     * @throws RemoteException
     */
    @Override
    public int getBWDumpFlags(String packageName) throws RemoteException {
        BWDumpBase bwDumpBaseInDB = mBWDatabase.queryBWDump(packageName);
        if (null == bwDumpBaseInDB) {
            // 如果从数据查询BWDumpBase失败，则插入一条BWDumpBase数据。
            try {
                bwDumpBaseInDB = new BWDumpBase(mContext, packageName, BWDumpBase.BW_DUMP_FLAGS_ALL);
                if (mBWDatabase.insertOrUpdateBWDump(bwDumpBaseInDB)) {
                    return bwDumpBaseInDB.bwDumpFlags;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            BWLog.e(BWCommon.TAG, "[-] getBWDumpFlags - 获得BWDump数据失败！包名：" + packageName);
            return BWDumpBase.BW_DUMP_FLAGS_INVALID;
        } else {
            return bwDumpBaseInDB.bwDumpFlags;
        }
    }

    /**
     * 设置BWDump的标志。
     * @param packageName 包名。
     * @param bwDumpFlags 标志。
     * @return 设置成功，则返回true；设置失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean updateBWDumpFlags(String packageName, int bwDumpFlags) throws RemoteException {
        BWDumpBase bwDumpBase = mBWDatabase.queryBWDump(packageName);
        int newFlags = bwDumpFlags;
        if (null != bwDumpBase) {
            if (bwDumpFlags == (bwDumpBase.bwDumpFlags & bwDumpFlags)) {
                return true;    // 如果bwDumpFlags的值相等，也认为设置成功。
            } else {
                newFlags |= bwDumpFlags;
                bwDumpBase.bwDumpFlags |= bwDumpFlags;
            }
        }
        return setBWDumpFlags(packageName, newFlags);
    }

    /**
     * 设置BWDump的标志。
     * @param packageName 包名。
     * @param bwDumpFlags 标志。
     * @return 设置成功，则返回true；设置失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean setBWDumpFlags(String packageName, int bwDumpFlags) throws RemoteException {
        BWDumpBase bwDumpBase = mBWDatabase.queryBWDump(packageName);
        if (null == bwDumpBase) {
            // 对应的BWDumpBase对象在数据库中不存在时，创建BWDumpBase对象。
            try {
                bwDumpBase = new BWDumpBase(mContext, packageName, bwDumpFlags);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (null == bwDumpBase) {
                return false;
            }
        } else if (bwDumpBase.bwDumpFlags == bwDumpFlags) {
            return true;    // 如果bwDumpFlags的值相等，也认为设置成功。
        } else {
            bwDumpBase.bwDumpFlags = bwDumpFlags;
        }

        if (bwAppAnalysisControlClientSet.setBWDumpFlags(packageName, bwDumpFlags)) {
            // 数据库插入数据。
            if (!mBWDatabase.insertOrUpdateBWDump(bwDumpBase)) {
                BWLog.e(BWCommon.TAG, "[-] setBWDumpFlags - " +
                        "向数据库中插入BWDumpBase失败，不对插入APP的数据进行回滚操作，" +
                        "方法将返回true。App包名：" + packageName + "。");
            }
            return true;
        } else {
            BWLog.e(BWCommon.TAG, "[-] setBWDumpFlags - " +
                    "向APP中插入BWDumpBase失败。App包名：" + packageName + "。");
            return false;
        }
    }

    /**
     * 禁用BWDump。
     * @param packageName 包名。
     * @return 禁用成功，则返回true；禁用失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean disableBWDump(String packageName) throws RemoteException {
        return setBWDumpFlags(packageName, BWDumpBase.BW_DUMP_FLAGS_DISABLE);
    }

    /**
     * 清理数据库。
     * 清理数据库中所有表中的数据。
     * @throws RemoteException
     */
    @Override
    public void clearDatabase() throws RemoteException {
        mBWDatabase.clearDatabase();
    }

    /**
     * 重置数据库。
     * 将数据库删除，再创建一个数据库。
     * @return 重置成功，则返回true；重置失败，则返回false。
     * @throws RemoteException
     */
    @Override
    public boolean resetDatabase() throws RemoteException {
        return mBWDatabase.resetDatabase();
    }

    /**
     * 当应用卸载成功后，调用这个方法。
     * @param uid 应用的uid。
     * @throws RemoteException
     */
    @Override
    public void clearWhenAppUninstall(int uid) throws RemoteException {
        BWLog.i(BWCommon.TAG, "[*] clearWhenAppUninstall - uid=" + uid);
        clearWhenProcessGroupKilled(uid);
        mBWDatabase.clearWhenAppUninstall(uid);
    }

    @Override
    public void clearWhenProcessGroupKilled(int uid) throws RemoteException {
        BWLog.i(BWCommon.TAG, "[*] clearWhenProcessGroupKilled - uid=" + uid);
        bwAppAnalysisControlClientSet.removeAllClientByUID(uid);
    }

    /**
     * 当进程被杀后，调用这个方法。
     * @param uid 应用的uid。
     * @param pid 进程ID。
     * @throws RemoteException
     */
    @Override
    public void clearWhenProcessKilled(int uid, int pid) throws RemoteException {
        BWLog.i(BWCommon.TAG, "[*] clearWhenProcessKilled - uid=" + uid + ", pid=" + pid);
        bwAppAnalysisControlClientSet.removeClient(uid, pid);
    }

    @Override
    public boolean addBWAppAnalysisControlServerPort(int port) throws RemoteException {
        BWLog.i(BWCommon.TAG, "[*] addBWAppAnalysisControlServerPort - port=" + port);
        return bwAppAnalysisControlClientSet.add(port);
    }

    // TODO: 这个接口应该要改一下，去掉processName参数，并且方法内使用getBWAppAnalysisControlClient函数。
    @Override
    public int getBWAppAnalysisControlServerPort(int uid, int pid, String processName) throws RemoteException {
        List<BWAppAnalysisControlClient> list = bwAppAnalysisControlClientSet.getByUID(uid);
        if (null == list) {
            return -1;
        }
        for (BWAppAnalysisControlClient client : list) {
            ProcessInfoForNetPort p = client.processInfoForNetPort;
            if ((p.uid == uid) && (p.pid == pid) &&
                    (p.processName.equals(processName)) && (p.isApp)) {
                return p.port;
            }
        }
        return -1;
    }

    @Override
    public boolean needsInterpreter(int uid) throws RemoteException {
        return (mBWDatabase.getStartFlagsInAppID(uid) & AppIDBase.START_FLAG_INTERPRETER) != 0;
    }

    @Override
    public boolean needsRoot(int uid) throws RemoteException {
        return (mBWDatabase.getStartFlagsInAppID(uid) & AppIDBase.START_FLAG_ROOT) != 0;
    }

    @Override
    public void setStartFlags(int uid, int startFlags) throws RemoteException {
        try {
            mBWDatabase.setStartFlags(uid, startFlags);
        } catch (BWDatabaseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean insertOrUpdateAppID(AppIDBase appIDBase) throws RemoteException {
        return (null != mBWDatabase.insertOrUpdateAppID(appIDBase));
    }

    /**
     * 是否验证应用签名。
     * @return true：验证应用签名；false：不验证应用签名。
     * @throws RemoteException
     */
    @Override
    public boolean isVerifyApplicationSignature() throws RemoteException {
        try {
            return mBWDatabase.isVerifyApplicationSignature();
        } catch (BWDatabaseException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 设置是否验证应用签名。
     * @param isVerifyApplicationSignature true：验证应用签名；false：不验证应用签名。
     * @throws RemoteException
     */
    @Override
    public void setVerifyApplicationSignature(boolean isVerifyApplicationSignature) throws RemoteException {
        mBWDatabase.setVerifyApplicationSignature(isVerifyApplicationSignature);
    }

}
