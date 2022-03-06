package android.bw.db.base;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * MethodID表的数据。
 * Created by asherli on 16/1/18.
 */
public class MethodIDBase implements Parcelable {

    /**
     * 类描述符。如：Ljava/lang/String;
     */
    private String classDesc = "";
    /**
     * 方法名。
     */
    private String methodName = "";
    /**
     * 方法签名。如：()Ljava/lang/String;。
     */
    private String methodSig = "";

    /**
     * 这个哈希值的计算方法：
     * (classDesc + methodName + methodSig).hashCode()。
     */
    private int hash = 0;

    public MethodIDBase(String classDesc, String methodName, String methodSig) {
        this.classDesc = classDesc;
        this.methodName = methodName;
        this.methodSig = methodSig;
        this.hash = hashCode();
    }

    public MethodIDBase(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getString("classDesc"), jsonObject.getString("methodName"),
                jsonObject.getString("methodSig"));
    }

    public String getClassDesc() {
        return classDesc;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodSig() {
        return methodSig;
    }

    public int getHash() {
        return hash;
    }

    public void setClassDesc(String classDesc) {
        this.classDesc = classDesc;
        this.hash = hashCode();
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
        this.hash = hashCode();
    }

    public void setMethodSig(String methodSig) {
        this.methodSig = methodSig;
        this.hash = hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(classDesc);
        dest.writeString(methodName);
        dest.writeString(methodSig);
    }

    public static final Creator<MethodIDBase> CREATOR = new Creator<MethodIDBase>() {
        @Override
        public MethodIDBase createFromParcel(Parcel source) {
            return new MethodIDBase(source.readString(), source.readString(), source.readString());
        }

        @Override
        public MethodIDBase[] newArray(int size) {
            return new MethodIDBase[size];
        }
    };

    @Override
    public int hashCode() {
        return (classDesc + methodName + methodSig).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodIDBase that = (MethodIDBase) o;

        return hash == that.hash &&
                (classDesc != null && classDesc.equals(that.classDesc)) &&
                (methodName != null && methodName.equals(that.methodName)) &&
                (methodSig != null && methodSig.equals(that.methodSig));

    }

    @Override
    public String toString() {
        return "MethodIDBase{" +
                "classDesc='" + classDesc + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodSig='" + methodSig + '\'' +
                ", hash=" + hash +
                '}';
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("classDesc", getClassDesc());
        jsonObject.put("methodName", getMethodName());
        jsonObject.put("methodSig", getMethodSig());
        jsonObject.put("hash", getHash());
        return jsonObject;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("classDesc", getClassDesc());
        values.put("methodName", getMethodName());
        values.put("methodSig", getMethodSig());
        values.put("hash", getHash());
        return values;
    }

}
