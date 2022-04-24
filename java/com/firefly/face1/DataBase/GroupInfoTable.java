package com.firefly.face1.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.firefly.face1.bean.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.firefly.face1.DataBase.DBOpenHelper.FEATURE_TABLE_NAME;
import static com.firefly.face1.DataBase.DBOpenHelper.GROUP_INFO_TABLE_NAME;

public class GroupInfoTable {
    private SQLiteDatabase mDatabase;
    private Context mContext;
    private DBOpenHelper mDBOpenHelper;

    private static final String TAG = "GroupInfoTable";
    private boolean allowTransaction = true;
    private Lock writeLock = new ReentrantLock();
    private volatile boolean writeLocked = false;

    public GroupInfoTable(Context context){
        mContext = context;
    }

    public void setDatabase(SQLiteDatabase db){
        mDatabase = db;
    }

    private void init(){

    }
    public boolean modifyGroup(String name,String description,String field){
        if (mDatabase == null) {
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put("group_name", name);
        cv.put("group_description", description);
        String where = "field = ? ";
        String[] whereValue = { field };
        if (mDatabase.update(GROUP_INFO_TABLE_NAME,cv, where, whereValue) < 0) {
            return false;
        }
        return true;
    }
    public boolean addGroup(Group group){
        String field;
        field = group.getField();
        ContentValues cv = new ContentValues();
        cv.put("field",field);
        cv.put("is_priority",group.getPriority());
        cv.put("group_name",group.getGroupName());
        cv.put("group_description",group.getGroupDescription());
        cv.put("is_null",group.getShow());
        long pos = mDatabase.insert(GROUP_INFO_TABLE_NAME, null, cv);
        DBMaster.getInstance().mFieldTable.setInUsing(field,1);
        if ( pos < 0) {
            return false;
        }
        return true;
    }
    public synchronized String queryGroupName(String field){
        if (mDatabase == null) {
            return null;
        }
        String groupName = null;
        Cursor cursor = null;
        String where = "field = ?";
        String[] whereValue = { field };
        try {
            cursor = mDatabase.query(GROUP_INFO_TABLE_NAME, null, where, whereValue,
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    groupName = cursor.getString(cursor.getColumnIndex("group_name"));
                }
            }
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return groupName;
    }
    private List<Group> groupList = new ArrayList<>();

    public List<Group> queryAllGroups(){
        groupList.remove(groupList);
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return groupList;
            }
            cursor = mDatabase.query(GROUP_INFO_TABLE_NAME, null, null,
                    null, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                String groupField = cursor.getString(cursor.getColumnIndex("field"));
                String groupName = cursor.getString(cursor.getColumnIndex("group_name"));
                String groupDescription = cursor.getString(cursor.getColumnIndex("group_description"));
                int isPriority = cursor.getInt(cursor.getColumnIndex("is_priority"));
                Group group = new Group();
                group.setGroupName(groupName);
                group.setGroupDescription(groupDescription);
                group.setPriority(isPriority);
                group.setField(groupField);
                groupList.add(group);
            }
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return groupList;
    }

    public void deleteGroup(String field){
        String where = " field=?";
        String[] whereValue = { field};
        mDatabase.delete(GROUP_INFO_TABLE_NAME, where, whereValue);
    }
    public void deleteALLGroup(){
        mDatabase.delete(GROUP_INFO_TABLE_NAME, null, null);
    }
}
