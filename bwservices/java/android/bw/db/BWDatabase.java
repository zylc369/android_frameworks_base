package android.bw.db;

import android.annotation.TargetApi;
import android.bw.BWCommon;
import android.bw.BWLog;
import android.bw.db.base.AppIDBase;
import android.bw.db.base.BWDumpBase;
import android.bw.db.base.HookMethodInstInfoBase;
import android.bw.db.base.MethodIDBase;
import android.bw.db.base.TraceMethodInfoBase;
import android.bw.exception.BWDatabaseException;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * BW数据库操作类。
 * Created by asherli on 16/1/8.
 */
public class BWDatabase {

    public static final String TABLE_NAME_GLOBALS = "Globals";
    public static final String TABLE_NAME_APPID = "AppID";
    public static final String TABLE_NAME_METHODID = "MethodID";
    public static final String TABLE_NAME_TRACEMETHODINFO = "TraceMethodInfo";
    public static final String TABLE_NAME_HOOKMETHODINSTINFO = "HookMethodInstInfo";
    public static final String TABLE_NAME_BWDUMP = "BWDump";

    private File mDBFile;
    private SQLiteDatabase db = null;

    public BWDatabase() {
        this(BWCommon.BW_DB_PATH);
    }

    public BWDatabase(String dababasePath) {
        mDBFile = new File(dababasePath);
        createDatabase();
    }

    /**
     * 创建数据库。
     */
    private void createDatabase() {
        boolean isNewDB = !mDBFile.exists();
        db = SQLiteDatabase.openOrCreateDatabase(mDBFile, null);
        if (isNewDB) {
            // 创建表。

            /**
            create table Globals(
                    isVerifyApplicationSignature BOOLEAN NOT NULL UNIQUE DEFAULT 1
            )
            */
            db.execSQL("create table " + TABLE_NAME_GLOBALS + "(" +
                "isVerifyApplicationSignature BOOLEAN NOT NULL UNIQUE DEFAULT 1)");
            db.execSQL("INSERT INTO " + TABLE_NAME_GLOBALS + "(isVerifyApplicationSignature) VALUES (1)");

            /**
             create table AppID(
             id integer PRIMARY KEY autoincrement NOT NULL UNIQUE,
             packageName text NOT NULL UNIQUE,
             uid integer NOT NULL UNIQUE,
             appType integer NOT NULL,
             startFlags integer NOT NULL)

             packageName是唯一的。
             */
            db.execSQL("create table " + TABLE_NAME_APPID + "(" +
                    "id integer PRIMARY KEY autoincrement NOT NULL UNIQUE," +
                    "packageName text NOT NULL UNIQUE," +
                    "uid integer NOT NULL UNIQUE," +
                    "appType integer NOT NULL," +
                    "startFlags integer NOT NULL)");
            /**
             create table MethodID (
             id integer PRIMARY KEY autoincrement NOT NULL UNIQUE,
             classDesc text NOT NULL,
             methodName text NOT NULL,
             methodSig text NOT NULL,
             hash integer NOT NULL UNIQUE
             )

             hash是唯一的。
             */
            db.execSQL("create table " + TABLE_NAME_METHODID + " (" +
                    "id integer PRIMARY KEY autoincrement NOT NULL UNIQUE," +
                    "classDesc text NOT NULL," +
                    "methodName text NOT NULL," +
                    "methodSig text NOT NULL," +
                    "hash integer NOT NULL UNIQUE" +
                    ")");
            /**
             create table TraceMethodInfo(
             id integer PRIMARY KEY autoincrement NOT NULL UNIQUE,
             appID integer NOT NULL,
             methodID integer NOT NULL,
             traceMethodFlags integer NOT NULL,
             granularity integer NOT NULL,
             promptMethodType integer NOT NULL,
             FOREIGN KEY(appID) REFERENCES AppID(id),
             FOREIGN KEY(methodID) REFERENCES MethodID(id)
             )
             */
            db.execSQL("create table " + TABLE_NAME_TRACEMETHODINFO + "(" +
                    "id integer PRIMARY KEY autoincrement NOT NULL UNIQUE," +
                    "appID integer NOT NULL," +
                    "methodID integer NOT NULL," +
                    "traceMethodFlags integer NOT NULL," +
                    "granularity integer NOT NULL," +
                    "promptMethodType integer NOT NULL," +
                    "FOREIGN KEY(appID) REFERENCES AppID(id)," +
                    "FOREIGN KEY(methodID) REFERENCES MethodID(id)" +
                    ")");
            /**
             create table HookMethodInstInfo(
             id integer PRIMARY KEY autoincrement NOT NULL UNIQUE,
             appID integer NOT NULL,
             methodID integer NOT NULL,
             instLineNum integer NOT NULL,
             dexPC integer NOT NULL,
             content text,
             FOREIGN KEY(appID) REFERENCES AppID(id),
             FOREIGN KEY(methodID) REFERENCES MethodID(id)
             )
             */
            db.execSQL("create table " + TABLE_NAME_HOOKMETHODINSTINFO + "(" +
                    "id integer PRIMARY KEY autoincrement NOT NULL UNIQUE," +
                    "appID integer NOT NULL," +
                    "methodID integer NOT NULL," +
                    "instLineNum integer NOT NULL," +
                    "dexPC integer NOT NULL," +
                    "content text," +
                    "FOREIGN KEY(appID) REFERENCES AppID(id)," +
                    "FOREIGN KEY(methodID) REFERENCES MethodID(id)" +
                    ")");
            /**
             create table BWDump(
             id integer PRIMARY KEY autoincrement NOT NULL UNIQUE,
             appID integer NOT NULL UNIQUE,
             bwDumpFlags integer NOT NULL,
             FOREIGN KEY(appID) REFERENCES AppID(id)
             )
             */
            db.execSQL("create table " + TABLE_NAME_BWDUMP + "(" +
                    "id integer PRIMARY KEY autoincrement NOT NULL UNIQUE," +
                    "appID integer NOT NULL UNIQUE," +
                    "bwDumpFlags integer NOT NULL," +
                    "FOREIGN KEY(appID) REFERENCES AppID(id)" +
                    ")");
        }
    }

