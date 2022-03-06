package com.android.server.bw;

import android.bw.BWCommon;
import android.bw.BWLog;
import android.bw.BWUtils;
import android.bw.ProcessInfoForNetPort;
import android.bw.db.base.HookMethodInstInfoBase;
import android.bw.db.base.TraceMethodInfoBase;
import android.bw.socket.BWAppAnalysisControlClient;
import android.os.Binder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * BWAppAnalysisControlClient集。
 * Created by asherli on 16/5/17.
 */
class BWAppAnalysisControlClientSet {

    private BWService bwService;
    private Map<Integer, List<BWAppAnalysisControlClient>> bwAppAnalysisControlClientMap = new HashMap<>();

    public BWAppAnalysisControlClientSet(BWService bwService) {
        this.bwService = bwService;
    }

    /**
     * 依据端口添加端口所属进程的信息。
     * @param port 端口。
     * @return 添加成功，则返回true；否则，返回false。
     */
    public boolean add(int port) {
        boolean result = false;
        try {
            ProcessInfoForNetPort processInfoForNetPort =
                    new ProcessInfoForNetPort(Binder.getCallingUid(), Binder.getCallingPid(),
                            bwService.getPackageNameByUID(Binder.getCallingUid()),
                            port,
                            BWUtils.nativeGetProcessName(Binder.getCallingPid()), true);

            List<BWAppAnalysisControlClient> list = bwAppAnalysisControlClientMap.get(processInfoForNetPort.uid);
            if (null == list) {
                list = new ArrayList<>();
            }
            BWAppAnalysisControlClient bwAppAnalysisControlClient = new BWAppAnalysisControlClient(processInfoForNetPort);
            if (list.contains(bwAppAnalysisControlClient)) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlClientSet.add - 正在添加一个重复的ProcessInfoForNetPort对象。");
                return false;
            }
            list.add(bwAppAnalysisControlClient);
            BWLog.i(BWCommon.TAG, "[*] BWAppAnalysisControlClientSet.add - 添加的进程信息：" + processInfoForNetPort);
            bwAppAnalysisControlClientMap.put(processInfoForNetPort.uid, list);
            result = true;
        } catch (NullPointerException | RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean insertOrUpdateTraceMethodInfo(TraceMethodInfoBase traceMethodInfoBase) {
        List<BWAppAnalysisControlClient> clients =
                bwAppAnalysisControlClientMap.get(traceMethodInfoBase.methodLocation.appIDBase.getUid());

        if ((null == clients) || (0 == clients.size())) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "insertOrUpdateTraceMethodInfo - " +
                    "没有找到对应的socket客户端。uid=" + traceMethodInfoBase.methodLocation.appIDBase.getUid());
            return false;
        }
        // 遍历相同UID和包名的所有应用的BWAppAnalysisControlClient对象，然后发送数据。
        int count = 1;
        String packageName = traceMethodInfoBase.methodLocation.appIDBase.getPackageName();
        for (BWAppAnalysisControlClient client : clients) {
            if (!client.processInfoForNetPort.packageName.equals(packageName)) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlClientSet." +
                        "insertOrUpdateTraceMethodInfo - " + count +
                        ". socket客户端记录的包名：" +
                        client.processInfoForNetPort.packageName + "，" +
                        "TraceMethodInfoBase对象记录的包名：" + packageName);
                return false;
            }
            if (!client.insertOrUpdateTraceMethodInfo(traceMethodInfoBase)) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlClientSet." +
                        "insertOrUpdateTraceMethodInfo - " + count +
                        ". 向应用中插入数据失败。");
                return false;
            }
            count++;
        }
        return true;
    }

    public boolean deleteTraceMethodInfo(String packageName, int hashCode) {
        List<BWAppAnalysisControlClient> clients = getByPackageName(packageName);
        if ((null == clients) || (0 == clients.size())) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "deleteTraceMethodInfo - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return false;
        }

        int count = 1;
        for (BWAppAnalysisControlClient client : clients) {
            if (!client.deleteTraceMethodInfo(hashCode)) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlClientSet." +
                        "deleteTraceMethodInfo - " + count +
                        ". 删除应用中的数据失败。。");
                return false;
            }
            count++;
        }
        return true;
    }

    public TraceMethodInfoBase queryTraceMethodInfo(String packageName, int hashCode) {
        BWAppAnalysisControlClient client = getByPackageName2(packageName);
        if (null == client) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "deleteTraceMethodInfo - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return null;
        }
        return client.queryTraceMethodInfo(hashCode);
    }

    public boolean insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase hookMethodInstInfoBase) {
        String packageName = hookMethodInstInfoBase.instructionLocation.appIDBase.getPackageName();
        List<BWAppAnalysisControlClient> clients = getByPackageName(packageName);
        if ((null == clients) || (0 == clients.size())) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "insertOrUpdateHookMethodInstInfo - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return false;
        }
        for (BWAppAnalysisControlClient client : clients) {
            if (!client.insertOrUpdateHookMethodInstInfo(hookMethodInstInfoBase)) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlClientSet." +
                        "insertOrUpdateHookMethodInstInfo - 插入或更新失败。包名：" + packageName);
                return false;
            }
        }
        return true;
    }

    public boolean deleteHookMethodInstInfo(String packageName, int hashCode, long instLineNum) {
        List<BWAppAnalysisControlClient> clients = getByPackageName(packageName);
        if ((null == clients) || (0 == clients.size())) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "deleteHookMethodInstInfo - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return false;
        }
        for (BWAppAnalysisControlClient client : clients) {
            if (!client.deleteHookMethodInstInfo(hashCode, instLineNum)) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlClientSet." +
                        "deleteHookMethodInstInfo - 删除Hook方法指令信息失败。包名：" + packageName);
                return false;
            }
        }
        return true;
    }

    public HookMethodInstInfoBase queryHookMethodInstInfo(
            String packageName, int hashCode, long instLineNum) {
        BWAppAnalysisControlClient client = getByPackageName2(packageName);
        if (null == client) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "queryHookMethodInstInfo - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return null;
        }
        return client.queryHookMethodInstInfo(hashCode, instLineNum);
    }

    public List<HookMethodInstInfoBase> queryHookMethodInstInfoInMethod(String packageName, int hashCode) {
        BWAppAnalysisControlClient client = getByPackageName2(packageName);
        if (null == client) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "queryHookMethodInstInfoInMethod - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return null;
        }
        return client.queryHookMethodInstInfoInMethod(hashCode);
    }

    public List<HookMethodInstInfoBase> queryHookMethodInstInfoInPackage(String packageName) {
        BWAppAnalysisControlClient client = getByPackageName2(packageName);
        if (null == client) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "queryHookMethodInstInfoInPackage - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return null;
        }
        return client.queryHookMethodInstInfoInPackage();
    }

    public boolean setBWDumpFlags(final String packageName, final int bwDumpFlags) {
        List<BWAppAnalysisControlClient> clients = getByPackageName(packageName);
        if ((null == clients) || (0 == clients.size())) {
            BWLog.w(BWCommon.TAG, "[!] BWAppAnalysisControlClientSet." +
                    "updateBWDumpFlags - " +
                    "没有找到对应的socket客户端。包名：" + packageName);
            return false;
        }
        for (BWAppAnalysisControlClient client : clients) {
            if (!client.setBWDumpFlags(bwDumpFlags)) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlClientSet." +
                        "setBWDumpFlags - 设置BWDumpFlags失败。包名：" + packageName);
                return false;
            }
        }
        return true;
    }

    public void removeAllClientByUID(final int uid) {
        bwAppAnalysisControlClientMap.remove(uid);
    }

    public void removeClient(final int uid, final int pid) {
        List<BWAppAnalysisControlClient> clients = bwAppAnalysisControlClientMap.get(uid);
        if (null == clients) {
            return;
        }
        Iterator<BWAppAnalysisControlClient> iterator = clients.iterator();
        while (iterator.hasNext()) {
            BWAppAnalysisControlClient client = iterator.next();
            if (pid == client.processInfoForNetPort.pid) {
                iterator.remove();
            }
        }
    }

    public List<BWAppAnalysisControlClient> getByUID(final int uid) {
        return bwAppAnalysisControlClientMap.get(uid);
    }

    private BWAppAnalysisControlClient get(final int uid, final int pid) {
        List<BWAppAnalysisControlClient> list = bwAppAnalysisControlClientMap.get(uid);
        if (null == list) {
            return null;
        }
        for (BWAppAnalysisControlClient client : list) {
            ProcessInfoForNetPort p = client.processInfoForNetPort;
            if ((p.uid == uid) && (p.pid == pid)) {
                return client;
            }
        }
        return null;
    }

    private List<BWAppAnalysisControlClient> getByPackageName(final String packageName) {
        for (Map.Entry<Integer, List<BWAppAnalysisControlClient>> entry :
                bwAppAnalysisControlClientMap.entrySet()) {
            List<BWAppAnalysisControlClient> list = entry.getValue();
            if (null != list && 0 != list.size()) {
                if (list.get(0).processInfoForNetPort.packageName.equals(packageName)) {
                    return list;
                }
            }
        }
        return null;
    }

    private BWAppAnalysisControlClient getByPackageName2(final String packageName) {
        for (Map.Entry<Integer, List<BWAppAnalysisControlClient>> entry :
                bwAppAnalysisControlClientMap.entrySet()) {
            List<BWAppAnalysisControlClient> list = entry.getValue();
            if (null != list && 0 != list.size()) {
                if (list.get(0).processInfoForNetPort.packageName.equals(packageName)) {
                    return list.get(0);
                }
            }
        }
        return null;
    }

}
