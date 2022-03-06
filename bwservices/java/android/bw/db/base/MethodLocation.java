package android.bw.db.base;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 方法位置。
 * Created by asherli on 16/1/18.
 */
public class MethodLocation implements Parcelable {

    public AppIDBase appIDBase = null;
    public MethodIDBase methodIDBase = null;

    public MethodLocation(AppIDBase appIDBase, MethodIDBase methodIDBase) {
        this.appIDBase = appIDBase;
        this.methodIDBase = methodIDBase;
    }
    public MethodLocation(JSONObject jsonObject) throws JSONException {
        this.appIDBase = new AppIDBase(jsonObject.getJSONObject("appID"));
        this.methodIDBase = new MethodIDBase(jsonObject.getJSONObject("methodID"));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(appIDBase, flags);
        dest.writeParcelable(methodIDBase, flags);
    }

    public static final Creator<MethodLocation> CREATOR = new Creator<MethodLocation>() {
        @Override
        public MethodLocation createFromParcel(Parcel source) {
            return new MethodLocation(
                    (AppIDBase) source.readParcelable(AppIDBase.class.getClassLoader()),
                    (MethodIDBase) source.readParcelable(MethodIDBase.class.getClassLoader()));
        }

        @Override
        public MethodLocation[] newArray(int size) {
            return new MethodLocation[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodLocation that = (MethodLocation) o;

        return (appIDBase != null && appIDBase.equals(that.appIDBase)) &&
                (methodIDBase != null && methodIDBase.equals(that.methodIDBase));

    }

    @Override
    public int hashCode() {
        int result = appIDBase != null ? appIDBase.hashCode() : 0;
        result = 31 * result + (methodIDBase != null ? methodIDBase.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MethodLocation{" +
                "appIDBase=" + appIDBase +
                ", methodIDBase=" + methodIDBase +
                '}';
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appID", appIDBase.toJSONObject());
        jsonObject.put("methodID", methodIDBase.toJSONObject());
        return jsonObject;
    }
}
