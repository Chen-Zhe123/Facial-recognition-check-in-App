package com.firefly.face1.bean;

import android.graphics.Bitmap;

public  class AllRecord {
    private boolean isRegistered = true;
    private Bitmap imageScr;
    private String userName;
    private String studentId;
    private long captureTime;

    public AllRecord() {
    }

    public void setImageRes(Bitmap image){
        imageScr = image;
    }
    public Bitmap getImageRes() {
        return imageScr;
    }
    public void setUserName(String name){
        userName = name;
    }
    public String getUserName(){
        return userName;
    }
    public void setStudentId(String student_id){
        studentId = student_id;
    }
    public String getStudentId() {
        return studentId;
    }
    public String getNameId() {
        return "姓名: " + userName + "    学号: " + studentId;
    }
    public void setCaptureTime(long capture_time){
        captureTime = capture_time;
    }
    public long getCaptureTime(){
        return captureTime;
    }
}
