/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.bean.Feature;
import com.firefly.face1.bean.UserInfo;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.example.checkvendor.CheckVendor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.firefly.face1.api.Constants.DEFAULT_GROUP_FIELD;

public class FaceApi {

    public static final int FACE_FILE = 1;
    public static final int FACE_TOKEN = 2;
    private HashMap<String, byte[]> group2Facesets = new HashMap<>();
    private static FaceApi instance;
    private int check;

    private int faceFeaturelen = 512;

    private static DBMaster mDBMaster;//***
    private static Context mContext;//***

    private FaceApi() {
        CheckVendor checkVendor = new CheckVendor();
        check = checkVendor.check();
        mIdentifyRet = new IdentifyRet();

        mDBMaster = DBMaster.getInstance();//***
    }

    public static synchronized FaceApi getInstance() {
        if (instance == null) {
            instance = new FaceApi();
        }
        return instance;
    }

    public boolean addFeature(Feature feature){
        if (feature==null||check==1){
            return false;
        }
//        boolean ret = DBManager.getInstance().addFeature(feature);
        boolean ret = mDBMaster.mFeatureTable.addFeature(feature);
        return ret;
    }

    public boolean deleteFeatureByfaceToken(String faceToken) {
        if (TextUtils.isEmpty(faceToken)) {
            return false;
        }
//        boolean ret = DBManager.getInstance().deleteFeatureByFaceToken(faceToken);
        boolean ret = DBMaster.getInstance().mFeatureTable.deleteFeatureByFaceToken(faceToken);
        return ret;
    }

    public boolean deleteFeatureByUserId(String user_id) {
        if (TextUtils.isEmpty(user_id)) {
            return false;
        }
//        boolean ret = DBManager.getInstance().deleteFeatureByUserId(user_id);
        boolean ret = mDBMaster.mFeatureTable.deleteFeatureByUserId(user_id);
        return ret;
    }

    public boolean updateUserInfo(UserInfo userInfo){
        if (userInfo==null){
            return false;
        }
//        return DBManager.getInstance().updateUserInfo(userInfo);
        return mDBMaster.mFeatureTable.updateUserInfo(userInfo);
    }

    private final List<Feature> mFeatureList = new ArrayList<>();

