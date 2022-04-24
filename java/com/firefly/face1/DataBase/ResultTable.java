package com.firefly.face1.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.firefly.face1.bean.PreviewResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.firefly.face1.DataBase.DBOpenHelper.GROUP_TABLE_NAME;
import static com.firefly.face1.DataBase.DBOpenHelper.RESULT_TABLE_NAME;

public class ResultTable {

    private Context mContext;
    private SQLiteDatabase mDatabase;
    private String TAG = "ResultTable";

    public ResultTable(Context context){
        mContext = context;
    }

    public void setDatabase(SQLiteDatabase db){
        mDatabase = db;
    }

    public Boolean addEvent(String field,long startTime,String needSignText,String alreadySignText,String lateSignText,String neverSignText){
        ContentValues cv = new ContentValues();
        cv.put("group_name",field);
        cv.put("time",startTime);
        cv.put("need_sign",needSignText);
        cv.put("already_sign",alreadySignText);
        cv.put("late_sign",lateSignText);
        cv.put("never_sign",neverSignText);
        long pos = mDatabase.insert(RESULT_TABLE_NAME, null, cv);
        if ( pos < 0) {
            return false;
        }
        return true;
    }
    private List<PreviewResult> preResultList = new ArrayList<>();

    /**
     * 查询所有签到事件的预览结果，通过对数据库中id和time进行字符分割处理，
     * 将其转存在List或Map中，并将数据传给PreviewResult的实例中，并生成该实例的List
     * @return preResultList
     */
    public List<PreviewResult> queryResult(){
        preResultList.remove(preResultList);
        if(mDatabase == null){
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(RESULT_TABLE_NAME, null, null, null,
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    PreviewResult preResult = new PreviewResult();
                    preResult.setGroupName(cursor.getString(cursor.getColumnIndex("group_name")));
                    preResult.setWorkTime(cursor.getLong(cursor.getColumnIndex("time")));

                    String needSignText = cursor.getString(cursor.getColumnIndex("need_sign"));
                    if(needSignText.length() != 0){
                        preResult.setNeedSignList(Arrays.asList(needSignText.split(",")));
                    }

                    Map<String, Long> alreadySignMap = new HashMap<>();
                    String alreadySignText = cursor.getString(cursor.getColumnIndex("already_sign"));
                    if(alreadySignText != null) {
                        String[] alreadySignString = alreadySignText.split("\\.");
                        if(alreadySignString.length > 0) {
                            for (String str : alreadySignString) {
                                if(str.split(",").length == 2) {
                                    alreadySignMap.put(str.split(",")[0], Long.valueOf(str.split(",")[1]));
                                }
                            }
                        }
                    }
                    preResult.setAlreadySignMap(alreadySignMap);

                    Map<String,Long> lateSignMap = new HashMap<>();
                    String lateSignText = cursor.getString(cursor.getColumnIndex("late_sign"));
                    if(lateSignText != null) {
                        String[] lateSignString = lateSignText.split("\\.");
                        if(lateSignString.length > 0) {
                            for (String str : lateSignString) {
                                if(str.split(",").length == 2) {
                                    lateSignMap.put(str.split(",")[0], Long.valueOf(str.split(",")[1]));
                                }
                            }
                        }
                    }
                    preResult.setLateSignMap(lateSignMap);

                    String neverSignText = cursor.getString(cursor.getColumnIndex("never_sign"));
                    //考虑到空字符串在分割后，长度为1
                    if(neverSignText.length() != 0) {
                        preResult.setNeverSignList(Arrays.asList(neverSignText.split(",")));
                    }
                    preResultList.add(preResult);
                }
            }
        }catch (Exception pE){
            Log.e("ResultTable", "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return preResultList;
    }

    //将签到开始时间作为签到事件的唯一标识
    public Boolean deleteEventByTime(long time){
        String where = " time=?";
        String[] whereValue = { String.valueOf(time)};
        return mDatabase.delete(RESULT_TABLE_NAME, where, whereValue) > 0;
    }

    //用于调试
    public Boolean cleanAllEvent(){
        return mDatabase.delete(RESULT_TABLE_NAME, null, null) > 0;
    }
}
