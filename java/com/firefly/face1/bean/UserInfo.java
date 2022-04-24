package com.firefly.face1.bean;

public class UserInfo {

    private byte[] feature;

    private String userId = "";

    private String studentId = "";

    private long regTime;

    private String userName = "";

    private Boolean isSelected = false;

    private Boolean isShow = false;

    private Boolean isInGroup = false;

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof UserInfo) {
            UserInfo user = (UserInfo) obj;
            return this.getUserId().equals(user.getUserId());
        } else{
            return false;
        }
    }

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public UserInfo(){}

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

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
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

    public void setInGroup(Boolean inGroup) {
        isInGroup = inGroup;
    }

    public Boolean getInGroup() {
        return isInGroup;
    }
}
