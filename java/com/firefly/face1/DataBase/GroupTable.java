package com.firefly.face1.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.firefly.face1.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.firefly.face1.DataBase.DBOpenHelper.FEATURE_TABLE_NAME;
import static com.firefly.face1.DataBase.DBOpenHelper.GROUP_INFO_TABLE_NAME;
import static com.firefly.face1.DataBase.DBOpenHelper.GROUP_TABLE_NAME;

public class GroupTable {
    private SQLiteDatabase mDatabase;
    private Context mContext;
    private DBOpenHelper mDBOpenHelper;

    private static final String TAG = "GroupTable";
    private boolean allowTransaction = true;
    private Lock writeLock = new ReentrantLock();
    private volatile boolean writeLocked = false;

    public GroupTable(Context context){
        mContext = context;
    }

    public void setDatabase(SQLiteDatabase db){
        mDatabase = db;
    }

    public Boolean initUserToTable(String id){
        ContentValues cv = new ContentValues();
        cv.put("user_id",id);
        long pos = mDatabase.insert(GROUP_TABLE_NAME, null, cv);
        if ( pos < 0) {
            return false;
        }
        return true;
    }
    public Boolean addUserToGroup(String field,List<String> idList){
        if (mDatabase == null) {
            Log.d(TAG, "addUserToGroup : mDatabase == null");
            return false;
        }
        for(String id :idList) {
            ContentValues cv = new ContentValues();
            cv.put(field,1);
            String where = "user_id = ? ";
            String[] whereValue = { id };
            if (mDatabase.update(GROUP_TABLE_NAME,cv, where, whereValue) < 0) {
                Log.d(TAG, "表更新失败");
                return false;
            }
        }
        return true;
    }

    public int queryCountByField(String field) {
        if (mDatabase == null) {
            Log.d(TAG, "queryCountByField : mDatabase == null");
            return 0;
        }
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(GROUP_TABLE_NAME, null, null, null,
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if(field.equals("group_0")){
                        count++;
                    }else {
                        if (cursor.getInt(cursor.getColumnIndex(field)) == 1) {
                            count++;
                        }
                    }
                }
                return count;
            }
        }catch (Exception pE){
                Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return count;
    }

    public synchronized List<String> queryIdListByField(String field) {
        List<String> userIdList = new ArrayList<>();
        userIdList.remove(userIdList);
        if (mDatabase == null) {
            return userIdList;
        }
        String userId ;
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(GROUP_TABLE_NAME, null, null, null,
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    userId = cursor.getString(cursor.getColumnIndex("user_id"));
                    if (field.equals("group_0")) {
                        userIdList.add(userId);
                    } else {
                        if (cursor.getInt(cursor.getColumnIndex(field)) == 1) {
                            userIdList.add(userId);
                        }
                    }
                }
            }
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return userIdList;
    }

    public void deleteRowById(String id){
        if (!TextUtils.isEmpty(id)) {
            String where = " user_id=?";
            String[] whereValue = { id};
            mDatabase.delete(GROUP_TABLE_NAME, where, whereValue);
        }
    }

    public void deleteUserByField(String field){//将对应列归零
        ContentValues cv = new ContentValues();
        cv.put(field, 0);
        mDatabase.update(GROUP_TABLE_NAME,cv, null, null);
    }

}
