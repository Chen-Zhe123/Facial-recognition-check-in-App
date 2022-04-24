package com.firefly.face1.DataBase;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBMaster {
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private DBOpenHelper mDBOpenHelper;
    public FeatureTable mFeatureTable;
    public RecordTable mRecordTable;
    public GroupTable mGroupTable;
    public GroupInfoTable mGroupInfoTable;
    public FieldTable mFieldTable;
    public ResultTable mResultTable;

    public static String FILTER = "group_0";//过滤条件

    private static DBMaster instance;

    public DBMaster(){
    }

    public void init(Context context) {
        if (context == null) {
            return;
        }
        mContext = context;
        mFeatureTable = new FeatureTable(mContext);
        mRecordTable = new RecordTable(mContext);
        mGroupTable = new GroupTable(mContext);
        mGroupInfoTable = new GroupInfoTable(mContext);
        mFieldTable = new FieldTable(mContext);
        mResultTable = new ResultTable(mContext);
        openDataBase();
    }

    public static synchronized DBMaster getInstance() {
            if (instance == null) {
                instance = new DBMaster();
            }
            return instance;
    }

    public void openDataBase(){
        mDBOpenHelper = new DBOpenHelper(mContext);
        try {
            mDatabase = mDBOpenHelper.getWritableDatabase();
        }catch(SQLException e){
            mDatabase = mDBOpenHelper.getReadableDatabase();
        }
        mFeatureTable.setDatabase(mDatabase);
        mRecordTable.setDatabase(mDatabase);
        mGroupTable.setDatabase(mDatabase);
        mGroupInfoTable.setDatabase(mDatabase);
        mFieldTable.setDatabase(mDatabase);
        mResultTable.setDatabase(mDatabase);
    }

    public void closeDataBase(){//设置回调接口，退出程序时关闭数据库
        if(mDatabase != null){
            mDatabase.close();
        }
    }
}
