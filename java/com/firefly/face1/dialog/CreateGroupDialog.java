package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firefly.face1.R;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.UserInfo;

public class CreateGroupDialog {

    private Context context;
    private Dialog mDialog;
    private CreateGroupCallback mCallback;
    private EditText name;
    private EditText description;
    private Button ensure;
    private Button cancel;

    public CreateGroupDialog(Context pContext) {
        context = pContext;
    }

    public void show() {
        mDialog = new Dialog(context);
        mDialog.setContentView(R.layout.create_group_dialog);
        mDialog.setCancelable(false);
        mDialog.show();
        name = mDialog.findViewById(R.id.init_group_name_edit);
        description = mDialog.findViewById(R.id.init_group_description_edit);
        ensure = mDialog.findViewById(R.id.ensure_create_group);
        cancel = mDialog.findViewById(R.id.cancel_create_group);
        addListener();
    }

    private void addListener() {
        ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = name.getText().toString();
                String key1 = description.getText().toString();
                if (TextUtils.isEmpty(key)) {
                    Toast.makeText(context, "组名不可为空!", Toast.LENGTH_LONG).show();
                } else {
                    if (TextUtils.isEmpty(key1)) {
                        key1 = "";
                    }
                    mCallback.createCallback(key,key1);
                    mDialog.dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });

    }

    public void setCallback(CreateGroupCallback callback) {
        this.mCallback = callback;
    }

    public interface CreateGroupCallback {
        void createCallback(String name,String description);
    }
}