    public List<Feature> getFeatures(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return mFeatureList;
        }
//        List<Feature> ret = DBManager.getInstance().queryFeatureById(userId);
        List<Feature> ret = mDBMaster.mFeatureTable.queryFeatureById(userId);
        return ret;
    }

    public byte[] getFeature(String faceToken) {
        if (TextUtils.isEmpty(faceToken)) {
            return null;
        }
//        byte[] feature = DBManager.getInstance().queryFeature(faceToken);
        byte[] feature = mDBMaster.mFeatureTable.queryFeature(faceToken);
        return feature;
    }

    public int getFeature(Bitmap bitmap, byte[] feature ) {
        if (bitmap == null) {
            return -1;
        }
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, feature);

        return ret;
    }

    public int getFeature(Bitmap bitmap, byte[] feature, int minFaceSize) {
        if (bitmap == null) {
            return -1;
        }
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, feature, minFaceSize);

        return ret;
    }

    public int getFeatureForIDPhoto(Bitmap bitmap, byte[] feature, int minFaceSize) {
        if (bitmap == null) {
            return -1;
        }
        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeatureForIDPhoto(argbImg, feature, minFaceSize);

        return ret;
    }

    public float match(String image1, String image2, int type, Context context) {
        if (TextUtils.isEmpty(image1) || TextUtils.isEmpty(image2)||check==1) {
            return -1;
        }
        float ret = -1;
        if (type == FACE_FILE) {
            Uri uri1  = Uri.parse(image1);
            Uri uri2  = Uri.parse(image1);
            ret = match(uri1, uri2, context);
        } else if (type == FACE_TOKEN) {

//            byte[] firstFeature = DBManager.getInstance().queryFeature(image1);
//            byte[] secondFeature = DBManager.getInstance().queryFeature(image2);
            byte[] firstFeature = DBMaster.getInstance().mFeatureTable.queryFeature(image1);
            byte[] secondFeature = DBMaster.getInstance().mFeatureTable.queryFeature(image2);
                    ret = FaceSDKManager.getInstance().getFaceFeature()
                    .getFaceFeatureDistance(firstFeature, secondFeature);
        }
        return ret;
    }

    public float match(Uri image1, Uri image2, Context context) {
        if (image1 == null) {
            return -100;
        }

        if (image2 == null) {
            return -101;
        }
        float ret = -1;

        try {
            byte[] firstFeature = new byte[2048];
            byte[] secondFeature = new byte[2048];
            final Bitmap bitmap1 = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(image1));
            ARGBImg argbImg1 = FeatureUtils.getImageInfo(bitmap1);
            int ret1 = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg1, firstFeature);

            final Bitmap bitmap2 = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(image2));
            ARGBImg argbImg2 = FeatureUtils.getImageInfo(bitmap2);
            int ret2 = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg2, secondFeature);
            if (ret1 != 512) {
                return -102;
            }
            if (ret2 != 512) {
                return -103;
            }

            ret = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(firstFeature, secondFeature);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public float match(final byte[] photoFeature, int[] argbData, int rows, int cols, int[] landmarks) {
        if (photoFeature == null || argbData == null || landmarks == null) {
            return -1;
        }
        byte[] imageFrameFeature = new byte[2048];

        FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbData, rows, cols,
                imageFrameFeature, landmarks);
        final float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(photoFeature,
                imageFrameFeature);
        return score;
    }

    public float match(final byte[] feature1, final byte[] feature2) {
        if (feature1 == null || feature2 == null) {
            return -1;
        }

        final float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(feature1, feature2);
        return score;
    }

    public float matchIDPhoto(final byte[] feature1, final byte[] feature2) {
        if (feature1 == null || feature2 == null) {
            return -1;
        }

        final float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistanceForIDPhoto(feature1, feature2);
        return score;
    }

    public IdentifyRet identity(String image, int type, Context context) {
        if (TextUtils.isEmpty(image)) {
            return null;
        }
        byte[] imageFrameFeature = new byte[2048];
        if (type == FACE_FILE) {
            try {
                Uri uri  = Uri.parse(image);
                final Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, imageFrameFeature);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (type == FACE_TOKEN) {
//            imageFrameFeature = DBManager.getInstance().queryFeature(image);
            imageFrameFeature = DBMaster.getInstance().mFeatureTable.queryFeature(image);
        }

        if (imageFrameFeature == null) {
            return null;
        }

        HashMap<String, byte[]> userId2Feature = group2Facesets;

        String userIdOfMaxScore = "";
        float identifyScore = 0;
        Iterator iterator = userId2Feature.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, byte[]>  entry = (Map.Entry<String, byte[]>) iterator.next();
            byte[] feature = entry.getValue();
            final float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(
                    feature, imageFrameFeature);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = entry.getKey();
            }
        }
        mIdentifyRet.setScore(identifyScore);
        mIdentifyRet.setUserId(userIdOfMaxScore);
        return mIdentifyRet;
    }

    public IdentifyRet identity(String image, int type, String userId, Context context) {
        if (TextUtils.isEmpty(image) || TextUtils.isEmpty(userId)) {
            return null;
        }
        byte[] imageFrameFeature = new byte[2048];
        if (type == FACE_FILE) {
            try {
                Uri uri  = Uri.parse(image);
                final Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, imageFrameFeature);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (type == FACE_TOKEN) {
//            imageFrameFeature = DBManager.getInstance().queryFeature(image);
            imageFrameFeature = DBMaster.getInstance().mFeatureTable.queryFeature(image);
        }

        if (imageFrameFeature == null|| userId == null) {
            return null;
        }

        HashMap<String, byte[]> userId2Feature = group2Facesets;

        byte[] feature = userId2Feature.get(userId);
        float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(
                feature, imageFrameFeature);
        mIdentifyRet.setScore(score);
        mIdentifyRet.setUserId(userId);
        return mIdentifyRet;
    }


    private IdentifyRet mIdentifyRet;
    private byte[] imageFrameFeature = new byte[2048];

    public IdentifyRet identity(int[] argbData, int rows, int cols, int[] landmarks) {
        if (argbData == null || landmarks == null||check==1) {
            return null;
        }
        HashMap<String, byte[]> userId2Feature = group2Facesets;

        FaceSDKManager.getInstance().getFaceFeature().extractFeature(argbData, rows, cols,
                imageFrameFeature, landmarks);

        byte[] _firstFeatureDataResult = java.util.Arrays.copyOf(imageFrameFeature, faceFeaturelen);

        String userIdOfMaxScore = "";
        float identifyScore = 0;
        Iterator iterator = userId2Feature.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, byte[]>  entry = (Map.Entry<String, byte[]>) iterator.next();
            byte[] feature = entry.getValue();
            byte[] _secondFeatureDataResult = java.util.Arrays.copyOf(feature, faceFeaturelen);

            float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistance(
                    _secondFeatureDataResult, _firstFeatureDataResult);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = entry.getKey();
            }
        }

        mIdentifyRet.setScore(identifyScore);
        mIdentifyRet.setUserId(userIdOfMaxScore);
        return mIdentifyRet;
    }

    public IdentifyRet identityForIDPhoto(int[] argbData, int rows, int cols, int[] landmarks) {
        if (argbData == null || landmarks == null||check==1) {
            return null;
        }
        HashMap<String, byte[]> userId2Feature = group2Facesets;


        byte[] firstFeatureDataResult = java.util.Arrays.copyOf(imageFrameFeature, faceFeaturelen);
        FaceSDKManager.getInstance().getFaceFeature().extractFeatureForIDPhoto(argbData, rows, cols,
                firstFeatureDataResult, landmarks);

        String userIdOfMaxScore = "";
        float identifyScore = 0;
        Iterator iterator = userId2Feature.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry<String, byte[]>) iterator.next();
            byte[] feature = entry.getValue();
            byte[] secondFeatureDataResult = java.util.Arrays.copyOf(feature, faceFeaturelen);

            final float score = FaceSDKManager.getInstance().getFaceFeature().getFaceFeatureDistanceForIDPhoto(
                    secondFeatureDataResult, firstFeatureDataResult);
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = entry.getKey();
            }
        }
        mIdentifyRet.setScore(identifyScore);
        mIdentifyRet.setUserId(userIdOfMaxScore);
        return mIdentifyRet;
    }

    public void loadFaceFromLibrary() {
        List<Feature> featureList = mDBMaster.mFeatureTable.queryAllFeature();
        HashMap<String, byte[]> userId2Feature = new HashMap<String, byte[]>();
        for (Feature feature : featureList) {
            userId2Feature.put(feature.getUserId(), feature.getFeature());
        }
        group2Facesets = userId2Feature;
    }

    public List<String> loadFaceIdFromGroup(){//加载默认组人脸id列表//点击开始签到后（或更改默认组后），要调用一次该方法，重新加载待签到组
        List<String> idList = mDBMaster.mGroupTable.queryIdListByField(DEFAULT_GROUP_FIELD);
        return idList;
    }

    public HashMap<String, byte[]> getGroup2Facesets() {
        return group2Facesets;
    }

}
