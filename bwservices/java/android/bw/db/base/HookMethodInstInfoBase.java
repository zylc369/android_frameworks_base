package android.bw.db.base;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Hook方法指令信息。
 * Created by asherli on 16/1/8.
 */
public class HookMethodInstInfoBase implements Parcelable {

    public InstructionLocation instructionLocation;
    public ContentData contentData = new ContentData();

    public HookMethodInstInfoBase() {}

    public HookMethodInstInfoBase(JSONObject jsonObject) throws JSONException {
        ContentData contentData = ContentData.toContentData(jsonObject.getString("content"));
        if (null == contentData) {
            throw new JSONException("将content作为JSON内容进行解析失败！");
        }

        instructionLocation = new InstructionLocation(jsonObject);
        this.contentData = contentData;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(instructionLocation, flags);
        dest.writeParcelable(this.contentData, flags);
    }

    public static final Creator<HookMethodInstInfoBase> CREATOR = new Creator<HookMethodInstInfoBase>() {
        @Override
        public HookMethodInstInfoBase createFromParcel(Parcel source) {
            HookMethodInstInfoBase newHookMethodInstInfoBase = new HookMethodInstInfoBase();
            newHookMethodInstInfoBase.instructionLocation = source.readParcelable(
                    InstructionLocation.class.getClassLoader());
            newHookMethodInstInfoBase.contentData = source.readParcelable(ContentData.class.getClassLoader());
            return newHookMethodInstInfoBase;
        }

        @Override
        public HookMethodInstInfoBase[] newArray(int size) {
            return new HookMethodInstInfoBase[size];
        }
    };

//    @Override
//    public boolean equals(Object o) {
//        if (null == o) {
//            return false;
//        }
//
//        if (!(o instanceof HookMethodInstInfoBase)) {
//            return false;
//        }
//
//        HookMethodInstInfoBase other = (HookMethodInstInfoBase) o;
//        return this.instructionLocation.equals(other.instructionLocation) &&
//                this.contentData == other.contentData;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HookMethodInstInfoBase that = (HookMethodInstInfoBase) o;

