package android.bw.db.base;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * AppID表的数据。
 * Created by asherli on 16/1/18.
 */
public class AppIDBase implements Parcelable {

    /**
     * Android App程序。
     */
    public static final int APP_TYPE_ANDROID_APP = 1;
    /**
     * 本地代码的可执行程序。
     */
    public static final int APP_TYPE_EXECUTABLE = 2;

    /**
     * 未设置启动标志。
     */
    public static final int START_FLAG_UNSET = 0;
    /**
     * 以解释模式启动。
     */
    public static final int START_FLAG_INTERPRETER = 1;

    /**
     * 以root权限启动。
     */
    public static final int START_FLAG_ROOT = (1 << 1);

    /**
     * App UID。
     */
    private int uid;

    /**
     * 对于应用来说这代表包名。
     * 对于可执行程序来说这代表完整路径。
     */
    private String packageName;

    /**
     * 应用类型。
     */
    private int appType;

    /**
     * 启动标记。
     */
    private int startFlags;

    public AppIDBase(int uid, String packageName, int appType, int startFlags) {
        this.uid = uid;
        this.packageName = packageName;
        this.appType = appType;
        this.startFlags = startFlags;
    }

    public AppIDBase(JSONObject jsonObject) throws JSONException {
        this(jsonObject.getInt("uid"), jsonObject.getString("packageName"),
                jsonObject.getInt("appType"), jsonObject.getInt("startFlags"));
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getAppType() {
        return appType;
    }

    public void setAppType(int appType) {
        this.appType = appType;
    }

    public int getStartFlags() {
        return startFlags;
    }

    public void setStartFlags(int startFlags) {
        this.startFlags = startFlags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(packageName);
        dest.writeInt(appType);
        dest.writeInt(startFlags);
    }

    public static final Creator<AppIDBase> CREATOR = new Creator<AppIDBase>() {
        @Override
        public AppIDBase createFromParcel(Parcel source) {
            return new AppIDBase(source.readInt(), source.readString(), source.readInt(), source.readInt());
        }

        @Override
        public AppIDBase[] newArray(int size) {
            return new AppIDBase[size];
        }
    };


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppIDBase appIDBase = (AppIDBase) o;

        return uid == appIDBase.uid && appType == appIDBase.appType &&
                startFlags == appIDBase.startFlags &&
                (packageName != null ? packageName.equals(appIDBase.packageName) : appIDBase.packageName == null);

    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + appType;
        result = 31 * result + startFlags;
        return result;
    }

    @Override
    public String toString() {
        return "AppIDBase{" +
                "uid=" + uid +
                ", packageName='" + packageName + '\'' +
                ", appType=" + appType +
                ", startFlags=" + startFlags +
                '}';
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", getUid());
        jsonObject.put("packageName", getPackageName());
        jsonObject.put("appType", getAppType());
        jsonObject.put("startFlags", getStartFlags());
        return jsonObject;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("uid", getUid());
        values.put("packageName", getPackageName());
        values.put("appType", getAppType());
        values.put("startFlags", getStartFlags());
        return values;
    }

}
