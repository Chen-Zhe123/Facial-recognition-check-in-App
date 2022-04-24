package com.firefly.face1.bean;

public class Group {
    private String field;
    private int isPriority = -1;
    private String groupName;
    private String groupDescription;
    private Boolean isSelected;
    private int isShow;
    //由于数据库不提供Boolean值，用1代true,0代false

    public void setField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setPriority(int priority) {
        isPriority = priority;
    }

    public int getPriority() {
        return isPriority;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setShow(int aShow) {
        isShow = aShow;
    }

    public int getShow() {
        return isShow;
    }
}
