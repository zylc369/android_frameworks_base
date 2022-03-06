package android.bw.db.base;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 对应BWDump表中的数据。
 * Created by asherli on 16/2/18.
 */
public class BWDumpBase implements Parcelable {

    /**
     * 无效。
     * 通常的原因是查询失败，或者还未设置bwDumpFlags标志。
     */
    public static final int BW_DUMP_FLAGS_INVALID = (1 << 31);
    public static final int BW_DUMP_FLAGS_DISABLE = 0;
    public static final int BW_DUMP_FLAGS_PRINT = 1;
    public static final int BW_DUMP_FLAGS_WRITE_FILE = (1 << 1);
    public static final int BW_DUMP_FLAGS_ALL = BW_DUMP_FLAGS_PRINT | BW_DUMP_FLAGS_WRITE_FILE;

    public AppIDBase appIDBase = null;
    public int bwDumpFlags = BW_DUMP_FLAGS_ALL;

    public BWDumpBase() {}

    public BWDumpBase(Context context, String packageName, int bwDumpFlags) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
        this.appIDBase = new AppIDBase(ai.uid, packageName, AppIDBase.APP_TYPE_ANDROID_APP,
                AppIDBase.START_FLAG_UNSET);
        this.bwDumpFlags = bwDumpFlags;
    }

    protected BWDumpBase(Parcel in) {
        appIDBase = in.readParcelable(AppIDBase.class.getClassLoader());
        bwDumpFlags = in.readInt();
    }

    public static final Creator<BWDumpBase> CREATOR = new Creator<BWDumpBase>() {
        @Override
        public BWDumpBase createFromParcel(Parcel in) {
            return new BWDumpBase(in);
        }

        @Override
        public BWDumpBase[] newArray(int size) {
            return new BWDumpBase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(appIDBase, flags);
        dest.writeInt(bwDumpFlags);
    }

    public static BWDumpBase toBWDumpBase(Cursor cursor) {
        BWDumpBase bwDumpBase = new BWDumpBase();
        bwDumpBase.appIDBase = new AppIDBase(cursor.getInt(cursor.getColumnIndex("uid")),
                cursor.getString(cursor.getColumnIndex("packageName")),
                cursor.getInt(cursor.getColumnIndex("appType")),
                cursor.getInt(cursor.getColumnIndex("startFlags")));
        bwDumpBase.bwDumpFlags = cursor.getInt(cursor.getColumnIndex("bwDumpFlags"));
        return bwDumpBase;
    }

    public static BWDumpBase toBWDumpBase(ContentValues values) {
        BWDumpBase bwDumpBase = new BWDumpBase();
        bwDumpBase.appIDBase = new AppIDBase(values.getAsInteger("uid"),
                values.getAsString("packageName"), values.getAsInteger("appType"),
                values.getAsInteger("startFlags"));
        bwDumpBase.bwDumpFlags = values.getAsInteger("bwDumpFlags");
        return bwDumpBase;
    }

    public static Cursor toCursor(BWDumpBase bwDumpBase) {
        if (null == bwDumpBase) return null;
        MatrixCursor matrixCursor = new MatrixCursor(
                new String[] {
                        "uid", "packageName", "bwDumpFlags"
                }, 1);
        matrixCursor.addRow(new Object[] {
            bwDumpBase.appIDBase.getUid(), bwDumpBase.appIDBase.getPackageName(), bwDumpBase.bwDumpFlags});
        return matrixCursor;
    }
}