    /**
     * 向数据库插入或更新TraceMethodInfoBase。
     * 这个方法开启了一个数据库事务，当插入或更新失败时会回滚。
     * @param traceMethodInfoBase TraceMethodInfoBase对象。
     * @return 插入或更新成功，则返回true；当不需要插入或更新时，则返回true；插入或更新失败，则返回false。
     */
    // TODO: 当一个APP被卸载，AppID中的uid和包名也就没有了意义；当一个APP被卸载后重新安装，那么这个APP的uid通常都与之前安装时不同，这个怎么解决？
    public boolean insertOrUpdateTraceMethodInfo(TraceMethodInfoBase traceMethodInfoBase) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateTraceMethodInfo(TraceMethodInfoBase) - 数据库未打开！");
            return false;
        }
        if (null == traceMethodInfoBase) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateTraceMethodInfo(TraceMethodInfoBase) - " +
                    "参数traceMethodInfoBase为null。");
            return false;
        }
        boolean isSuccess = false;
        int columnIndex;
        // 开始事务。
        db.beginTransaction();
        Cursor cursorAppID = null;
        Cursor cursorMethodID = null;
        Cursor cursorTraceMethodInfo = null;
        try {
            do {
                cursorAppID = insertOrUpdateAppID(traceMethodInfoBase.methodLocation.appIDBase);
                if (null == cursorAppID) {
                    break;
                }
                if (!cursorAppID.moveToFirst()) {
                    BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_APPID + "\"表，移动游标到第一行失败！");
                    break;
                }

                cursorMethodID = insertToMethodID(traceMethodInfoBase.methodLocation.methodIDBase);
                if (null == cursorMethodID) {
                    break;
                }
                if (!cursorMethodID.moveToFirst()) {
                    BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_METHODID + "\"表，移动游标到第一行失败！");
                    break;
                }

                if (-1 == (columnIndex = cursorAppID.getColumnIndex("id"))) {
                    BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_APPID + "\"表，获得列\"id\"的索引失败！");
                    break;
                }
                int appID = cursorAppID.getInt(columnIndex);
                if (appID < 0) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateTraceMethodInfo - \"" +
                            TABLE_NAME_APPID + "\"表，获得的\"id\"值小于0！");
                    break;
                }

                if (-1 == (columnIndex = cursorMethodID.getColumnIndex("id"))) {
                    BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_METHODID + "\"表，获得列\"id\"的索引失败！");
                    break;
                }
                int methodID = cursorMethodID.getInt(columnIndex);
                if (methodID < 0) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateTraceMethodInfo - \"" +
                            TABLE_NAME_METHODID + "\"表，获得的\"id\"值小于0！");
                    break;
                }

                // 先根据appID和MethodID查询数据。
                cursorTraceMethodInfo = db.query(true, TABLE_NAME_TRACEMETHODINFO,
                        null, "appID=? AND methodID=?",
                        new String[]{Integer.toString(appID), Integer.toString(methodID)},
                        null, null, null, null);
                if (cursorTraceMethodInfo.getCount() > 1 || cursorTraceMethodInfo.getCount() < 0) {
                    BWLog.e(BWCommon.TAG, "[-] 查询\"" + TABLE_NAME_TRACEMETHODINFO + "\"表获得的行数不对，" +
                            "行数：" + cursorTraceMethodInfo.getCount() + "。");
                    break;
                }

                ContentValues values = new ContentValues();
                values.put("appID", appID);
                values.put("methodID", methodID);
                values.put("traceMethodFlags", traceMethodInfoBase.traceMethodFlags);
                values.put("granularity", traceMethodInfoBase.granularity);
                values.put("promptMethodType", traceMethodInfoBase.promptMethodType);
                if (0 == cursorTraceMethodInfo.getCount()) {    // 数据不存在，插入一行。
                    if (-1 == db.insert(TABLE_NAME_TRACEMETHODINFO, null, values)) {
                        BWLog.e(BWCommon.TAG, "[-] 向\"" + TABLE_NAME_TRACEMETHODINFO + "\"表" +
                                "中插入数据失败！");
                        break;
                    }
                } else { // 数据存在。
                    if (!cursorTraceMethodInfo.moveToFirst()) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_TRACEMETHODINFO + "\"表，" +
                                "移动游标到第一行失败！");
                        break;
                    }
                    if (-1 == (columnIndex = cursorTraceMethodInfo.getColumnIndex("traceMethodFlags"))) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_TRACEMETHODINFO + "\"表，" +
                                "获得列\"traceMethodFlags\"的索引失败！");
                        break;
                    }
                    final int oldFlags = cursorTraceMethodInfo.getInt(columnIndex);
                    if (-1 == (columnIndex = cursorTraceMethodInfo.getColumnIndex("granularity"))) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_TRACEMETHODINFO + "\"表，" +
                                "获得列\"granularity\"的索引失败！");
                        break;
                    }
                    final int oldGranularity = cursorTraceMethodInfo.getInt(columnIndex);
                    if (-1 == (columnIndex = cursorTraceMethodInfo.getColumnIndex("promptMethodType"))) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_TRACEMETHODINFO + "\"表，" +
                                "获得列\"promptMethodType\"的索引失败！");
                        break;
                    }
                    final int oldPromptMethodType = cursorTraceMethodInfo.getInt(columnIndex);
                    if (oldFlags != traceMethodInfoBase.traceMethodFlags || oldGranularity != traceMethodInfoBase.granularity ||
                            oldPromptMethodType != traceMethodInfoBase.promptMethodType) {
                        // 更新数据。
                        int rowsAffectedNum = db.update(TABLE_NAME_TRACEMETHODINFO, values, "appID=? AND methodID=?",
                                new String[]{Integer.toString(appID), Integer.toString(methodID)});
                        if (1 != rowsAffectedNum) {
                            BWLog.e(BWCommon.TAG, "[-] " + TABLE_NAME_TRACEMETHODINFO + "表更新的行数不对！" +
                                    "更新的行数：" + rowsAffectedNum + "。");
                            break;
                        }
                    } else {
                        BWLog.e(BWCommon.TAG, "[-] " + TABLE_NAME_TRACEMETHODINFO + "表，插入或更新一个重复的数据。");
                        break;
                    }
                }
                isSuccess = true;
                db.setTransactionSuccessful();  // 设置事务成功标记。
            } while (false);
        } finally {
            if (null != cursorAppID) {
                cursorAppID.close();
            }
            if (null != cursorMethodID) {
                cursorMethodID.close();
            }
            if (null != cursorTraceMethodInfo) {
                cursorTraceMethodInfo.close();
            }
            // 结束事务。
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 删除TraceMethodInfo表中的数据。
     * @param packageName App包名。
     * @param hashCode 哈希。
     * @return 删除成功，则返回true；删除失败，则返回false。
     */
    public boolean deleteTraceMethodInfo(String packageName, int hashCode) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] deleteTraceMethodInfo(String,int) - 数据库未打开！");
            return false;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] deleteTraceMethodInfo(String,int) - 参数packageName为null。");
            return false;
        }
        /**
         DELETE FROM TraceMethodInfo WHERE id=(SELECT TraceMethodInfo.id FROM TraceMethodInfo
         JOIN AppID, MethodID ON
         TraceMethodInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         TraceMethodInfo.methodID=(SELECT id FROM MethodID WHERE hash=?)
         AND TraceMethodInfo.appID=AppID.id AND TraceMethodInfo.methodID=MethodID.id)
         */
        int deleteNum = db.delete(TABLE_NAME_TRACEMETHODINFO,
                "id=(SELECT TraceMethodInfo.id FROM TraceMethodInfo JOIN AppID, MethodID ON " +
                "TraceMethodInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "TraceMethodInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) " +
                "AND TraceMethodInfo.appID=AppID.id AND TraceMethodInfo.methodID=MethodID.id)",
                new  String[]{packageName, Integer.toString(hashCode)});
        return (0 != deleteNum);
    }

    /**
     * 查询TraceMethodInfo表中的数据。
     * @param packageName App包名。
     * @param hashCode 哈希。
     * @return 返回Cursor对象。
     */
    public TraceMethodInfoBase queryTraceMethodInfo(String packageName, int hashCode) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] queryTraceMethodInfo(String,int) - 数据库未打开！");
            return null;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] queryTraceMethodInfo(String,int) - 参数packageName为null。");
            return null;
        }
        TraceMethodInfoBase traceMethodInfoBase = null;
        /**
         SELECT TraceMethodInfo.traceMethodFlags, TraceMethodInfo.granularity,
         TraceMethodInfo.promptMethodType, MethodID.hash,
         AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags,
         MethodID.classDesc, MethodID.methodName, MethodID.methodSig
         FROM TraceMethodInfo JOIN AppID, MethodID ON
         TraceMethodInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         TraceMethodInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND
         TraceMethodInfo.appID=AppID.id AND TraceMethodInfo.methodID=MethodID.id
         */
        Cursor cursor = db.rawQuery(
                "SELECT TraceMethodInfo.traceMethodFlags, TraceMethodInfo.granularity, " +
                "TraceMethodInfo.promptMethodType, MethodID.hash, " +
                "AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags, " +
                "MethodID.classDesc, MethodID.methodName, MethodID.methodSig " +
                "FROM TraceMethodInfo JOIN AppID, MethodID ON " +
                "TraceMethodInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "TraceMethodInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND " +
                "TraceMethodInfo.appID=AppID.id AND TraceMethodInfo.methodID=MethodID.id",
                new  String[]{packageName, Integer.toString(hashCode)});

        do {
            if (1 != cursor.getCount()) {
                BWLog.e(BWCommon.TAG, "[-] 查询TraceMethodInfo数据获得的行数不对，行数：" + cursor.getCount() + "。");
                break;
            }
            if (!cursor.moveToFirst()) {
                BWLog.e(BWCommon.TAG, "[-] 将查询到的TraceMethodInfo数据游标，移动游标到第一行失败！");
                break;
            }
            traceMethodInfoBase = TraceMethodInfoBase.toTraceMethodInfoBase(cursor);
        } while (false);
        cursor.close();
        return traceMethodInfoBase;
    }

    /**
     * 查询与一个包相关的所有TraceMethodInfo数据。
     * @param packageName 包名。
     * @return 返回一个列表。
     */
    public List<TraceMethodInfoBase> queryAllTraceMethodInfo(String packageName) {
        List<TraceMethodInfoBase> traceMethodInfoBases = new ArrayList<>();
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] queryAllTraceMethodInfo(String) - 数据库未打开！");
            return traceMethodInfoBases;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] queryAllTraceMethodInfo(String) - 参数packageName为null。");
            return traceMethodInfoBases;
        }
        /**
         SELECT AppID.uid, TraceMethodInfo.traceMethodFlags, TraceMethodInfo.granularity,
         TraceMethodInfo.promptMethodType, MethodID.hash,
         AppID.packageName, AppID.appType, AppID.startFlags,
         MethodID.classDesc, MethodID.methodName, MethodID.methodSig
         FROM TraceMethodInfo JOIN AppID, MethodID ON
         TraceMethodInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         TraceMethodInfo.appID=AppID.id AND TraceMethodInfo.methodID=MethodID.id
         */
        Cursor cursor = db.rawQuery(
                "SELECT AppID.uid, TraceMethodInfo.traceMethodFlags, TraceMethodInfo.granularity, " +
                "TraceMethodInfo.promptMethodType, MethodID.hash, " +
                "AppID.packageName, AppID.appType, AppID.startFlags, " +
                "MethodID.classDesc, MethodID.methodName, MethodID.methodSig " +
                "FROM TraceMethodInfo JOIN AppID, MethodID ON " +
                "TraceMethodInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "TraceMethodInfo.appID=AppID.id AND TraceMethodInfo.methodID=MethodID.id",
                new  String[]{packageName});
        if (null == cursor || 0 == cursor.getCount()) {
            return traceMethodInfoBases;
        }
        if (!cursor.moveToFirst()) {
            BWLog.e(BWCommon.TAG, "[-] 将查询到的所有TraceMethodInfo数据游标移动游标到第一行失败！");
            return traceMethodInfoBases;
        }
        do {
            traceMethodInfoBases.add(TraceMethodInfoBase.toTraceMethodInfoBase(cursor));
        } while (cursor.moveToNext());
        cursor.close();
        return traceMethodInfoBases;
    }

    public boolean insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase hookMethodInstInfoBase) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase) - 数据库未打开！");
            return false;
        }
        if (null == hookMethodInstInfoBase) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo(HookMethodInstInfoBase) - " +
                    "参数hookMethodInstInfoBase为null。");
            return false;
        }
        boolean isSuccess = false;
        int columnIndex;
        // 开始事务。
        db.beginTransaction();
        Cursor cursorAppID = null;
        Cursor cursorMethodID = null;
        Cursor cursorHookMethodInstInfo = null;
        try {
            do {
                cursorAppID = insertOrUpdateAppID(hookMethodInstInfoBase.instructionLocation.appIDBase);
                if (null == cursorAppID) {
                    break;
                }
                if (!cursorAppID.moveToFirst()) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo - \"" +
                            TABLE_NAME_APPID + "\"表，移动游标到第一行失败！");
                    break;
                }

                cursorMethodID = insertToMethodID(hookMethodInstInfoBase.instructionLocation.methodIDBase);
                if (null == cursorMethodID) {
                    break;
                }
                if (!cursorMethodID.moveToFirst()) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo - \"" +
                            TABLE_NAME_METHODID + "\"表，移动游标到第一行失败！");
                    break;
                }

                if (-1 == (columnIndex = cursorAppID.getColumnIndex("id"))) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo - \"" +
                            TABLE_NAME_APPID + "\"表，获得列\"id\"的索引失败！");
                    break;
                }
                int appID = cursorAppID.getInt(columnIndex);
                if (appID < 0) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo - \"" +
                            TABLE_NAME_APPID + "\"表，获得的\"id\"值小于0！");
                    break;
                }

                if (-1 == (columnIndex = cursorMethodID.getColumnIndex("id"))) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo - \"" +
                            TABLE_NAME_METHODID + "\"表，获得列\"id\"的索引失败！");
                    break;
                }
                int methodID = cursorMethodID.getInt(columnIndex);
                if (methodID < 0) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo - \"" +
                            TABLE_NAME_METHODID + "\"表，获得的\"id\"值小于0！");
                    break;
                }

                // 先根据appID和MethodID查询数据。
                cursorHookMethodInstInfo = db.query(true, TABLE_NAME_HOOKMETHODINSTINFO,
                        null, "appID=? AND methodID=?",
                        new String[]{Integer.toString(appID), Integer.toString(methodID)},
                        null, null, null, null);
                if (cursorHookMethodInstInfo.getCount() > 1 || cursorHookMethodInstInfo.getCount() < 0) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateHookMethodInstInfo - 查询\"" +
                            TABLE_NAME_HOOKMETHODINSTINFO + "\"表获得的行数不对，" +
                            "行数：" + cursorHookMethodInstInfo.getCount() + "。");
                    break;
                }

                ContentValues values = new ContentValues();
                values.put("appID", appID);
                values.put("methodID", methodID);
                values.put("instLineNum", hookMethodInstInfoBase.instructionLocation.instLineNum);
                values.put("dexPC", hookMethodInstInfoBase.instructionLocation.dexPC);
                values.put("content", hookMethodInstInfoBase.contentData.toJSONText());
                if (0 == cursorHookMethodInstInfo.getCount()) {    // 数据不存在，插入一行。
                    if (-1 == db.insert(TABLE_NAME_HOOKMETHODINSTINFO, null, values)) {
                        BWLog.e(BWCommon.TAG, "[-] 向\"" + TABLE_NAME_HOOKMETHODINSTINFO +
                                "\"表中插入数据失败！");
                        break;
                    }
                } else { // 数据存在。
                    if (!cursorHookMethodInstInfo.moveToFirst()) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_HOOKMETHODINSTINFO + "\"表，移动游标到第一行失败！");
                        break;
                    }

                    if (-1 == (columnIndex = cursorHookMethodInstInfo.getColumnIndex("instLineNum"))) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_HOOKMETHODINSTINFO +
                                "\"表，获得列\"instLineNum\"的索引失败！");
                        break;
                    }
                    final long oldDexInstLineNum = cursorHookMethodInstInfo.getLong(columnIndex);

                    if (-1 == (columnIndex = cursorHookMethodInstInfo.getColumnIndex("dexPC"))) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_HOOKMETHODINSTINFO +
                                "\"表，获得列\"dexPC\"的索引失败！");
                        break;
                    }
                    final long oldDexPC = cursorHookMethodInstInfo.getLong(columnIndex);

                    if (oldDexInstLineNum != hookMethodInstInfoBase.instructionLocation.instLineNum ||
                            oldDexPC != hookMethodInstInfoBase.instructionLocation.dexPC) {
                        // 更新数据。
                        int rowsAffectedNum = db.update(TABLE_NAME_HOOKMETHODINSTINFO, values, "appID=? AND methodID=?",
                                new String[]{Integer.toString(appID), Integer.toString(methodID)});
                        if (1 != rowsAffectedNum) {
                            BWLog.e(BWCommon.TAG, "[-] " + TABLE_NAME_HOOKMETHODINSTINFO + "表更新的行数不对！" +
                                    "更新的行数：" + rowsAffectedNum + "。");
                            break;
                        }
                    } else {
                        BWLog.e(BWCommon.TAG, "[-] " + TABLE_NAME_HOOKMETHODINSTINFO + "表，插入或更新一个重复的数据。");
                        break;
                    }
                }
                isSuccess = true;
                db.setTransactionSuccessful();  // 设置事务成功标记。
            } while (false);
        } finally {
            if (null != cursorAppID) {
                cursorAppID.close();
            }
            if (null != cursorMethodID) {
                cursorMethodID.close();
            }
            if (null != cursorHookMethodInstInfo) {
                cursorHookMethodInstInfo.close();
            }
            // 结束事务。
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 删除HookMethodInstInfo表中的数据。
     * @param packageName App包名。
     * @param hashCode 哈希。
     * @return 删除成功，则返回true；删除失败，则返回false。
     */
    public boolean deleteHookMethodInstInfo(String packageName, int hashCode, long instLineNum) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] deleteHookMethodInstInfo(String,int) - 数据库未打开！");
            return false;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] deleteHookMethodInstInfo(String,int) - 参数packageName为null。");
            return false;
        }
        /**
         DELETE FROM HookMethodInstInfo WHERE id=(SELECT HookMethodInstInfo.id FROM HookMethodInstInfo
         JOIN AppID, MethodID ON
         HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND
         HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id) AND
         HookMethodInstInfo.instLineNum=?
         */
        int deleteNum = db.delete(TABLE_NAME_HOOKMETHODINSTINFO,
                "id=(SELECT HookMethodInstInfo.id FROM HookMethodInstInfo JOIN AppID, MethodID ON " +
                "HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND " +
                "HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id) AND " +
                "HookMethodInstInfo.instLineNum=?",
                new  String[]{packageName, Integer.toString(hashCode), Long.toString(instLineNum)});
        return (0 != deleteNum);
    }

    /**
     * 删除HookMethodInstInfo表中的数据。
     * @param packageName App包名。
     * @param hashCode 哈希。
     * @return 删除成功，则返回true；删除失败，则返回false。
     */
