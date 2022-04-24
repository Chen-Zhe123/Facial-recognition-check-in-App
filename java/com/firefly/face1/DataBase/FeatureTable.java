package com.firefly.face1.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.firefly.face1.bean.Feature;
import com.firefly.face1.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.firefly.face1.DataBase.DBMaster.FILTER;
import static com.firefly.face1.DataBase.DBOpenHelper.FEATURE_TABLE_NAME;
import static com.firefly.face1.DataBase.DBOpenHelper.GROUP_TABLE_NAME;

public class FeatureTable {

    private SQLiteDatabase mDatabase;
    private Context mContext;
    private DBOpenHelper mDBOpenHelper;

    private static final String TAG = "FeatureTable";
    private boolean allowTransaction = true;
    private Lock writeLock = new ReentrantLock();
    private volatile boolean writeLocked = false;

    public FeatureTable(Context context){
        mContext = context;
    }

    public void setDatabase(SQLiteDatabase db){
        mDatabase = db;
    }

    public boolean addFeature(Feature feature) {//视频注册和图片注册通过此添加至主数据库
        if (mDatabase == null) {
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put("face_token", feature.getFaceToken());
        cv.put("feature", feature.getFeature());
        cv.put("user_id", feature.getUserId());
        cv.put("student_id", feature.getStudentId());
        cv.put("update_time", System.currentTimeMillis());
        cv.put("reg_time", System.currentTimeMillis());
        cv.put("user_name", feature.getUserName());
        long pos = mDatabase.insert(FEATURE_TABLE_NAME, null, cv);
        if ( pos < 0) {
            return false;
        }
        return true;
    }

    public boolean updateUserInfo(UserInfo userInfo){
        if (mDatabase == null) {
            return false;
        }
//        beginTransaction(mDatabase);
        ContentValues cv = new ContentValues();
        cv.put("student_id", userInfo.getStudentId());
        cv.put("user_name", userInfo.getUserName());
        String where = "user_id = ? ";
        String[] whereValue = { userInfo.getUserId() };
        if (mDatabase.update(FEATURE_TABLE_NAME,cv, where, whereValue) < 0) {//updata()函数返回更新受影响的行数
            return false;
        }
//        endTransaction(mDatabase);
        return true;
    }

    private final List<UserInfo> userInfoList = new ArrayList<>();

    public synchronized List<UserInfo> searchUser(String keyword) {
        userInfoList.removeAll(userInfoList);
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return userInfoList;
            }
            String where = "user_name = ?";
            if(keyword.length() == 13){
                where = "student_id = ?";
            }
            String[] whereValue = { keyword };
            cursor = mDatabase.query(FEATURE_TABLE_NAME, null, where, whereValue,
                    null, null, null);
            if(cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
                    String userName = cursor.getString(cursor.getColumnIndex("user_name"));
                    String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
                    long regTime = cursor.getLong(cursor.getColumnIndex("reg_time"));
                    UserInfo userInfo = new UserInfo();
                    userInfo.setFeature(featureContent);
                    userInfo.setRegTime(regTime);
                    userInfo.setStudentId(studentId);
                    userInfo.setUserName(userName);
                    userInfoList.add(userInfo);
                }
            }
//            mDatabase.close();//注意此处关闭了数据库
        }catch (Exception pE){

        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return userInfoList;
    }
//    public synchronized UserInfo queryUserInfoById(String userId){
//        UserInfo userInfo = null;
//        Cursor cursor = null;
//        try {
//            if (mDatabase == null) {
//                return null;
//            }
//            String where = "user_id = ? ";
//            String[] whereValue = { userId };
//            cursor = mDatabase.query(FEATURE_TABLE_NAME, null, where, whereValue,
//                    null, null, null);
//            if(cursor != null && cursor.getCount() > 0) {
//                while (cursor.moveToNext()) {
//                    String userName = cursor.getString(cursor.getColumnIndex("user_name"));
//                    long regTime = cursor.getLong(cursor.getColumnIndex("reg_time"));
//                    String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
//                    userInfo.setUserId(userId);
//                    userInfo.setUserName(userName);
//                    userInfo.setStudentId(studentId);
//                    userInfo.setRegTime(regTime);
//                }
//            }
////            mDatabase.close();
//        }catch (Exception pE){
//            Log.e(TAG, "pE = "+pE.getMessage());
//        }finally {
//            if (cursor!=null)
//                cursor.close();
//        }
//        return userInfo;
//    }

    public synchronized List<UserInfo> queryAllUserInfo() {
        userInfoList.removeAll(userInfoList);
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return userInfoList;
            }
            cursor = mDatabase.query(FEATURE_TABLE_NAME, null, null,
                    null, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
                String userId = cursor.getString(cursor.getColumnIndex("user_id"));
                String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
                long ctime = cursor.getLong(cursor.getColumnIndex("reg_time"));
                String imageName = cursor.getString(cursor.getColumnIndex("user_name"));
                UserInfo userInfo = new UserInfo();
                userInfo.setFeature(featureContent);
                userInfo.setRegTime(ctime);
                userInfo.setUserId(userId);
                userInfo.setStudentId(studentId);
                userInfo.setUserName(imageName);
                userInfoList.add(userInfo);
            }
//            mDatabase.close();
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return userInfoList;
    }

    private final List<Feature> featureList = new ArrayList<>();

    public synchronized List<Feature> queryFeatureById(String userId) {
        featureList.removeAll(featureList);
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return featureList;
            }
            String where = "user_id = ? ";
            String[] whereValue = { userId };
            cursor = mDatabase.query(FEATURE_TABLE_NAME, null, where, whereValue,
                    null, null, null);
            if(cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
                    String userName = cursor.getString(cursor.getColumnIndex("user_name"));
                    long updateTime = cursor.getLong(cursor.getColumnIndex("update_time"));
                    long regTime = cursor.getLong(cursor.getColumnIndex("reg_time"));
                    String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
                    Feature feature = new Feature();
                    feature.setFeature(featureContent);
                    feature.setRegTime(regTime);
                    feature.setUpdateTime(updateTime);
                    feature.setUserId(userId);
                    feature.setUserName(userName);
                    feature.setStudentId(studentId);
                    featureList.add(feature);
                }
            }
