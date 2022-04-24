package com.firefly.face1.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "myDataBase";
    private static final int VERSION = 1;
    public static final String FEATURE_TABLE_NAME = "feature";
    public static final String RECORD_TABLE_NAME = "record";
    public static final String GROUP_TABLE_NAME = "groups";
    public static final String GROUP_INFO_TABLE_NAME = "groupInfo";
    public static final String FIELD_TABLE_NAME = "field";
    public static final String RESULT_TABLE_NAME = "result";


    private static final String CREATE_TABLE_START_SQL = "CREATE TABLE IF NOT EXISTS ";
    private static final String CREATE_TABLE_PRIMARY_SQL = " integer primary key autoincrement,";

    private static final String createFeatureStr =
            CREATE_TABLE_START_SQL+FEATURE_TABLE_NAME + " ( " +
            " _id" + CREATE_TABLE_PRIMARY_SQL +
            " face_token"+" varchar(128) default \"\" ,"+
            " user_id" + " varchar(32) default \"\" ,"+
            " student_id" + " varchar(32) default \"\" ,"+
            " feature" + " blob   ,"+
            " user_name" + " varchar(64) default \"\"  ,"+
            " reg_time" + " long ,"+
            " update_time" + " long )";

    private static final String createRecordStr =
            CREATE_TABLE_START_SQL+RECORD_TABLE_NAME + " ( " +
            " _id" + CREATE_TABLE_PRIMARY_SQL +
            " user_id" + " varchar(32) default\"\","+
            " capture_time" + " long )";

    private static final String createGroupStr =
            CREATE_TABLE_START_SQL+GROUP_TABLE_NAME + "(" +
                    " _id" + CREATE_TABLE_PRIMARY_SQL +
                    " user_id" + " varchar(32) default\"\","+
                    " group_1" + " integer ," +
                    " group_2" + " integer ," +
                    " group_3" + " integer ," +
                    " group_4" + " integer ," +
                    " group_5" + " integer ," +
                    " group_6" + " integer ," +
                    " group_7" + " integer ," +
                    " group_8" + " integer )" ;//用户可创建组上限为8
    private static final String createGroupInfoStr =
            CREATE_TABLE_START_SQL+GROUP_INFO_TABLE_NAME + " ( " +
                    " _id" + CREATE_TABLE_PRIMARY_SQL +
                    " field" + " varchar(8)," +
                    " is_priority" + " integer,"+
                    " group_name" + " text," +
                    " group_description" + " text,"+
                    " is_null" + " integer)";
    private static final String createFieldStr =
            CREATE_TABLE_START_SQL+ FIELD_TABLE_NAME + " ( " +
                    " _id" + CREATE_TABLE_PRIMARY_SQL +
                    " field" + " varchar(8), " +
                    " in_using" + " integer)";
    private static final String createResultStr =
            CREATE_TABLE_START_SQL+ RESULT_TABLE_NAME + " ( " +
                    " _id" + CREATE_TABLE_PRIMARY_SQL +
                    " group_name" + " varchar(32)," +
                    " time" + " long," +
                    " need_sign" + " text," +
                    " already_sign" + " text," +
                    " late_sign" + " text," +
                    " never_sign" + " text)";

    private  static final String deleteFeatureStr = "DROP TABLE IF EXISTS " + FEATURE_TABLE_NAME;
    private  static final String deleteRecordStr = "DROP TABLE IF EXISTS " + RECORD_TABLE_NAME;
    private  static final String deleteGroupsStr = "DROP TABLE IF EXISTS " + GROUP_TABLE_NAME;
    private  static final String deleteGroupInfoStr = "DROP TABLE IF EXISTS " + GROUP_INFO_TABLE_NAME;
    private  static final String deleteFieldStr = "DROP TABLE IF EXISTS " + FIELD_TABLE_NAME;
    private  static final String deleteResultStr = "DROP TABLE IF EXISTS " + RESULT_TABLE_NAME;

    public DBOpenHelper(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createFeatureStr);
        db.execSQL(createRecordStr);
        db.execSQL(createGroupStr);
        db.execSQL(createGroupInfoStr);
        db.execSQL(createFieldStr);
        db.execSQL(createResultStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(deleteFeatureStr);
        db.execSQL(deleteRecordStr);
        db.execSQL(deleteGroupsStr);
        db.execSQL(deleteGroupInfoStr);
        db.execSQL(deleteFieldStr);
        db.execSQL(deleteResultStr);
        onCreate(db);
    }
}
