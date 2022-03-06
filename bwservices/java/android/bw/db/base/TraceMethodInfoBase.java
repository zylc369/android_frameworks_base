package android.bw.db.base;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 跟踪/提示信息。
 * Created by asherli on 16/1/4.
 */
public class TraceMethodInfoBase implements Parcelable {

    public static final int TRACE_METHOD_INFO_FLAG_UNKNOW = 0;
    /**
     * 这个标志指定启用跟踪信息。
     */
    public static final int TRACE_METHOD_INFO_FLAG_ENABLE = 1;
    /**
     * 这个标志指定需要打印调用堆栈。
     */
    public static final int TRACE_METHOD_INFO_FLAG_PRINTCALLSTACK = 1 << 1;
    /**
     * 这个标志指定跟踪信息是可传递的。
     */
    public static final int TRACE_METHOD_INFO_FLAG_TRANSITIVE = 1 << 2;

    /**
     * 粒度：方法级别。
     */
    public static final int GRANULARITY_LEVEL_METHOD = 1;
    /**
     * 粒度：指令级别。
     */
    public static final int GRANULARITY_LEVEL_INSTRUCTION = 2;

    /**
     * 要提示的方法类型：不限。
     * 当GRANULARITY_LEVEL_CURRENT_METHOD被设置表示“提示”时才有意义。
     */
    public static final int PROMPT_METHOD_TYPE_ARY = 0;
    /**
     * 要提示的方法类型：native方法。
     * 当GRANULARITY_LEVEL_CURRENT_METHOD被设置表示“提示”时才有意义。
     */
    public static final int PROMPT_METHOD_TYPE_NATIVE = 1; // native方法。

    public MethodLocation methodLocation;
    public int traceMethodFlags = TRACE_METHOD_INFO_FLAG_UNKNOW;
    public int granularity = GRANULARITY_LEVEL_METHOD;
    public int promptMethodType = PROMPT_METHOD_TYPE_ARY;

    public TraceMethodInfoBase() {}

    public TraceMethodInfoBase(JSONObject jsonObject) throws JSONException {
        this.methodLocation = new MethodLocation(jsonObject.getJSONObject("methodLocation"));
        this.traceMethodFlags = jsonObject.getInt("traceMethodFlags");
        this.granularity = jsonObject.getInt("granularity");
        this.promptMethodType = jsonObject.getInt("promptMethodType");
    }

    // 下面是扩展部分。

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(methodLocation, flags);
        dest.writeInt(this.traceMethodFlags);
        dest.writeInt(this.granularity);
        dest.writeInt(this.promptMethodType);
    }

    public static final Creator<TraceMethodInfoBase> CREATOR = new Creator<TraceMethodInfoBase>() {
        @Override
        public TraceMethodInfoBase createFromParcel(Parcel source) {
            TraceMethodInfoBase newTraceMethodInfoBase = new TraceMethodInfoBase();
            newTraceMethodInfoBase.methodLocation = source.readParcelable(MethodLocation.class.getClassLoader());
            newTraceMethodInfoBase.traceMethodFlags = source.readInt();
            newTraceMethodInfoBase.granularity = source.readInt();
            newTraceMethodInfoBase.promptMethodType = source.readInt();
            return newTraceMethodInfoBase;
        }

        @Override
        public TraceMethodInfoBase[] newArray(int size) {
            return new TraceMethodInfoBase[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TraceMethodInfoBase other = (TraceMethodInfoBase) o;
        return this.traceMethodFlags == other.traceMethodFlags &&
                this.granularity == other.granularity &&
                this.promptMethodType == other.promptMethodType;

    }

    @Override
    public String toString() {
        return "TraceMethodInfoBase{" +
                "methodLocation=" + methodLocation +
                ", traceMethodFlags=" + traceMethodFlags +
                ", granularity=" + granularity +
                ", promptMethodType=" + promptMethodType +
                '}';
    }

    /**
     *
     * @param cursor 游标。
     * @return 转换成功，则返回TraceMethodInfoBase对象；转换失败，则返回null。
     */
    public static TraceMethodInfoBase toTraceMethodInfoBase(Cursor cursor) {
        TraceMethodInfoBase traceMethodInfoBase = new TraceMethodInfoBase();
        AppIDBase appIDBase = new AppIDBase(cursor.getInt(cursor.getColumnIndex("uid")),
                cursor.getString(cursor.getColumnIndex("packageName")),
                cursor.getInt(cursor.getColumnIndex("appType")),
                cursor.getInt(cursor.getColumnIndex("startFlags")));
        MethodIDBase methodIDBase =
                new MethodIDBase(cursor.getString(cursor.getColumnIndex("classDesc")),
                cursor.getString(cursor.getColumnIndex("methodName")),
                cursor.getString(cursor.getColumnIndex("methodSig")));
        traceMethodInfoBase.methodLocation = new MethodLocation(appIDBase, methodIDBase);
        traceMethodInfoBase.traceMethodFlags = cursor.getInt(cursor.getColumnIndex("traceMethodFlags"));
        traceMethodInfoBase.granularity = cursor.getInt(cursor.getColumnIndex("granularity"));
        traceMethodInfoBase.promptMethodType = cursor.getInt(cursor.getColumnIndex("promptMethodType"));
        return traceMethodInfoBase;
    }

    /**
     *
     * @param jsonObject 不能传入null值。
     * @return 成功，则返回参数2传入的对象；失败，则返回null。
     * @throws NullPointerException
     * @throws JSONException
     */
    public JSONObject toJSONObject(JSONObject jsonObject) throws NullPointerException, JSONException {
        if (null == jsonObject) {
            throw new NullPointerException("参数2不能传入null值");
        }
        jsonObject.put("methodLocation", this.methodLocation.toJSONObject());
        jsonObject.put("traceMethodFlags", this.traceMethodFlags);
        jsonObject.put("granularity", this.granularity);
        jsonObject.put("promptMethodType", this.promptMethodType);
        return jsonObject;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = toJSONObject(jsonObject);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
