package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.aip.utils.ConfigUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;

public class BeginWorkingDialog implements View.OnClickListener{

    private Context mContext;
    private Dialog mDialog;
    private BeginWorkingCallback mCallback;
    private SharedPreferences pref;
    private static String DEFAULT_GROUP_FIELD;
    private static String DEFAULT_GROUP_NAME;

    private int liveType = 0;

    private TextView defaultGroup;
    private TextView defaultLivingType;
    private Button ensureBeginWorking;
    private Button cancelBeginWorking;

    public BeginWorkingDialog(Context context){
        mContext = context;
    }
    public void show(){
        getParameter();
        initDialog();
        findView();
    }
    public void initDialog(){
        mDialog = new Dialog(mContext);
        mDialog.setContentView(R.layout.begin_working_dialog);
        mDialog.setCancelable(true);
        mDialog.show();
    }
    public void getParameter(){
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);//待整合
        DEFAULT_GROUP_FIELD = pref.getString("field","group_0");
        if(DEFAULT_GROUP_FIELD.equals("group_0")){
            DEFAULT_GROUP_NAME = "系统组";
        }else {
            DEFAULT_GROUP_NAME = DBMaster.getInstance().mGroupInfoTable.queryGroupName(DEFAULT_GROUP_FIELD);
        }
        liveType = PreferencesUtil.getInt(ConfigUtils.TYPE_LIVENSS, ConfigUtils.TYPE_NO_LIVENSS);
    }
    public void findView(){
        defaultGroup = mDialog.findViewById(R.id.default_group);
        defaultGroup.setText(DEFAULT_GROUP_NAME);
        defaultLivingType = mDialog.findViewById(R.id.default_living_type);
        defaultLivingType.setText(String.valueOf(liveType));
        ensureBeginWorking = mDialog.findViewById(R.id.ensure_begin_working);
        ensureBeginWorking.setOnClickListener(this);
        cancelBeginWorking = mDialog.findViewById(R.id.cancel_begin_working);
        cancelBeginWorking.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ensure_begin_working:
                mCallback.workingCallback();
                mDialog.dismiss();
            case R.id.cancel_begin_working:
                mDialog.dismiss();
                break;
        }
    }
    public void setCallback(BeginWorkingCallback callback){
        mCallback = callback;
    }
    public interface BeginWorkingCallback{
        void workingCallback();
    }
}
