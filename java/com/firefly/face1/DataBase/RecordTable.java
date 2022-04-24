package com.firefly.face1.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.baidu.aip.utils.FileUitls;
import com.firefly.face1.bean.AllRecord;
import com.firefly.face1.bean.Feature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.firefly.face1.DataBase.DBOpenHelper.RECORD_TABLE_NAME;

public class RecordTable {
    private static SQLiteDatabase mDatabase;
    private Context mContext;
    private DBOpenHelper mDBOpenHelper;

    private static final String TAG = "RecordTable";

    public RecordTable(Context context){
        mContext = context;
    }

    public void setDatabase(SQLiteDatabase db){
        mDatabase = db;
    }

    public boolean addRecord(String userId,long time) {
        if (mDatabase == null) {
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put("user_id",userId);
        cv.put("capture_time",time);
//        cv.put("feature", record.getFeature());
//        cv.put("user_id", record.getUserId());
//        cv.put("student_id", record.getStudentId());
//        cv.put("capture_time", System.currentTimeMillis());
//        cv.put("user_name", record.getUserName());
        long pos = mDatabase.insert(RECORD_TABLE_NAME, null, cv);
        if ( pos < 0) {
            return false;
        }
        return true;
    }

    private final List<AllRecord> recordList = new ArrayList<>();
    private List<Feature> featureList = new ArrayList<>();

    public synchronized List<AllRecord> queryAllRecord() {
        recordList.removeAll(recordList);
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return recordList;
            }
            cursor = mDatabase.query(RECORD_TABLE_NAME, null, null,
                    null, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                long capture = cursor.getLong(cursor.getColumnIndex("capture_time"));
                String userId = cursor.getString(cursor.getColumnIndex("user_id"));
//                String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
//                byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
//                String imageName = cursor.getString(cursor.getColumnIndex("user_name"));

                AllRecord record = new AllRecord();
                featureList = DBMaster.getInstance().mFeatureTable.queryFeatureById(userId);
                if(featureList.size() > 0) {
                    record.setStudentId(featureList.get(0).getStudentId());
                    record.setUserName(featureList.get(0).getUserName());
                    File faceDir = FileUitls.getFaceDirectory();
                    record.setImageRes(BitmapFactory.decodeFile(
                            faceDir.getPath() + "/" + userId));
                    record.setCaptureTime(capture);
                    recordList.add(record);
                }
            }
//            mDatabase.close();
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return recordList;
    }

    public Boolean cleanAllRecord(){
        return mDatabase.delete(RECORD_TABLE_NAME,null,null) > 0;
    }
}
