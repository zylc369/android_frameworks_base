package android.bw.socket;

import android.annotation.TargetApi;
import android.bw.BWCommon;
import android.bw.BWLog;
import android.bw.ProcessInfoForNetPort;
import android.bw.db.base.HookMethodInstInfoBase;
import android.bw.db.base.TraceMethodInfoBase;
import android.os.Build;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

/**
 * BW应用分析控制客户端。
 * 客户端运行在BWService所属的进程中。
 * Created by asherli on 16/3/15.
 */
public class BWAppAnalysisControlClient {

    public ProcessInfoForNetPort processInfoForNetPort;

    public BWAppAnalysisControlClient(ProcessInfoForNetPort processInfoForNetPort) {
        this.processInfoForNetPort = processInfoForNetPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BWAppAnalysisControlClient client = (BWAppAnalysisControlClient) o;

        return processInfoForNetPort != null && processInfoForNetPort.equals(client.processInfoForNetPort);

    }

    @Override
    public int hashCode() {
        return processInfoForNetPort != null ? processInfoForNetPort.hashCode() : 0;
    }

    /**
     * 关闭服务端。
     */
    public boolean closeServer() {
        boolean result = false;
        try {
            JSONObject jsonObject = createJSONObject("closeServer");
            result = sendDataReturnBoolean(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean insertOrUpdateTraceMethodInfo(TraceMethodInfoBase traceMethodInfoBase) {
        boolean result = false;
        try {
            JSONObject jsonObject = createJSONObject("insertOrUpdateTraceMethodInfo");
            result = sendDataReturnBoolean(
                    traceMethodInfoBase.toJSONObject(jsonObject));
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean deleteTraceMethodInfo(int hashCode) {
        boolean result = false;
        try {
            JSONObject jsonObject = createJSONObject("deleteTraceMethodInfo");
            jsonObject.put("hashCode", hashCode);
            result = sendDataReturnBoolean(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public TraceMethodInfoBase queryTraceMethodInfo(int hashCode) {
        TraceMethodInfoBase result = null;
        JSONObject responseData = null;
        try {
            do {
                JSONObject jsonObject = createJSONObject("queryTraceMethodInfo");
                jsonObject.put("hashCode", hashCode);
                responseData = sendData(jsonObject);
            } while (false);
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (null != responseData) {
            try {
                result = new TraceMethodInfoBase(responseData);
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    BWLog.e(BWCommon.TAG, responseData.getString("error"));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase hookMethodInstInfoBase) {
        boolean result = false;
        try {
            JSONObject jsonObject = createJSONObject("insertOrUpdateTraceMethodInfo");
            result = sendDataReturnBoolean(
                    HookMethodInstInfoBase.toJSONObject(hookMethodInstInfoBase, jsonObject));
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean deleteHookMethodInstInfo(int hashCode, long instLineNum) {
        boolean result = false;
        try {
            JSONObject jsonObject = createJSONObject("deleteHookMethodInstInfo");
            jsonObject.put("hashCode", hashCode);
            jsonObject.put("instLineNum", instLineNum);
            result = sendDataReturnBoolean(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public HookMethodInstInfoBase queryHookMethodInstInfo(
            int hashCode, long instLineNum) {
        HookMethodInstInfoBase result = null;
        JSONObject responseData = null;
        try {
            do {
                JSONObject jsonObject = createJSONObject("queryHookMethodInstInfo");
                jsonObject.put("hashCode", hashCode);
                jsonObject.put("instLineNum", instLineNum);
                responseData = sendData(jsonObject);
            } while (false);
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (null != responseData) {
            try {
                result = new HookMethodInstInfoBase(responseData);
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    BWLog.e(BWCommon.TAG, responseData.getString("error"));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return result;
    }

    public List<HookMethodInstInfoBase> queryHookMethodInstInfoInMethod(int hashCode) {
        JSONObject responseData = null;
        List<HookMethodInstInfoBase> result = null;
        try {
            do {
                JSONObject jsonObject = createJSONObject("queryHookMethodInstInfo");
                jsonObject.put("hashCode", hashCode);
                responseData = sendData(jsonObject);
            } while (false);
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (null != responseData) {
            try {
                int size = responseData.getInt("size");
                if (0 != size) {
                    result = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        result.add(new HookMethodInstInfoBase(responseData.getJSONObject(String.valueOf(i))));
                    }
                }
            } catch (JSONException e) {
                result = null;
                e.printStackTrace();
                try {
                    BWLog.e(BWCommon.TAG, responseData.getString("error"));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return result;
    }

    public List<HookMethodInstInfoBase> queryHookMethodInstInfoInPackage() {
        // TODO:
        return null;
    }

    public boolean setBWDumpFlags(int bwDumpFlags) {
        boolean result = false;
        try {
            JSONObject jsonObject = createJSONObject("setBWDumpFlags");
            jsonObject.put("bwDumpFlags", bwDumpFlags);
            result = sendDataReturnBoolean(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONObject createJSONObject(String op) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("bytes_num", "00000000");
        jsonObject.put("op", op);
        return jsonObject;
    }

    private boolean sendDataReturnBoolean(JSONObject jsonObject) {
        boolean result = false;
        JSONObject responseData = null;
        try {
            responseData = sendData(jsonObject);
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        if (null != responseData) {
            try {
                result = responseData.getBoolean("result");
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    BWLog.e(BWCommon.TAG, responseData.getString("error"));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 发送数据。
     * @param jsonObject 要发送的数据。
     * @return 发送成功，则返回true。否则返回false。
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private JSONObject sendData(JSONObject jsonObject)
            throws ExecutionException, InterruptedException, JSONException {
        String jsonText = jsonObject.toString();
        jsonText = jsonText.replace("00000000", String.format(Locale.CHINA, "%08d", jsonText.length()));

        ExecutorService executor = null;
        Future<String> future = null;
        try {
            executor = Executors.newCachedThreadPool();
            int port = processInfoForNetPort.port;
            BWLog.i(BWCommon.TAG, "[*] BWAppAnalysisControlClient.sendData - port=" + port);
            future = executor.submit(new SocketCallable(port, jsonText));
        } finally {
            if (null != executor) {
                executor.shutdown();
            }
        }
        try {
            return new JSONObject(future.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 向服务端发送数据。
     */
    private class SocketCallable implements Callable<String> {

        private int port;
        private String jsonData;

        public SocketCallable(int port, String jsonData) {
            this.port = port;
            this.jsonData = jsonData;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public String call() throws Exception {
            StringBuilder sb = new StringBuilder();
            try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port);
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream()));
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))) {
                Log.i(BWCommon.TAG, "[client] msg:" + jsonData);
                try {
                    writer.write(jsonData);
                    writer.flush();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
    }

}
