package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;
import com.firefly.face1.adapter.AddUserToGroupAdapter;
import com.firefly.face1.adapter.GroupListAdapter;
import com.firefly.face1.adapter.UserInGroupAdapter;
import com.firefly.face1.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class ScanGroupDialog implements View.OnClickListener, AddUserToGroupDialog.AddUserToGroupCallback {
    UserInGroupAdapter mAdapter;
    AddUserToGroupAdapter addUserToGroupAdapter;
    AddUserToGroupDialog addUserDialog;
    private List<UserInfo> mUserInfoList = new ArrayList<>();
    private Context mContext;
    private Dialog mDialog;
    private String mField;
    private RecyclerView mRecyclerView;
    private TextView name;
    private ImageView dismiss;
    private TextView manage;
    private ImageView add;
    private TextView emptyText;
    public ScanGroupDialog(Context pContext) {
        mContext = pContext;
    }

    public void show(String field,List<UserInfo> userInfoList,String groupName) {
        mField = field;
        mDialog = new Dialog(mContext);
        mDialog.setContentView(R.layout.scan_group_dialog);
        mDialog.setCancelable(false);
        mDialog.show();

        name = mDialog.findViewById(R.id.dialog_group_name);
        name.setText(groupName);
        dismiss = mDialog.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(this);

        manage = mDialog.findViewById(R.id.dialog_group_manager);
        add = mDialog.findViewById(R.id.add_face);
        add.setOnClickListener(this);

        emptyText = mDialog.findViewById(R.id.group_empty_text);
        if(userInfoList != null) {
            mUserInfoList = userInfoList;
            mRecyclerView = mDialog.findViewById(R.id.group_face_list);
            mAdapter = new UserInGroupAdapter(mContext);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setUserInfoList(userInfoList);
        }else{
            emptyText.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dismiss:
                if (mDialog != null) {
                    mDialog.dismiss();
                    break;
                }
            case R.id.add_face:
                addUserDialog = new AddUserToGroupDialog(mContext);
                addUserDialog.show(mField,mUserInfoList);
                addUserDialog.setCallback(this);
                break;
        }
    }
    @Override
    public void addUserCallback(String field, List<String> idList) {
        if(DBMaster.getInstance().mGroupTable.addUserToGroup(field,idList)){
            Toast.makeText(mContext,"人脸添加成功",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mContext,"人脸添加失败！！！",Toast.LENGTH_SHORT).show();
        }
        mDialog.dismiss();//偷个懒，向组中增加人脸后直接退出会话，不用考虑数据刷新的问题
    }
}
