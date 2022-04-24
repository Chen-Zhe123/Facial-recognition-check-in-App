package com.firefly.face1.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.firefly.face1.DataBase.DBOpenHelper.FEATURE_TABLE_NAME;
import static com.firefly.face1.DataBase.DBOpenHelper.FIELD_TABLE_NAME;

public class FieldTable {

    private SQLiteDatabase mDatabase;
    private Context mContext;
    private DBOpenHelper mDBOpenHelper;

    private static final String TAG = "FieldTable";
    private boolean allowTransaction = true;
    private Lock writeLock = new ReentrantLock();
    private volatile boolean writeLocked = false;

    public FieldTable(Context context) {
        mContext = context;
    }

    public void setDatabase(SQLiteDatabase db) {
        mDatabase = db;
    }

    public boolean initField(){
        if (mDatabase == null) {
            return false;

        }
        ContentValues cv = new ContentValues();
        cv.put("field","group_1");
        cv.put("in_using",0);
        if(mDatabase.insert("field", null, cv) < 0){
            Log.e(TAG, "init失败**************************************" +
                    "***************************************");
            return false;
        }
        cv.clear();
        cv.put("field","group_2");
        cv.put("in_using",0);
        if(mDatabase.insert(FIELD_TABLE_NAME, null, cv) < 0){
            return false;
        }        cv.clear();
        cv.put("field","group_3");
        cv.put("in_using",0);
        if(mDatabase.insert(FIELD_TABLE_NAME, null, cv) < 0){
            return false;
        }        cv.clear();
        cv.put("field","group_4");
        cv.put("in_using",0);
        if(mDatabase.insert(FIELD_TABLE_NAME, null, cv) < 0){
            return false;
        }        cv.clear();
        cv.put("field","group_5");
        cv.put("in_using",0);
        if(mDatabase.insert(FIELD_TABLE_NAME, null, cv) < 0){
            return false;
        }        cv.clear();
        cv.put("field","group_6");
        cv.put("in_using",0);
        if(mDatabase.insert(FIELD_TABLE_NAME, null, cv) < 0){
            return false;
        }        cv.clear();
        cv.put("field","group_7");
        cv.put("in_using",0);
        if(mDatabase.insert(FIELD_TABLE_NAME, null, cv) < 0){
            return false;
        }        cv.clear();
        cv.put("field","group_8");
        cv.put("in_using",0);
        if(mDatabase.insert(FIELD_TABLE_NAME, null, cv) < 0){
            return false;
        }
        return true;
    }

    public String getIdleField(){
        if (mDatabase == null) {
            return null;
        }
        String idleField;
        Cursor cursor = null;
        cursor = mDatabase.query(FIELD_TABLE_NAME, null, null, null,
                null, null, null);
        if(cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex("in_using")) == 0) {
                    idleField = cursor.getString(cursor.getColumnIndex("field"));
                    return idleField;
                }
            }
        }
        return null;
    }

    public boolean setInUsing(String field,int using){
        if (mDatabase == null) {
            return false;
        }
//        beginTransaction(mDatabase);
        ContentValues cv = new ContentValues();
        cv.put("field", field);
        cv.put("in_using", using);
        String where = "field = ? ";
        String[] whereValue = { field };
        if (mDatabase.update(FIELD_TABLE_NAME,cv, where, whereValue) < 0) {
            return false;
        }
//        endTransaction(mDatabase);
        return true;
    }
    public void setNotInUsing(String field){
        ContentValues cv = new ContentValues();
        String where = " field=?";
        String[] whereValue = { field};
        cv.put("in_using", 0);
        mDatabase.update(FIELD_TABLE_NAME,cv, where, whereValue);
    }
}

