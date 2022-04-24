/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1.bean;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Feature implements Serializable{

    private String faceToken = "";

    private byte[] feature;

    private String userId = "";

    private String studentId;

    private long regTime;

    private long updateTime;

    private String imageName = "";

    public Feature() {
    }

    public String getFaceToken() {
        if (feature != null) {
            byte[] base = Base64.encode(feature, Base64.NO_WRAP);
            faceToken = new String(base);
        }
        return faceToken;
    }

    public void setFaceToken() {
        if (feature != null) {
            byte[] base = Base64.encode(feature, Base64.NO_WRAP);
            faceToken = new String(base);
        }
    }

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String student_id) {
        this.studentId = student_id;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUserName() {
        return imageName;
    }

    public void setUserName(String imageName) {
        this.imageName = imageName;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object pO) {
        if (this == pO) return true;
        if (pO == null || getClass() != pO.getClass()) return false;
        Feature feature1 = (Feature) pO;
        return regTime == feature1.regTime &&
                Objects.equals(faceToken, feature1.faceToken) &&
                Arrays.equals(feature, feature1.feature) &&
                Objects.equals(userId, feature1.userId) &&
                Objects.equals(imageName, feature1.imageName);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        int result = Objects.hash(faceToken, userId, regTime, imageName);
        result = 31 * result + Arrays.hashCode(feature);
        return result;
    }
}
