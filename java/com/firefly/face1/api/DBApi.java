package com.firefly.face1.api;


import android.content.Context;

import com.example.checkvendor.CheckVendor;
import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.bean.Feature;

public class DBApi {

    private static DBApi instance;
    private static DBMaster mDBMaster;
    private static Context mContext;
    private int check;

    public DBApi(){
        CheckVendor checkVendor = new CheckVendor();
        check = checkVendor.check();
        mDBMaster = DBMaster.getInstance();
        mDBMaster.openDataBase();
    }

    public static synchronized DBApi getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new DBApi();
        }
        return instance;
    }

    public boolean addFeature(Feature feature){
        if (feature==null||check==1){
            return false;
        }
        boolean ret = mDBMaster.mFeatureTable.addFeature(feature);
        return ret;
    }
}
