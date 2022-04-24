package com.firefly.face1.bean;

public class Record {
    private byte[] feature;

    private String userId = "";

    private String studentId = "";

    private long captureTime;

    private String userName = "";

    private Boolean isSelected = false;

    private Boolean isShow = false;

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public Record(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public long getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(long regTime) {
        this.captureTime = captureTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public Boolean isSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public boolean isShow() {
        return isShow;
    }
    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }
}