//            mDatabase.close();
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return featureList;
    }
    public synchronized List<UserInfo> queryUserInfoById(String userId) {
        List<UserInfo> userInfoList = new ArrayList<>();
        userInfoList.removeAll(userInfoList);
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return userInfoList;
            }
            String where = "user_id = ? ";
            String[] whereValue = { userId };
            cursor = mDatabase.query(FEATURE_TABLE_NAME, null, where, whereValue,
                    null, null, null);
            if(cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String userName = cursor.getString(cursor.getColumnIndex("user_name"));
                    long regTime = cursor.getLong(cursor.getColumnIndex("reg_time"));
                    String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
                    UserInfo userInfo = new UserInfo();
                    userInfo.setRegTime(regTime);
                    userInfo.setUserId(userId);
                    userInfo.setUserName(userName);
                    userInfo.setStudentId(studentId);
                    userInfoList.add(userInfo);
                }
            }
//            mDatabase.close();
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return userInfoList;
    }
    public synchronized List<Feature> queryAllFeature() {
        featureList.removeAll(featureList);
        idList = filterGroup();
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return featureList;
            }
            cursor = mDatabase.query(FEATURE_TABLE_NAME, null, null,
                    null, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                byte[] featureContent = cursor.getBlob(cursor.getColumnIndex("feature"));
                String userId = cursor.getString(cursor.getColumnIndex("user_id"));
                String studentId = cursor.getString(cursor.getColumnIndex("student_id"));
                long updateTime = cursor.getLong(cursor.getColumnIndex("update_time"));
                long ctime = cursor.getLong(cursor.getColumnIndex("reg_time"));
                String imageName = cursor.getString(cursor.getColumnIndex("user_name"));
                Feature feature = new Feature();
                feature.setFeature(featureContent);
                feature.setRegTime(ctime);
                feature.setUpdateTime(updateTime);
                feature.setUserId(userId);
                feature.setStudentId(studentId);
                feature.setUserName(imageName);
                if(FILTER.equals("group_0")) {
                    featureList.add(feature);
                } else{
                    if(idList.contains(userId)){
                        featureList.add(feature);
                    }
                }
            }
//            mDatabase.close();
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return featureList;
    }

    public List<String> idList = new ArrayList<>();

    public synchronized List<String> filterGroup(){
        idList.removeAll(idList);
        Cursor cursor = null;
        try {
            if (mDatabase == null) {
                return idList;
            }
            String where = "field = ? ";
            String[] whereValue = {"1"};
            cursor = mDatabase.query(GROUP_TABLE_NAME, null, where, whereValue,
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    idList.add(cursor.getString(cursor.getColumnIndex("user_id")));
                }
            }
        }catch (Exception pE){
            Log.e(TAG, "pE = "+pE.getMessage());
        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return idList;
    }
    public boolean deleteFeatureByUserId(String userId) {
        boolean success = false;
        try {
            beginTransaction(mDatabase);
            if (!TextUtils.isEmpty(userId)) {
                String where = " user_id=?";
                String[] whereValue = { userId};
                if (mDatabase.delete(FEATURE_TABLE_NAME, where, whereValue) < 0) {
                    return false;
                }
                setTransactionSuccessful(mDatabase);
                success = true;
            }
        } finally {
            endTransaction(mDatabase);
        }
        return success;
    }
//以下3个函数随后再研究
    private void beginTransaction(SQLiteDatabase mDatabase) {
        if (allowTransaction) {
            mDatabase.beginTransaction();
        } else {
            writeLock.lock();
            writeLocked = true;
        }
    }

    private void setTransactionSuccessful(SQLiteDatabase mDatabase) {
        if (allowTransaction) {
            mDatabase.setTransactionSuccessful();
        }
    }

    private void endTransaction(SQLiteDatabase mDatabase) {
        if (allowTransaction) {
            mDatabase.endTransaction();
        }
        if (writeLocked) {
            writeLock.unlock();
            writeLocked = false;
        }
    }

    public synchronized byte[] queryFeature(String faceToken) {
        byte[] feature = null;
        Cursor cursor = null;

        try {
            if (mDatabase == null) {
                return null;
            }
            SQLiteDatabase db = mDBOpenHelper.getWritableDatabase();
            String where = "face_token = ? ";
            String[] whereValue = { faceToken };
            cursor = db.query(mDBOpenHelper.FEATURE_TABLE_NAME, null, where, whereValue, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                feature = cursor.getBlob(cursor.getColumnIndex("feature"));

            }
            db.close();
        } catch (Exception pE){

        }finally {
            if (cursor!=null)
                cursor.close();
        }
        return feature;
    }

    public boolean deleteFeatureByFaceToken(String faceToken) {
        boolean success = false;
        try {
            mDatabase = mDBOpenHelper.getWritableDatabase();
            beginTransaction(mDatabase);
            if (!TextUtils.isEmpty(faceToken)) {
                String where = " face_token=?";
                String[] whereValue = { faceToken};

                if (mDatabase.delete(mDBOpenHelper.FEATURE_TABLE_NAME, where, whereValue) < 0) {
                    return false;
                }
                setTransactionSuccessful(mDatabase);
                success = true;
            }

        } finally {
            endTransaction(mDatabase);
        }
        return success;
    }

}