//    public boolean deleteHookMethodInstInfo(String packageName, int hashCode) {
//        /**
//         DELETE FROM HookMethodInstInfo WHERE id=(SELECT HookMethodInstInfo.id FROM HookMethodInstInfo
//         JOIN AppID, MethodID ON
//         HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
//         HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND
//         HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id)
//         */
//        int deleteNum = db.delete(TABLE_NAME_HOOKMETHODINSTINFO,
//                "id=(SELECT HookMethodInstInfo.id FROM HookMethodInstInfo JOIN AppID, MethodID ON " +
//                "HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
//                "HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND " +
//                "HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id)",
//                new  String[]{packageName, Integer.toString(hashCode)});
//        return (0 != deleteNum);
//    }

    /**
     * 查询HookMethodInstInfo表中的数据。
     * @param packageName App包名。
     * @param hashCode 哈希。
     * @return 返回Cursor对象。
     */
    public HookMethodInstInfoBase queryHookMethodInstInfo(String packageName, int hashCode, long instLineNum) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String,int,long) - 数据库未打开！");
            return null;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String,int,long) - 参数packageName为null。");
            return null;
        }
        HookMethodInstInfoBase traceMethodInfoBase = null;
        /**
         SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags,
         MethodID.classDesc, MethodID.methodName, MethodID.methodSig, MethodID.hash,
         HookMethodInstInfo.instLineNum, HookMethodInstInfo.dexPC, HookMethodInstInfo.content
         FROM HookMethodInstInfo JOIN AppID, MethodID ON
         HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND
         HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id AND
         HookMethodInstInfo.instLineNum=?
         */
        Cursor cursor = db.rawQuery(
                "SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags, " +
                "MethodID.classDesc, MethodID.methodName, MethodID.methodSig, MethodID.hash, " +
                "HookMethodInstInfo.instLineNum, HookMethodInstInfo.dexPC, HookMethodInstInfo.content " +
                "FROM HookMethodInstInfo JOIN AppID, MethodID ON " +
                "HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND " +
                "HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id AND " +
                "HookMethodInstInfo.instLineNum=?",
                new  String[]{packageName, Integer.toString(hashCode), Long.toString(instLineNum)});

        do {
            if (1 != cursor.getCount()) {
                BWLog.e(BWCommon.TAG, "[-] 查询HookMethodInstInfo数据获得的行数不对，行数：" + cursor.getCount() + "。");
                break;
            }
            if (!cursor.moveToFirst()) {
                BWLog.e(BWCommon.TAG, "[-] 将查询到的HookMethodInstInfo数据游标，移动游标到第一行失败！");
                break;
            }
            traceMethodInfoBase = HookMethodInstInfoBase.toHookMethodInstInfoBase(cursor);
        } while (false);
        cursor.close();
        return traceMethodInfoBase;
    }

    public List<HookMethodInstInfoBase> queryHookMethodInstInfo(String packageName, int hashCode) {
        List<HookMethodInstInfoBase> list = new ArrayList<>();
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String,int) - 数据库未打开！");
            return list;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String,int) - 参数packageName为null。");
            return list;
        }
        /**
         SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags,
         MethodID.classDesc, MethodID.methodName, MethodID.methodSig, MethodID.hash,
         HookMethodInstInfo.instLineNum, HookMethodInstInfo.dexPC, HookMethodInstInfo.content
         FROM HookMethodInstInfo JOIN AppID, MethodID ON
         HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND
         HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id
         */
        Cursor cursor = db.rawQuery(
                "SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags, " +
                "MethodID.classDesc, MethodID.methodName, MethodID.methodSig, MethodID.hash, " +
                "HookMethodInstInfo.instLineNum, HookMethodInstInfo.dexPC, HookMethodInstInfo.content " +
                "FROM HookMethodInstInfo JOIN AppID, MethodID ON " +
                "HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "HookMethodInstInfo.methodID=(SELECT id FROM MethodID WHERE hash=?) AND " +
                "HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id",
                new  String[]{packageName, Integer.toString(hashCode)});

        do {
            if (0 != cursor.getCount()) {
                if (!cursor.moveToFirst()) {
                    BWLog.e(BWCommon.TAG, "[-] 将查询到的HookMethodInstInfo数据游标，移动游标到第一行失败！");
                    break;
                }
                do {
                    list.add(HookMethodInstInfoBase.toHookMethodInstInfoBase(cursor));
                } while (cursor.moveToNext());
            }
        } while (false);
        cursor.close();
        return list;
    }

    public List<HookMethodInstInfoBase> queryHookMethodInstInfo(String packageName) {
        List<HookMethodInstInfoBase> list = new ArrayList<>();
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String) - 数据库未打开！");
            return list;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] queryHookMethodInstInfo(String) - 参数packageName为null。");
            return list;
        }
        /**
         SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags,
         MethodID.classDesc, MethodID.methodName, MethodID.methodSig, MethodID.hash,
         HookMethodInstInfo.instLineNum, HookMethodInstInfo.dexPC, HookMethodInstInfo.content
         FROM HookMethodInstInfo JOIN AppID, MethodID ON
         HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id
         */
        Cursor cursor = db.rawQuery(
                "SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags, " +
                "MethodID.classDesc, MethodID.methodName, MethodID.methodSig, MethodID.hash, " +
                "HookMethodInstInfo.instLineNum, HookMethodInstInfo.dexPC, HookMethodInstInfo.content " +
                "FROM HookMethodInstInfo JOIN AppID, MethodID ON " +
                "HookMethodInstInfo.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "HookMethodInstInfo.appID=AppID.id AND HookMethodInstInfo.methodID=MethodID.id",
                new  String[]{packageName});

        if (0 != cursor.getCount()) {
            if (!cursor.moveToFirst()) {
                BWLog.e(BWCommon.TAG, "[-] 将查询到的HookMethodInstInfo数据游标，移动游标到第一行失败！");
                return list;
            }

            do {
                list.add(HookMethodInstInfoBase.toHookMethodInstInfoBase(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /**
     * 向BWDump表中插入或更新数据。
     * @param bwDumpBase BWDumpBase对象。
     * @return 插入或更新成功，则返回true；否则，返回false。
     */
    public boolean insertOrUpdateBWDump(BWDumpBase bwDumpBase) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateBWDump(BWDumpBase) - 数据库未打开！");
            return false;
        }
        if (null == bwDumpBase) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateBWDump(BWDumpBase) - " +
                    "参数hookMethodInstInfoBase为null。");
            return false;
        }
        boolean isSuccess = false;
        int columnIndex;
        // 开始事务。
        db.beginTransaction();
        Cursor cursorAppID = null;
//        Cursor cursorBWDump = null;

        try {
            do {
                cursorAppID = insertOrUpdateAppID(bwDumpBase.appIDBase);
                if (null == cursorAppID) {
                    break;
                }
                if (!cursorAppID.moveToFirst()) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateBWDump - \"" +
                            TABLE_NAME_APPID + "\"表，移动游标到第一行失败！");
                    break;
                }

                if (-1 == (columnIndex = cursorAppID.getColumnIndex("id"))) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateBWDump - \"" +
                            TABLE_NAME_APPID + "\"表，获得列\"id\"的索引失败！");
                    break;
                }
                int appID = cursorAppID.getInt(columnIndex);
                if (appID < 0) {
                    BWLog.e(BWCommon.TAG, "[-] insertOrUpdateBWDump - \"" +
                            TABLE_NAME_APPID + "\"表，获得的\"id\"值小于0！");
                    break;
                }

                // 查询数据库中的数据。
                BWDumpBase bwDumpBaseInDB = queryBWDump(bwDumpBase.appIDBase.getPackageName());

                ContentValues values = new ContentValues();
                values.put("appID", appID);
                values.put("bwDumpFlags", bwDumpBase.bwDumpFlags);
                if (null == bwDumpBaseInDB) {    // 数据不存在，插入一行。
                    if (-1 == db.insert(TABLE_NAME_BWDUMP, null, values)) {
                        BWLog.e(BWCommon.TAG, "[-] 向\"" + TABLE_NAME_BWDUMP +
                                "\"表中插入数据失败！");
                        break;
                    }
                } else { // 数据存在。
                    final long oldFlags = bwDumpBaseInDB.bwDumpFlags;

                    if (oldFlags != bwDumpBase.bwDumpFlags) {
                        // 更新数据。
                        int rowsAffectedNum = db.update(TABLE_NAME_BWDUMP, values, "appID=?",
                                new String[]{Integer.toString(appID)});
                        if (1 != rowsAffectedNum) {
                            BWLog.e(BWCommon.TAG, "[-] " + TABLE_NAME_BWDUMP + "表更新的行数不对！" +
                                    "更新的行数：" + rowsAffectedNum + "。");
                            break;
                        }
                    } else {
                        BWLog.e(BWCommon.TAG, "[-] " + TABLE_NAME_BWDUMP + "表，插入或更新一个重复的数据。" +
                                "oldFlags=" + oldFlags + ", bwDumpBase.bwDumpFlags=" + bwDumpBase.bwDumpFlags);
                        break;
                    }
                }

                isSuccess = true;
                db.setTransactionSuccessful();  // 设置事务成功标记。
            } while (false);
        } finally {
            if (null != cursorAppID) {
                cursorAppID.close();
            }
            // 结束事务。
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 删除BWDump表中的数据。
     * @param packageName 包名。
     * @return 删除成功，则返回true；否则，返回false。
     */
    public boolean deleteBWDump(String packageName) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] deleteBWDump(String) - 数据库未打开！");
            return false;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] deleteBWDump(String) - 参数packageName为null。");
            return false;
        }
        /**
         DELETE FROM BWDump WHERE id=(SELECT BWDump.id FROM BWDump
         JOIN AppID ON
         BWDump.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         BWDump.appID=AppID.id)
         */
        int deleteNum = db.delete(TABLE_NAME_BWDUMP,
                "id=(SELECT BWDump.id FROM BWDump " +
                "JOIN AppID ON " +
                "BWDump.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "BWDump.appID=AppID.id)",
                new String[]{packageName});
        return (0 != deleteNum);
    }

    /**
     * 查询BWDump表中的数据。
     * @param packageName 包名。
     * @return 查询成功，则返回BWDumpBase对象；否则，返回null。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public BWDumpBase queryBWDump(String packageName) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] queryBWDump(String) - 数据库未打开！");
            return null;
        }
        if (null == packageName) {
            BWLog.e(BWCommon.TAG, "[-] queryBWDump(String) - 参数packageName为null。");
            return null;
        }
        /**
         SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags,
         BWDump.bwDumpFlags FROM BWDump JOIN AppID ON
         BWDump.appID=(SELECT id FROM AppID WHERE packageName=?) AND
         BWDump.appID=AppID.id
         */
        BWDumpBase bwDumpBase = null;
        try (Cursor cursor = db.rawQuery(
                "SELECT AppID.uid, AppID.packageName, AppID.appType, AppID.startFlags, " +
                "BWDump.bwDumpFlags FROM BWDump JOIN AppID ON " +
                "BWDump.appID=(SELECT id FROM AppID WHERE packageName=?) AND " +
                "BWDump.appID=AppID.id",
                new String[]{packageName})) {
            do {
                if (1 != cursor.getCount()) {
                    BWLog.e(BWCommon.TAG, "[-] 查询BWDump数据获得的行数不对，行数：" + cursor.getCount() + "。包名：" + packageName);
                    break;
                }
                if (!cursor.moveToFirst()) {
                    BWLog.e(BWCommon.TAG, "[-] 将查询到的BWDump数据游标，移动游标到第一行失败！");
                    break;
                }
                bwDumpBase = BWDumpBase.toBWDumpBase(cursor);
            } while (false);
        }

        return bwDumpBase;
    }

    /**
     * 对AppID表进行插入或更新。
     *
     * 方法根据uid从AppID表中查找数据，当一个APP被卸载然后重新安装时同样包名的APP
     * 在前后被安装时的uid通常是不同的，所以对于Android系统APP的uid虽然是唯一的，但是却是可变的。
     * @param appIDBase AppID对象。
     * @return 当对AppID表更新或插入成功时，则返回非空的Cursor对象；
     *         当AppID表不须要插入或更新且查询成功时，则返回非空的Cursor对象；
     *         当插入AppID表失败时，则返回null。
     */
    public Cursor insertOrUpdateAppID(AppIDBase appIDBase) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateAppID(AppIDBase) - 数据库未打开！");
            return null;
        }
        if (null == appIDBase) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateAppID(AppIDBase) - 参数appIDBase为null。");
            return null;
        }
        boolean isSuccess = false;
        int columnIndex;
        Cursor result = null;
        do {
            // 先查询。
            // SELECT * FROM AppID WHERE packageName=?
            Cursor cursorAppID = null;
            try {
                cursorAppID = db.query(true, TABLE_NAME_APPID, null, "packageName=?",
                        new String[]{appIDBase.getPackageName()},
                        null, null, null, null);
                if (cursorAppID.getCount() > 1 || cursorAppID.getCount() < 0) {
                    BWLog.e(BWCommon.TAG, "[-] 查询\"" + TABLE_NAME_APPID + "\"表获得的行数不对，行数："
                            + cursorAppID.getCount() + "。");
                    break;
                }
                if (0 == cursorAppID.getCount()) {  // 在表中没有相应的记录，插入这条记录。
                    // 向AppID表插入数据。
                    ContentValues values = appIDBase.toContentValues();
                    if (-1 == db.insert(TABLE_NAME_APPID, null, values)) {
                        BWLog.e(BWCommon.TAG, "[-] 向" + TABLE_NAME_APPID + "表插入数据失败！");
                    } else {
                        isSuccess = true;
                    }
                    break;
                } else {    // 找到了1条数据，判断这条数据是否需要更新。
                    if (!cursorAppID.moveToFirst()) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_APPID + "\"表，移动游标到第一行失败！");
                        break;
                    }
                    if (-1 == (columnIndex = cursorAppID.getColumnIndex("uid"))) {
                        BWLog.e(BWCommon.TAG, "[-] \"" + TABLE_NAME_APPID + "\"表，获得列\"uid\"的索引失败！");
                        break;
                    }

                    ContentValues values = null;
                    if (cursorAppID.getInt(columnIndex) != appIDBase.getUid()) {    // uid不匹配，所以需要更新uid。
//                        BWLog.d(BWCommon.TAG, "[*] 更新\"" + appIDBase.getPackageName()  +
//                                "\"对应的uid(" + cursorAppID.getInt(columnIndex) +
//                                "->" + appIDBase.getUid() + ")。");
                        values = new ContentValues();
                        values.put("uid", appIDBase.getUid());
                    }
                    if (cursorAppID.getInt(cursorAppID.getColumnIndex("appType")) !=
                            appIDBase.getUid()) {
                        if (null == values) {
                            values = new ContentValues();
                        }
                        values.put("appType", appIDBase.getAppType());
                    }
                    if (cursorAppID.getInt(cursorAppID.getColumnIndex("startFlags")) !=
                            appIDBase.getStartFlags()) {
                        if (null == values) {
                            values = new ContentValues();
                        }
                        values.put("startFlags", appIDBase.getStartFlags());
                    }
                    if (null == values) {   // 不需要更新。
                        break;
                    }
                    // UPDATE AppID SET XXX WHERE packageName=?
                    int rowsAffectedNum = db.update(TABLE_NAME_APPID, values, "packageName=?",
                            new String[]{appIDBase.getPackageName()});
                    if (1 != rowsAffectedNum) {
                        BWLog.e(BWCommon.TAG, "[-] " + TABLE_NAME_APPID + "表更新的行数不对！" +
                                "更新的行数：" + rowsAffectedNum + "。");
                        break;
                    } else {
                        isSuccess = true;
                    }
                }
            } finally {
                if (null != cursorAppID) {
                    cursorAppID.close();
                }
            }
        } while (false);
        if (isSuccess) {    // 当成功时，再次查询一次。
            do {
                Cursor cursorAppID = db.query(true, TABLE_NAME_APPID, null, "packageName=?",
                        new String[]{appIDBase.getPackageName()}, null, null, null, null);
                if (1 != cursorAppID.getCount()) {
                    BWLog.e(BWCommon.TAG, "[-] 查询\"" + TABLE_NAME_APPID + "\"表获得的行数不对，行数："
                            + cursorAppID.getCount() + "。");
                    break;
                }

                result = cursorAppID;
            } while (false);
        }
        return result;
    }

    /**
     * 对MethodID表进行插入。
     *
     * 方法根据哈希从MethodID表中查找数据，而哈希被认为是不可能重复的。
     * @param methodIDBase MethodID对象。
     * @return 插入成功，则返回非空的Cursor对象；
     *         当不需要插入并且查询成功时，则返回查询到的非空的Cursor对象；
     *         插入失败，则返回null。
     */
    private Cursor insertToMethodID(MethodIDBase methodIDBase) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateAppID(MethodIDBase) - 数据库未打开！");
            return null;
        }
        if (null == methodIDBase) {
            BWLog.e(BWCommon.TAG, "[-] insertOrUpdateAppID(MethodIDBase) - 参数methodIDBase为null。");
            return null;
        }
        Cursor result = null;
        // 先查询。
        Cursor cursorMethodID = db.query(true, TABLE_NAME_METHODID, null, "hash=?",
                new String[]{Integer.toString(methodIDBase.getHash())},
                null, null, null, null);
        if (0 == cursorMethodID.getCount()) {  // 在表中没有相应的记录，插入这条记录。
            // 向MethodID表插入数据。
            ContentValues values = methodIDBase.toContentValues();
            if (-1 == db.insert(TABLE_NAME_METHODID, null, values)) {
                BWLog.e(BWCommon.TAG, "[-] 向" + TABLE_NAME_METHODID + "表插入数据失败！");
            } else {    // 再次查询。
                cursorMethodID = db.query(true, TABLE_NAME_METHODID, null, "hash=?",
                        new String[]{Integer.toString(methodIDBase.getHash())},
                        null, null, null, null);
            }
        }

        if (1 == cursorMethodID.getCount()) {
            result = cursorMethodID;
        } else {
            BWLog.e(BWCommon.TAG, "[-] 查询\"" + TABLE_NAME_METHODID + "\"表获得的行数不对，行数："
                    + cursorMethodID.getCount() + "。");
        }
        return result;
    }

    /**
     * 获得AppID表中的startFlags列。
     * @param uid 应用UID。
     * @return 返回uid对应的startFlags值。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public int getStartFlagsInAppID(int uid) {
        try (Cursor cursor = db.query(
                BWDatabase.TABLE_NAME_APPID, new String[]{"startFlags"},
                "uid=?", new String[]{Integer.toString(uid)}, null, null, null)) {
            int count = cursor.getCount();
            if (0 == count) {
                BWLog.w(BWCommon.TAG, "[!] getStartFlagsInAppID - 未查到相关数据。uid=" + uid);
            } else if (count > 1) {
                BWLog.w(BWCommon.TAG, "[!] getStartFlagsInAppID - 查到相关数据行数过多。count=" + count);
            } else {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(cursor.getColumnIndex("startFlags"));
                } else {
                    BWLog.w(BWCommon.TAG, "[!] getStartFlagsInAppID - cursor.moveToFirst()失败。count=" + count);
                }
            }
        }
        return AppIDBase.START_FLAG_UNSET;
    }

    /**
     * 设置AppID表中的startFlags列。
     * @param uid 应用UID。
     * @param startFlags startFlags值。
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setStartFlags(int uid, int startFlags) throws BWDatabaseException {
        try (Cursor cursor = db.query(true, TABLE_NAME_APPID, null, "uid=?",
                new String[]{Integer.toString(uid)},
                null, null, null, null)) {
            if (1 == cursor.getCount()) {
                throw new BWDatabaseException("查询\"" + TABLE_NAME_APPID +
                        "\"表获得的行数不对，行数：" + cursor.getCount() + "。");
            }
            if (!cursor.moveToFirst()) {
                throw new BWDatabaseException("游标移动到第一行失败。");
            }
            if (startFlags == cursor.getInt(cursor.getColumnIndex("startFlags"))) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put("startFlags", startFlags);
            db.update(TABLE_NAME_APPID, values, "uid=?", new String[]{Integer.toString(uid)});
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isVerifyApplicationSignature() throws BWDatabaseException {
        String columnName = "isVerifyApplicationSignature";
        try (Cursor cursor = db.query(true, TABLE_NAME_GLOBALS, new String[] {columnName},
                null, null, null, null, null, null)) {
            int rowCount = cursor.getCount();
            if (1 != rowCount) {
                throw new BWDatabaseException("期望查询到1列，实际查询到：" + rowCount + "列");
            }
            if (!cursor.moveToFirst()) {
                throw new BWDatabaseException("游标移动到第一行失败。");
            }
            int value = cursor.getInt(cursor.getColumnIndex(columnName));
            return 0 != value;
        }
    }

    public void setVerifyApplicationSignature(boolean isVerifyApplicationSignature) {
        ContentValues values = new ContentValues();
        values.put("isVerifyApplicationSignature", isVerifyApplicationSignature);
        db.update(TABLE_NAME_GLOBALS, values, null, null);
    }

    /**
     * 删除数据库。
     */
    public boolean deleteDB() {
        close();

        if (!mDBFile.delete()) {
            BWLog.e(BWCommon.TAG, "[-] 删除数据库文件失败！");
            return false;
        }
        return true;
    }

    /**
     * 数据库文件是否存在。
     * @return 数据库文件存在，则返回true；数据库文件不存在，则返回false。
     */
    public boolean isExist() {
        if (null == mDBFile) {
            BWLog.e(BWCommon.TAG, "[-] BW数据库文件不存在 - null == mDBFile！");
            return false;
        }
        if (mDBFile.exists()) {
            return true;
        } else {
            BWLog.e(BWCommon.TAG, "[-] BW数据库文件不存在！");
            return false;
        }
    }

    /**
     * 关闭数据库。
     */
    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    /**
     * 获得数据库文件的绝对路径。
     * @return 数据库文件如果存在，则返回数据库文件的绝对路径；否则，返回null。
     */
    public String getAbsolutePath() {
        if (isExist()) {
            return mDBFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * 将数据库中所有表中的数据清除。
     */
    public void clearDatabase() {
        if (db.isOpen()) {
            // 注意清理顺序，有些表中有外键。
            db.delete(TABLE_NAME_BWDUMP, null, null);
            db.delete(TABLE_NAME_TRACEMETHODINFO, null, null);
            db.delete(TABLE_NAME_HOOKMETHODINSTINFO, null, null);
            db.delete(TABLE_NAME_METHODID, null, null);
            db.delete(TABLE_NAME_APPID, null, null);
            db.delete(TABLE_NAME_GLOBALS, null, null);
        }
    }

    /**
     * 重置数据库。
     * @return 重置成功，则返回true；重置失败，则返回false。
     */
    public boolean resetDatabase() {
        if (mDBFile.exists()) {
            if (!deleteDB()) {
                BWLog.e(BWCommon.TAG, "[-] 删除数据库失败！");
                return false;
            }
        }
        createDatabase();
        return db.isOpen();
    }

    public boolean clearWhenAppUninstall(int uid) {
        if (!db.isOpen()) {
            BWLog.e(BWCommon.TAG, "[-] deleteTraceMethodInfo(String,int) - 数据库未打开！");
            return false;
        }
        db.delete(TABLE_NAME_BWDUMP, "appID=?", new String[]{Integer.toString(uid)});
        db.delete(TABLE_NAME_TRACEMETHODINFO, "appID=?", new String[]{Integer.toString(uid)});
        db.delete(TABLE_NAME_HOOKMETHODINSTINFO, "appID=?", new String[]{Integer.toString(uid)});
        // TODO: 不删除MethodID表中的任何数据，程序需要每隔一段时间查找一下MethodID中不再使用的项然后删除。
        db.delete(TABLE_NAME_APPID, "id=?", new String[]{Integer.toString(uid)});
        return true;
    }

}
