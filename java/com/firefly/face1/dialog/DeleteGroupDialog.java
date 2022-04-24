package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firefly.face1.R;

public class DeleteGroupDialog {

    private Button ensureDelete;
    private Button cancelDelete;
    private Context mContext;
    private Dialog mDialog;
    private DeleteGroupCallback mCallback;
    private String mField;

    public DeleteGroupDialog(Context pContext) {
        mContext = pContext;
    }

    public void show(String field) {
        mField = field;
        mDialog = new Dialog(mContext);
        mDialog.setContentView(R.layout.delete_group_dialog);
        mDialog.setCancelable(false);
        mDialog.show();
        ensureDelete = mDialog.findViewById(R.id.ensure_delete_group);
        cancelDelete = mDialog.findViewById(R.id.cancel_delete_group);
        addListener();
    }


    private void addListener() {
        ensureDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.deleteCallback(mField);
                mDialog.dismiss();
                Toast.makeText(mContext,"删除成功",Toast.LENGTH_SHORT).show();
            }
        });


        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });

    }

    public void setCallback(DeleteGroupCallback callback) {
        this.mCallback = callback;
    }

    public interface DeleteGroupCallback {
        void deleteCallback(String field);
    }

}
