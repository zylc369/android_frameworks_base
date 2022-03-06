package android.bw.socket;

import android.annotation.TargetApi;
import android.bw.BWCommon;
import android.bw.BWLog;
import android.bw.BWUtils;
import android.bw.db.base.BWDumpBase;
import android.bw.db.base.HookMethodInstInfoBase;
import android.bw.db.base.TraceMethodInfoBase;
import android.bw.service.IBWService;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * BW应用分析控制服务端。
 * 服务端运行在APP进程中。
 * Created by asherli on 16/3/15.
 */
public class BWAppAnalysisControlServer {

    private ServerSocket serverSocket = null;

    private static BWAppAnalysisControlServer instance = null;

    private static boolean isStartServerSuccess = false;

    private static BWAppAnalysisControlServer getInstance() {
        if (null == instance) {
            synchronized (Object.class) {
                if (null == instance) {
                    instance = new BWAppAnalysisControlServer();
                }
            }
        }
        return instance;
    }

    /**
     * 启动服务端。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean startServer() {
        BWLog.i(BWCommon.TAG, "[*] BWAppAnalysisControlServer.startServer - 进入");
        final BWAppAnalysisControlServer server = BWAppAnalysisControlServer.getInstance();
        if (!isStartServerSuccess) {
            synchronized (Object.class) {
                if (!isStartServerSuccess) {
                    boolean result = false;
                    try {
                        server.serverSocket = new ServerSocket();
                        InetSocketAddress inetSocketAddress =
                                new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
                        server.serverSocket.bind(inetSocketAddress);

                        IBWService ibwService = BWUtils.getBWService();
                        if (null == ibwService) {
                            BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer.startServer - 获取BWService失败。");
                            return false;
                        }
                        int port = server.serverSocket.getLocalPort();
                        BWLog.i(BWCommon.TAG, "[*] BWAppAnalysisControlServer.startServer - port=" + port);
                        result = ibwService.addBWAppAnalysisControlServerPort(port);
                    } catch (IOException | RemoteException e) {
                        BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer.startServer - " + e.getMessage());
                        e.printStackTrace();
                    }

                    if (!result) {
                        BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer.startServer - result=false");
                        return false;
                    }

                    new Thread() {
                        @TargetApi(Build.VERSION_CODES.KITKAT)
                        @Override
                        public void run() {
                            BWLog.i(BWCommon.TAG, "[*] [服务端] 启动成功。");

                            while (true) {
                                try (Socket socket = server.serverSocket.accept();
                                     InputStream inputStream = socket.getInputStream();
                                     BufferedWriter writer = new BufferedWriter(
                                             new OutputStreamWriter(socket.getOutputStream()))) {
                                    Log.i(BWCommon.TAG, "[*] [服务端] 发现socket连接。");
                                    String jsonText = server.read(inputStream);    // 读取JSON数据。

                                    JSONObject jsonObject = null;
                                    String op = null;
                                    try {
                                        // 解析JSON数据。
                                        jsonObject = new JSONObject(jsonText);
                                        op = jsonObject.getString("op");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (null == jsonObject) {
                                        BWLog.e(BWCommon.TAG, "[-] [服务端] 解析JSON数据失败。op=null");
                                        break;
                                    }

                                    String data = null;
                                    if ("closeServer".equals(op)) {
                                        boolean isBreak = true;
                                        try {
                                            data = server.createJSONTextForBoolean(true);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        if (null == data) {
                                            isBreak = false;
                                            BWLog.e(BWCommon.TAG, "[*] [服务端]关闭服务端失败。");
                                            data = server.constructionErrorData("关闭服务端失败。");
                                        } else {
                                            BWLog.i(BWCommon.TAG, "[*] [服务端] 操作：关闭服务端。");
                                        }
                                        writer.write(data);
                                        writer.flush();
                                        if (isBreak)
                                            break;
                                    } else if ("queryTraceMethodInfo".equals(op)) {
                                        data = server.queryTraceMethodInfo(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] queryTraceMethodInfo失败。");
                                            String errorData = server.constructionErrorData("queryTraceMethodInfo失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("queryHookMethodInstInfo".equals(op)) {
                                        data = server.queryHookMethodInstInfo(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] queryHookMethodInstInfo失败。");
                                            String errorData = server.constructionErrorData("queryHookMethodInstInfo失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("queryHookMethodInstInfoInMethod".equals(op)) {
                                        data = server.queryHookMethodInstInfoInMethod(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] queryHookMethodInstInfoInMethod失败。");
                                            String errorData = server.constructionErrorData("queryHookMethodInstInfoInMethod失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("getBWDumpFlags".equals(op)) {
                                        data = server.getBWDumpFlags();
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] getBWDumpFlags失败。");
                                            String errorData = server.constructionErrorData("getBWDumpFlags失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("insertOrUpdateTraceMethodInfo".equals(op)) {
                                        data = server.insertOrUpdateTraceMethodInfo(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] insertOrUpdateTraceMethodInfo失败。");
                                            String errorData = server.constructionErrorData("insertOrUpdateTraceMethodInfo失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("insertOrUpdateHookMethodInstInfo".equals(op)) {
                                        data = server.insertOrUpdateHookMethodInstInfo(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] insertOrUpdateHookMethodInstInfo失败。");
                                            String errorData = server.constructionErrorData("insertOrUpdateHookMethodInstInfo失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("setBWDumpFlags".equals(op)) {
                                        data = server.setBWDumpFlags(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] setBWDumpFlags失败。");
                                            String errorData = server.constructionErrorData("setBWDumpFlags失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("deleteTraceMethodInfo".equals(op)) {
                                        data = server.deleteTraceMethodInfo(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] deleteTraceMethodInfo失败。");
                                            String errorData = server.constructionErrorData("deleteTraceMethodInfo失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("deleteHookMethodInstInfo".equals(op)) {
                                        data = server.deleteHookMethodInstInfo(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] deleteHookMethodInstInfo失败。");
                                            String errorData = server.constructionErrorData("deleteHookMethodInstInfo失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("deleteHookMethodInstInfoInMethod".equals(op)) {
                                        data = server.deleteHookMethodInstInfoInMethod(jsonObject);
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] deleteHookMethodInstInfoInMethod失败。");
                                            String errorData = server.constructionErrorData("deleteHookMethodInstInfoInMethod失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else if ("deleteHookMethodInstInfoInPackage".equals(op)) {
                                        data = server.deleteHookMethodInstInfoInPackage();
                                        if (null == data) {
                                            BWLog.e(BWCommon.TAG, "[-] [服务端] deleteHookMethodInstInfoInPackage失败。");
                                            String errorData = server.constructionErrorData("deleteHookMethodInstInfoInPackage失败。");
                                            data = (null == errorData) ? "" : errorData;
                                        }
                                    } else {
                                        BWLog.e(BWCommon.TAG, "[-] [服务端] 未知的操作。op=" + op);
                                        String errorData = server.constructionErrorData("未知的操作。op=" + op);
                                        data = (null == errorData) ? "" : errorData;
                                    }
                                    writer.write(data);
                                    writer.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                server.serverSocket.close();
                                server.serverSocket = null;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }.start();
                    isStartServerSuccess = true;
                }
            }
        }
        BWLog.i(BWCommon.TAG, "[*] BWAppAnalysisControlServer.startServer - 离开，" + isStartServerSuccess);
        return true;
    }

    private String queryTraceMethodInfo(JSONObject jsonObject) {
        String result = null;
        try {
            int hashCode = jsonObject.getInt("hashCode");
            TraceMethodInfoBase traceMethodInfoBase = nativeQueryTraceMethodInfo(hashCode);
            if (null == traceMethodInfoBase) {
                BWLog.e(BWCommon.TAG, "[-] queryTraceMethodInfo - " +
                        "C层代码查询TraceMethodInfoBase失败！");
                return null;
            }
            JSONObject jsonTmp = traceMethodInfoBase.toJSONObject();
            if (null == jsonTmp) {
                BWLog.e(BWCommon.TAG, "[-] queryTraceMethodInfo - " +
                        "将TraceMethodInfoBase对象转换为JSONObject对象失败！");
                return null;
            }
            result = jsonTmp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String queryHookMethodInstInfo(JSONObject jsonObject) {
        String result = null;
        try {
            int hashCode = jsonObject.getInt("hashCode");
            long instLineNum = jsonObject.getLong("instLineNum");

            HookMethodInstInfoBase base = nativeQueryHookMethodInstInfo(hashCode, instLineNum);
            if (null == base) {
                BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo - " +
                        "C层代码查询HookMethodInstInfoBase失败！");
                return null;
            }
            JSONObject jsonTmp = HookMethodInstInfoBase.toJSONObject(base);
            if (null == jsonTmp) {
                BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo - " +
                        "将HookMethodInstInfoBase对象转换为JSONObject对象失败！");
                return null;
            }
            result = jsonTmp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String queryHookMethodInstInfoInMethod(JSONObject jsonObject) {
        String result = null;
        try {
            int hashCode = jsonObject.getInt("hashCode");
            List<HookMethodInstInfoBase> list = nativeQueryHookMethodInstInfoInMethod(hashCode);
            if (null == list || 0 == list.size()) {
                BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer.queryHookMethodInstInfoInMethod - " +
                        "null == list || 0 == list.size()");
                return null;
            }
            int size = list.size();
            JSONObject jsonTmp = new JSONObject();
            jsonTmp.put("size", size);
            for (int i = 0; i < size; i++) {
                HookMethodInstInfoBase base = list.get(i);
                JSONObject tmp = HookMethodInstInfoBase.toJSONObject(base);
                if (null == tmp) {
                    BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfoInMethod - " +
                            "将HookMethodInstInfoBase对象转换为JSONObject对象失败！index=" + i);
                    return null;
                }
                jsonTmp.put("" + i, tmp);
            }
            result = jsonTmp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getBWDumpFlags() {
        int bwDumpFlags = nativeGetBWDumpFlags();
        if (BWDumpBase.BW_DUMP_FLAGS_INVALID == bwDumpFlags) {
            BWLog.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer.getBWDumpFlags - " +
                    "C层代码查询bwDumpFlags失败。");
            return null;
        }
        String result = null;
        JSONObject jsonTmp = new JSONObject();
        try {
            jsonTmp.put("bwDumpFlags", bwDumpFlags);
            result = jsonTmp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String insertOrUpdateTraceMethodInfo(JSONObject jsonObject) {
        String result = null;
        boolean isSuccess = false;
        try {
            TraceMethodInfoBase traceMethodInfoBase = new TraceMethodInfoBase(jsonObject);
            isSuccess = nativeInsertOrUpdateTraceMethodInfo(traceMethodInfoBase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            result = createJSONTextForBoolean(isSuccess);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String insertOrUpdateHookMethodInstInfo(JSONObject jsonObject) {
        String result = null;
        boolean isSuccess = false;
        try {
            HookMethodInstInfoBase hookMethodInstInfoBase = new HookMethodInstInfoBase(jsonObject);
            isSuccess = nativeInsertOrUpdateHookMethodInstInfo(hookMethodInstInfoBase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            result = createJSONTextForBoolean(isSuccess);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String setBWDumpFlags(JSONObject jsonObject) {
        String result = null;
        boolean isSuccess = false;
        try {
            int bwDumpFlags = jsonObject.getInt("bwDumpFlags");
            isSuccess = nativeSetBWDumpFlags(bwDumpFlags);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            result = createJSONTextForBoolean(isSuccess);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String deleteTraceMethodInfo(JSONObject jsonObject) {
        String result = null;
        boolean isSuccess = false;
        try {
            int hashCode = jsonObject.getInt("hashCode");
            isSuccess = nativeDeleteTraceMethodInfo(hashCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            result = createJSONTextForBoolean(isSuccess);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String deleteHookMethodInstInfo(JSONObject jsonObject) {
        String result = null;
        int deletedNum = 0;
        try {
            int hashCode = jsonObject.getInt("hashCode");
            long instLineNum = jsonObject.getInt("instLineNum");
            deletedNum = nativeDeleteHookMethodInstInfo(hashCode, instLineNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject jsonTmp = new JSONObject();
        try {
            jsonTmp.put("deletedNum", deletedNum);
            result = jsonTmp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String deleteHookMethodInstInfoInMethod(JSONObject jsonObject) {
        String result = null;
        int deletedNum = 0;
        try {
            int hashCode = jsonObject.getInt("hashCode");
            deletedNum = nativeDeleteHookMethodInstInfoInMethod(hashCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject jsonTmp = new JSONObject();
        try {
            jsonTmp.put("deletedNum", deletedNum);
            result = jsonTmp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String deleteHookMethodInstInfoInPackage() {
        String result = null;
        int deletedNum = nativeDeleteHookMethodInstInfoInPackage();
        JSONObject jsonTmp = new JSONObject();
        try {
            jsonTmp.put("deletedNum", deletedNum);
            result = jsonTmp.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 构造错误信息数据。
     * @param errorInfo 错误信息。
     * @return 构造成功，则返回错误信息数据；否则，返回null。
     */
    private String constructionErrorData(String errorInfo) {
        String result = "";
        JSONObject errorData = new JSONObject();
        try {
            errorData.put("error", errorInfo);
            result = errorData.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从客户端读取数据。
     * @param inputStream 输入流。
     * @return 返回读取到的数据。
     * @throws IOException
     */
    private String read(InputStream inputStream) throws IOException {
        String jsonText = "";
        byte[] buffer = new byte[1024];
        int readBytes;
        while ((readBytes = inputStream.read(buffer)) > 0) {
            Log.i(BWCommon.TAG, "[服务端] 读取数据字节数：" + readBytes);
            jsonText += new String(buffer, 0, readBytes);
            int jsonTextBytesNum = getJSONTextBytesNum(jsonText);
            if (0 == jsonTextBytesNum) {
                Log.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer - " +
                        "json数据中的文本字节数：0，读取的字节数：" + readBytes +
                        "。JSON数据：" + jsonText);
            } else if (-1 == jsonTextBytesNum) {
                Log.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer - " +
                        "获得json数据中的文本字节数失败！JSON数据：" + jsonText);
            } else if (readBytes > jsonTextBytesNum) {
                Log.e(BWCommon.TAG, "[-] BWAppAnalysisControlServer - " +
                        "读取的字节数大于json数据中的文本字节数。" +
                        "json数据中的文本字节数：0，读取的字节数：" + readBytes +
                        "。JSON数据：" + jsonText);
            } else if (readBytes == jsonTextBytesNum) {
                break;
            }
        }
        Log.i(BWCommon.TAG, "[服务端] 读取数据完成。");
        return jsonText;
    }

