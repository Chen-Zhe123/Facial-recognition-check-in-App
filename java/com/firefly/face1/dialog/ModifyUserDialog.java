package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firefly.face1.R;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.UserInfo;

public class ModifyUserDialog {

    private Context context;
    private Dialog modifyDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private UpdateCallback updateCallback;
    private UserInfo userInfo;
    private int pos;

    private EditText name;
    private EditText id;
    private Button modify;
    private Button cancel;

    public ModifyUserDialog(Context pContext) {
        context = pContext;
    }

    public void show(UserInfo user, int pPosition) {
        userInfo = user;
        pos = pPosition;
        modifyDialog = new Dialog(context);
        modifyDialog.setContentView(R.layout.modify_user_dialog);
        modifyDialog.setCancelable(false);
        modifyDialog.show();
        name = modifyDialog.findViewById(R.id.new_name);
        id = modifyDialog.findViewById(R.id.new_id);
        modify = modifyDialog.findViewById(R.id.modify);
        cancel = modifyDialog.findViewById(R.id.cancel);
        addListener();
    }

    private void addListener() {
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = name.getText().toString();
                String key1 = id.getText().toString();
                if (TextUtils.isEmpty(key) || TextUtils.isEmpty(key1)){
                    toast("姓名或学号不能为空!");
                }else if(key.length() >= 12){
                    toast("姓名过长!请重新输入");
                } else{
                    userInfo.setUserName(key);
                    userInfo.setStudentId(key1);
                    if (updateDB(userInfo)) {//更新人脸库
                        if (modifyDialog != null) {
                            if (updateCallback != null)
                                updateCallback.updateCallback(pos, key, key1);
                            modifyDialog.dismiss();
                        }
                    } else {
                        toast(context.getString(R.string.modify_dialog_failture));
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modifyDialog != null) {
                    modifyDialog.dismiss();
                }
            }
        });

    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setCallback(UpdateCallback callback) {
        this.updateCallback = callback;
    }

    public interface UpdateCallback {
        void updateCallback(int pos, String updateName,String updateStudentId);
    }

    private boolean updateDB(UserInfo userInfo){//判断是否人脸库是否更新成功
       boolean ret = FaceApi.getInstance().updateUserInfo(userInfo);
       if (ret){
           FaceApi.getInstance().loadFaceFromLibrary();
       }
       return ret;
    }

}
