package com.firefly.face1.bean;

import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreviewResult {

    private long workTime;
    private String groupName;
    private Boolean isShow = false;
    private Boolean isSelected = false;

//    private int needSignCount;
//    private int alreadySignCount;
//    private int lateSignCount;
//    private int neverSignCount;

    private List<String> needSignList = new ArrayList<>();
    private Map<String,Long> alreadySignMap = new HashMap<>();
    private Map<String,Long> lateSignMap = new HashMap<>();
    private List<String> neverSignList = new ArrayList<>();

//    private String needSignText;
//    private String alreadySignText;
//    private String lateSignText;
//    private String neverSignText;


    public void setShow(Boolean show) {
        isShow = show;
    }

    public Boolean getShow() {
        return isShow;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setNeedSignList(List<String> needSignList) {
        this.needSignList = needSignList;
    }

    public List<String> getNeedSignList() {
        return needSignList;
    }

    public void setAlreadySignMap(Map<String, Long> alreadySignMap) {
        this.alreadySignMap = alreadySignMap;
    }

    public Map<String, Long> getAlreadySignMap() {
        return alreadySignMap;
    }

    public void setLateSignMap(Map<String, Long> lateSignMap) {
        this.lateSignMap = lateSignMap;
    }

    public Map<String, Long> getLateSignMap() {
        return lateSignMap;
    }

    public void setNeverSignList(List<String> neverSignList) {
        this.neverSignList = neverSignList;
    }

    public List<String> getNeverSignList() {
        return neverSignList;
    }

    public long getWorkTime() {
        return workTime;
    }

    public String getGroupName() {
        return groupName;
    }
//
//    public int getNeedSignCount() {
//        return needSignCount;
//    }
//
//    public int getAlreadySignCount() {
//        return alreadySignCount;
//    }
//
//    public int getLateSignCount() {
//        return lateSignCount;
//    }
//
//    public int getNeverSignCount() {
//        return neverSignCount;
//    }
//
    public void setWorkTime(long workTime) {
        this.workTime = workTime;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
//
//    public void setNeedSignCount(int needSignCount) {
//        this.needSignCount = needSignCount;
//    }
//
//    public void setAlreadySignCount(int alreadySignCount) {
//        this.alreadySignCount = alreadySignCount;
//    }
//
//    public void setLateSignCount(int lateSignCount) {
//        this.lateSignCount = lateSignCount;
//    }
//
//    public void setNeverSignCount(int neverSignCount) {
//        this.neverSignCount = neverSignCount;
//    }
//
//    public String getNeedSignText() {
//        return needSignText;
//    }
//
//    public void setNeedSignText(String needSignText) {
//        this.needSignText = needSignText;
//    }
//
//    public String getAlreadySignText() {
//        return alreadySignText;
//    }
//
//    public void setAlreadySignText(String alreadySignText) {
//        this.alreadySignText = alreadySignText;
//    }
//
//    public String getLateSignText() {
//        return lateSignText;
//    }
//
//    public void setLateSignText(String lateSignText) {
//        this.lateSignText = lateSignText;
//    }
//
//    public String getNeverSignText() {
//        return neverSignText;
//    }
//
//    public void setNeverSignText(String neverSignText) {
//        this.neverSignText = neverSignText;
//    }
}
