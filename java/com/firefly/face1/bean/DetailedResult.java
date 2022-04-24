package com.firefly.face1.bean;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailedResult {

    private String userId;
    private Bitmap faceImage;
    private String userName;
    private String studentId;
    private long signTime;
    private String signState;

    public DetailedResult() {
    }

    public void setUserId(String user_id) {
        userId = user_id;
    }

    public String getUserId() {
        return userId;
    }

    public void setFaceImage(Bitmap image){
        faceImage = image;
    }
    public Bitmap getFaceImage() {
        return faceImage;
    }

    public void setUserName(String user_name){
        userName = user_name;
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

    public void setSignTime(long sign_time){
        signTime = sign_time;
    }
    public String getSignTime(){
        if(signTime > 0) {//用于判断是否要在详细签到结果中显示签到时间
            Date date = new Date(Long.valueOf(signTime));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            String time = dateFormat.format(date);
            return "打卡时间: " + time;
        }else{
            return "";
        }
    }

    public void setSignState(String sign_state) {
        signState = sign_state;
    }
    public String getSignState() {
        return signState;
    }
}
