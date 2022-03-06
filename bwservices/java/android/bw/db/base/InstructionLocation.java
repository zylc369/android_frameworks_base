package android.bw.db.base;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 指令位置。
 * Created by asherli on 16/1/19.
 */
public class InstructionLocation extends MethodLocation {

    /**
     * dex方法的指令行号。
     */
    public long instLineNum = -1;
    /**
     * dex方法的pc值。
     * 这个值应该是一个运行时的概念。
     */
    public long dexPC = -1;

    public InstructionLocation(AppIDBase appIDBase, MethodIDBase methodIDBase,
                               long instLineNum, long dexPC) {
        super(appIDBase, methodIDBase);
        this.instLineNum = instLineNum;
        this.dexPC = dexPC;
    }

    public InstructionLocation(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        this.instLineNum = jsonObject.getLong("instLineNum");
        this.dexPC = jsonObject.getLong("dexPC");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(instLineNum);
        dest.writeLong(dexPC);
    }

    public static final Creator<InstructionLocation> CREATOR = new Creator<InstructionLocation>() {
        @Override
        public InstructionLocation createFromParcel(Parcel source) {
            return new InstructionLocation(
                    (AppIDBase) source.readParcelable(AppIDBase.class.getClassLoader()),
                    (MethodIDBase) source.readParcelable(MethodIDBase.class.getClassLoader()),
                    source.readLong(), source.readLong()
            );
        }

        @Override
        public InstructionLocation[] newArray(int size) {
            return new InstructionLocation[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        InstructionLocation that = (InstructionLocation) o;

        return instLineNum == that.instLineNum && dexPC == that.dexPC;

    }

    @Override
    public int hashCode() {
        int result = (int) (instLineNum ^ (instLineNum >>> 32));
        result = 31 * result + (int) (dexPC ^ (dexPC >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "InstructionLocation{" +
                super.toString() + " " +
                "instLineNum=" + instLineNum +
                ", dexPC=" + dexPC +
                '}';
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = super.toJSONObject();
        jsonObject.put("instLineNum", instLineNum);
        jsonObject.put("dexPC", dexPC);
        return jsonObject;
    }
}
