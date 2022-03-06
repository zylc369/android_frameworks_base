package android.bw.service;

import android.bw.db.base.AppIDBase;
import android.bw.db.base.TraceMethodInfoBase;
import android.bw.db.base.HookMethodInstInfoBase;

/**
 * Created by buwai on 15/11/11.
 */
interface IBWService {

    boolean isInitSuccess();

    String getPackageNameByUID(int uid);

    boolean insertOrUpdateTraceMethodInfo(in TraceMethodInfoBase traceMethodInfoBase);

    boolean deleteTraceMethodInfo(String packageName, int hashCode);

    TraceMethodInfoBase queryTraceMethodInfo(String packageName, int hashCode);

    List<TraceMethodInfoBase> queryAllTraceMethodInfo(String packageName);

    boolean insertOrUpdateHookMethodInstInfo(in HookMethodInstInfoBase hookMethodInstInfoBase);

    boolean deleteHookMethodInstInfo(String packageName, int hashCode, long instLineNum);

    boolean deleteHookMethodInstInfoInMethod(String packageName, int hashCode);

    boolean deleteHookMethodInstInfoInPackage(String packageName);

    HookMethodInstInfoBase queryHookMethodInstInfo(String packageName, int hashCode, long instLineNum);

    List<HookMethodInstInfoBase> queryHookMethodInstInfoInMethod(String packageName, int hashCode);

    List<HookMethodInstInfoBase> queryHookMethodInstInfoInPackage(String packageName);

    int getBWDumpFlags(String packageName);

    boolean updateBWDumpFlags(String packageName, int bwDumpFlags);

    boolean setBWDumpFlags(String packageName, int bwDumpFlags);

    boolean disableBWDump(String packageName);

    void clearDatabase();

    boolean resetDatabase();

    void clearWhenAppUninstall(int uid);

    void clearWhenProcessGroupKilled(int uid);

    void clearWhenProcessKilled(int uid, int pid);

    boolean addBWAppAnalysisControlServerPort(int port);

    int getBWAppAnalysisControlServerPort(int uid, int pid, String processName);

    boolean needsInterpreter(int uid);

    boolean needsRoot(int uid);

    void setStartFlags(int uid, int startFlags);

    boolean insertOrUpdateAppID(in AppIDBase appIDBase);

    boolean isVerifyApplicationSignature();

    void setVerifyApplicationSignature(boolean isVerifyApplicationSignature);

}