    /**
     * 验证字符串的特定索引的下一个字符是否是指定的字符。
     * 如果遇到空格则跳过。
     * @param str 字符串。
     * @param index 索引。
     * @param ch 要查询的字符。
     * @return 查询成功，则返回查询到的索引值；查询失败，则返回-1。
     */
    private int validNextCharWithoutSpace(String str, int index, char ch) {
        int length = str.length();
        for (int i = index; i < length; i++) {
            char tmp = str.charAt(i);
            if (' ' != tmp) {
                return ch == tmp ? i : -1;
            }
        }
        return -1;
    }

    /**
     * 获得json数据的字节数。
     * @param jsonText json文本。
     * @return 返回json数据的字节数。
     */
    private int getJSONTextBytesNum(String jsonText) {
        int index = validNextCharWithoutSpace(jsonText, 0, '{');
        if (-1 == index) {
            return -1;
        }
        index = jsonText.indexOf("\"bytes_num\"");
        if (-1 == index) {
            return -1;
        }
        index += "\"bytes_num\"".length();
        index = validNextCharWithoutSpace(jsonText, index, ':');
        if (-1 == index) {
            return -1;
        }
        index++;
        index = validNextCharWithoutSpace(jsonText, index, '\"');
        if (-1 == index) {
            return -1;
        }
        index++;
        try {
            return Integer.parseInt(jsonText.substring(index, jsonText.indexOf("\"", index)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String createJSONTextForBoolean(boolean b) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", b);
        return jsonObject.toString();
    }

    private native boolean nativeInsertOrUpdateTraceMethodInfo(TraceMethodInfoBase traceMethodInfoBase);
    private native boolean nativeDeleteTraceMethodInfo(int hash);
    private native TraceMethodInfoBase nativeQueryTraceMethodInfo(int hash);

    private native boolean nativeInsertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase hookMethodInstInfoBase);

    private native int nativeDeleteHookMethodInstInfo(int hash, long instLineNum);
    private native int nativeDeleteHookMethodInstInfoInMethod(int hash);
    private native int nativeDeleteHookMethodInstInfoInPackage();

    /**
     * 获得被Hook方法所有的Hook信息。
     * @param hash 被Hook方法的哈希。
     * @return 返回被Hook方法所有的Hook信息。
     */
    private native List<HookMethodInstInfoBase> nativeQueryHookMethodInstInfoInMethod(int hash);
    private native HookMethodInstInfoBase nativeQueryHookMethodInstInfo(int hash, long instLineNum);

    private native boolean nativeSetBWDumpFlags(int bwDumpFlags);
    private native int nativeGetBWDumpFlags();

}
