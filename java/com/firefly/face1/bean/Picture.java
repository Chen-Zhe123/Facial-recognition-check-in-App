package com.firefly.face1.bean;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

public class Picture implements Serializable{
    private boolean isSelected = false;
    private String fileName = "";

    private String picturePath = "";

    public Picture() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object pO) {
        if (this == pO) return true;
        if (pO == null || getClass() != pO.getClass()) return false;
        Picture picture = (Picture) pO;
        return isSelected == picture.isSelected &&
                Objects.equals(fileName, picture.fileName) &&
                Objects.equals(picturePath, picture.picturePath);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(isSelected, fileName, picturePath);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String pFileName) {
        fileName = pFileName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean pSelected) {
        isSelected = pSelected;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String pPicturePath) {
        picturePath = pPicturePath;
    }
}