        return (instructionLocation != null && instructionLocation.equals(that.instructionLocation)) &&
                (contentData != null && contentData.equals(that.contentData));

    }

    @Override
    public int hashCode() {
        int result = instructionLocation != null ? instructionLocation.hashCode() : 0;
        result = 31 * result + (contentData != null ? contentData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HookMethodInstInfoBase{" +
                "instructionLocation=" + instructionLocation +
                ", contentData=" + contentData +
                '}';
    }

    public static JSONObject toJSONObject(HookMethodInstInfoBase hookMethodInstInfoBase) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = toJSONObject(hookMethodInstInfoBase, jsonObject);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject toJSONObject(HookMethodInstInfoBase hookMethodInstInfoBase, JSONObject jsonObject)
            throws JSONException, NullPointerException {
        if (null == jsonObject) {
            throw new NullPointerException("参数2不能传入null值");
        }
        jsonObject.put("instructionLocation", hookMethodInstInfoBase.instructionLocation.toJSONObject());
        jsonObject.put("content", hookMethodInstInfoBase.contentData.toJSONText());
        return jsonObject;
    }

//    public static Cursor toCursor(HookMethodInstInfoBase base) {
//        if (null == base) return null;
//        MatrixCursor matrixCursor = new MatrixCursor(
//                new String[]{
//                        "uid", "packageName",
//                        "classDesc", "methodName", "methodSig", "hash",
//                        "instLineNum", "dexPC", "content"}, 1);
//        matrixCursor.addRow(new Object[]{
//                base.appIDBase.getUid(), base.appIDBase.getPackageName(),
//                base.methodIDBase.getClassDesc(), base.methodIDBase.getMethodName(),
//                base.methodIDBase.getMethodSig(), base.methodIDBase.getHash(),
//                base.instLineNum, base.dexPC, base.contentData});
//        return matrixCursor;
//    }
//
//    public static Cursor toCursor(List<HookMethodInstInfoBase> list) {
//        if (null == list || 0 == list.size()) return null;
//        MatrixCursor matrixCursor = new MatrixCursor(
//                new String[]{
//                        "uid", "packageName",
//                        "classDesc", "methodName", "methodSig", "hash",
//                        "instLineNum", "dexPC", "content"}, list.size());
//        for (HookMethodInstInfoBase base : list) {
//            matrixCursor.addRow(new Object[]{
//                    base.appIDBase.getUid(), base.appIDBase.getPackageName(),
//                    base.methodIDBase.getClassDesc(), base.methodIDBase.getMethodName(),
//                    base.methodIDBase.getMethodSig(), base.methodIDBase.getHash(),
//                    base.instLineNum, base.dexPC, base.contentData});
//        }
//        return matrixCursor;
//    }

    public static HookMethodInstInfoBase toHookMethodInstInfoBase(Cursor cursor) {
        int index = cursor.getColumnIndex("content");
        ContentData contentData = ContentData.toContentData(
                cursor.getString(index));
        if (null == contentData) {
            return null;
        }
        HookMethodInstInfoBase hookMethodInstInfoBase = new HookMethodInstInfoBase();
        AppIDBase appIDBase = new AppIDBase(
                cursor.getInt(cursor.getColumnIndex("uid")),
                cursor.getString(cursor.getColumnIndex("packageName")),
                cursor.getInt(cursor.getColumnIndex("appType")),
                cursor.getInt(cursor.getColumnIndex("startFlags")));
        MethodIDBase methodIDBase = new MethodIDBase(
                cursor.getString(cursor.getColumnIndex("classDesc")),
                cursor.getString(cursor.getColumnIndex("methodName")),
                cursor.getString(cursor.getColumnIndex("methodSig")));
        long instLineNum = cursor.getLong(cursor.getColumnIndex("instLineNum"));
        long dexPC = cursor.getLong(cursor.getColumnIndex("dexPC"));
        hookMethodInstInfoBase.instructionLocation = new InstructionLocation(
                appIDBase, methodIDBase, instLineNum, dexPC);
        hookMethodInstInfoBase.contentData = contentData;
        return hookMethodInstInfoBase;
    }

//    public static HookMethodInstInfoBase toHookMethodInstInfoBase(ContentValues values) {
//        ContentData contentData = ContentData.toContentData(values.getAsString("content"));
//        if (null == contentData) {
//            return null;
//        }
//        HookMethodInstInfoBase hookMethodInstInfoBase = new HookMethodInstInfoBase();
//        hookMethodInstInfoBase.appIDBase = new AppIDBase(
//                values.getAsInteger("uid"), values.getAsString("packageName"),
//                values.getAsInteger("appType"));
//        hookMethodInstInfoBase.methodIDBase = new MethodIDBase(values.getAsString("classDesc"),
//                values.getAsString("methodName"), values.getAsString("methodSig"));
//
//        hookMethodInstInfoBase.instLineNum = values.getAsLong("instLineNum");
//        hookMethodInstInfoBase.dexPC = values.getAsLong("dexPC");
//
//        hookMethodInstInfoBase.contentData = contentData;
//
//        return hookMethodInstInfoBase;
//    }

    public static HookMethodInstInfoBase create(Context context, String packageName,
        String classDesc, String methodName, String methodSig, long instLineNum,
        long dexPC, String contentData) {
        ApplicationInfo ai = null;
        PackageManager pm = context.getPackageManager();
        try {
            ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (null == ai) {
            return null;
        }

        ContentData cd = ContentData.toContentData(contentData);
        if (null == cd) {
            return null;
        }

        HookMethodInstInfoBase hookMethodInstInfoBase = new HookMethodInstInfoBase();
        AppIDBase appIDBase = new AppIDBase(ai.uid, packageName,
                AppIDBase.APP_TYPE_ANDROID_APP, AppIDBase.START_FLAG_UNSET);
        MethodIDBase methodIDBase = new MethodIDBase(classDesc, methodName, methodSig);

        hookMethodInstInfoBase.instructionLocation = new InstructionLocation(
                appIDBase, methodIDBase, instLineNum, dexPC);
        hookMethodInstInfoBase.contentData = cd;
        return hookMethodInstInfoBase;
    }

    public static class ContentData implements Parcelable {
        
        /**
         * 当Hook Dex路径为null时，表示调用的是当前dex中的方法。
         */
        public String hookDexPath = null;
        /**
         * Hook方法所属的类描述符。
         * 当调用当前dex中的方法，且是非静态方法时，此参数可以为null。
         */
        public String hookClassDesc = null;
        /**
         * Hook方法名。
         */
        public String hookMethodName = null;
        /**
         * Hook方法签名。
         */
        public String hookMethodSig = null;
        /**
         * Hook方法是否为静态方法。
         * true：静态方法；false：非静态方法。
         */
        public boolean isHookMethodStatic = false;
        /**
         * 当是静态方法时，这个字段无效；
         * 当Hook方法在其他的DEX中时，这个字段无效（因为this寄存器所引用的类型只可能是当前dex中的类型）。
         */
        public String thisRegister = null;
        /**
         * 参数寄存器，以逗号分隔。
         * 当不需要传入参数时将这个字段设置为null。
         */
        public String paramRegisters = null;
        /**
         * 返回寄存器。
         * 当不需要返回时将这个字段设置为null。
         */
        public String returnRegister = null;
        /**
         * hook方法的方法ID。这里指的是jmethodID。
         * 当Hook方法在其他dex中时，这个字段必须为有效值。
         * 这是一个运行时需要的状态，所以注释掉这个字段。
         */
//        public long methodID = 0;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.hookDexPath);
            dest.writeString(this.hookClassDesc);
            dest.writeString(this.hookMethodName);
            dest.writeString(this.hookMethodSig);
            dest.writeByte((byte) (this.isHookMethodStatic ? 1 : 0));
            dest.writeString(this.thisRegister);
            dest.writeString(this.paramRegisters);
            dest.writeString(this.returnRegister);
        }

        public static final Creator<ContentData> CREATOR = new Creator<ContentData>() {
            @Override
            public ContentData createFromParcel(Parcel source) {
                ContentData newContent = new ContentData();
                newContent.hookDexPath = source.readString();
                newContent.hookClassDesc = source.readString();
                newContent.hookMethodName = source.readString();
                newContent.hookMethodSig = source.readString();
                newContent.isHookMethodStatic = source.readByte() != 0;
                newContent.thisRegister = source.readString();
                newContent.paramRegisters = source.readString();
                newContent.returnRegister = source.readString();
                return newContent;
            }

            @Override
            public ContentData[] newArray(int size) {
                return new ContentData[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (null == o) {
                return false;
            }

            if (!(o instanceof ContentData)) {
                return false;
            }

            ContentData other = (ContentData) o;
            return this.hookDexPath.equals(other.hookDexPath) &&
                   this.hookClassDesc.equals(other.hookClassDesc) &&
                   this.hookMethodName.equals(other.hookMethodName) &&
                   this.hookMethodSig.equals(other.hookMethodSig) &&
                   this.isHookMethodStatic == other.isHookMethodStatic &&
                   this.thisRegister.equals(other.thisRegister) &&
                   this.paramRegisters.equals(other.paramRegisters) &&
                   this.returnRegister.equals(other.returnRegister)
                   ;
        }

        @Override
        public String toString() {
            return "ContentData{" +
                    "hookDexPath='" + hookDexPath + '\'' +
                    ", hookClassDesc='" + hookClassDesc + '\'' +
                    ", hookMethodName='" + hookMethodName + '\'' +
                    ", hookMethodSig='" + hookMethodSig + '\'' +
                    ", isHookMethodStatic=" + isHookMethodStatic +
                    ", thisRegister='" + thisRegister + '\'' +
                    ", paramRegisters='" + paramRegisters + '\'' +
                    ", returnRegister='" + returnRegister + '\'' +
                    '}';
        }

        public String toJSONText() {
            String result = null;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("hookDexPath", hookDexPath);
                jsonObject.put("hookClassDesc", hookClassDesc);
                jsonObject.put("hookMethodName", hookMethodName);
                jsonObject.put("hookMethodSig", hookMethodSig);
                jsonObject.put("isHookMethodStatic", isHookMethodStatic);
                jsonObject.put("thisRegister", thisRegister);
                jsonObject.put("paramRegisters", paramRegisters);
                jsonObject.put("returnRegister", returnRegister);
                result = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        public static ContentData toContentData(String content) {
            ContentData contentData = null;
            try {
                do {
                    JSONObject jsonObject = new JSONObject(content);

                    String hookDexPath = jsonObject.getString("hookDexPath");
                    String hookClassDesc = jsonObject.getString("hookClassDesc");
                    if (null == hookClassDesc || 0 == hookClassDesc.length()) {
                        break;
                    }
                    String hookMethodName = jsonObject.getString("hookMethodName");
                    if (null == hookMethodName || 0 == hookMethodName.length()) {
                        break;
                    }
                    String hookMethodSig = jsonObject.getString("hookMethodSig");
                    if (null == hookMethodSig || 0 == hookMethodSig.length()) {
                        break;
                    }
                    boolean isHookMethodStatic = jsonObject.getBoolean("isHookMethodStatic");
                    String thisRegister = jsonObject.getString("thisRegister");
                    String paramRegisters = jsonObject.getString("paramRegisters");
                    String returnRegister = jsonObject.getString("returnRegister");

//                    long methodID = 0;
//                    if (null != hookDexPath && 0 != hookDexPath.length()) { // 表明Hook方法在其他DEX中。
//                        // TODO: 优化目录在/data/local/bw/dex这个统一的目录中如何哪？会不会造成优化时的文件冲突？
//                        File optimizedDirectory = context.getDir("bw_dexes", Context.MODE_PRIVATE);
//                        DexClassLoader dexClassLoader = new DexClassLoader(
//                                hookDexPath, optimizedDirectory.getAbsolutePath(), null,
//                                ClassLoader.getSystemClassLoader());
//                        methodID = BWUtils.getMethodID(dexClassLoader, BWUtils.classDescToClassName(hookClassDesc),
//                                hookMethodName, hookMethodSig, isHookMethodStatic);
//                        if (0 == methodID) {
//                            break;
//                        }
//
//                    }

                    contentData = new ContentData();
                    contentData.hookDexPath = hookDexPath;
                    contentData.hookClassDesc = hookClassDesc;
                    contentData.hookMethodName = hookMethodName;
                    contentData.hookMethodSig = hookMethodSig;
                    contentData.isHookMethodStatic = isHookMethodStatic;
                    contentData.thisRegister = thisRegister;
                    contentData.paramRegisters = paramRegisters;
                    contentData.returnRegister = returnRegister;
                } while (false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return contentData;
        }
    }

}
