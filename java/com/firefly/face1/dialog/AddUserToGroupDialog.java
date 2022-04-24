package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;
import com.firefly.face1.adapter.AddUserToGroupAdapter;
import com.firefly.face1.bean.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class AddUserToGroupDialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private AddUserToGroupAdapter mAdapter;
    private List<UserInfo> allUserList;
    private List<UserInfo> inGroupList;
    private AddUserToGroupCallback mCallback;
    private Context mContext;
    private Dialog mDialog;
    private String mField;
    private LinearLayout addFaceBottomBar;
    private RecyclerView mRecyclerView;
    private EditText editText;
    private ImageView cleanEdit;
    private ImageView searchButton;
    private TextView cancelSearch;
    private TextView beginSelect;
    private TextView dismiss;
    private TextView ensureAdd;
    private TextView cancelAdd;
    private CheckBox selectAllUser;

    public AddUserToGroupDialog(Context pContext) {
        mContext = pContext;
    }

    public void show(String field,List<UserInfo> inGroupUserList) {
        mField = field;
        initDialog();
        findView();
        allUserList = DBMaster.getInstance().mFeatureTable.queryAllUserInfo();
        inGroupList = inGroupUserList;
        mAdapter = new AddUserToGroupAdapter(mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setUserList(inGroupList,allUserList);
    }

    public void initDialog(){
        mDialog = new Dialog(mContext);
        mDialog.setContentView(R.layout.add_user_to_group_dialog);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    public void findView(){
        editText = mDialog.findViewById(R.id.group_search_bar);
        editText.clearFocus();
        cleanEdit = mDialog.findViewById(R.id.group_clean_EditText);
        cleanEdit.setOnClickListener(this);
        searchButton = mDialog.findViewById(R.id.group_search);
        searchButton.setOnClickListener(this);
        cancelSearch = mDialog.findViewById(R.id.cancel_select_user);
        cancelSearch.setOnClickListener(this);
        beginSelect = mDialog.findViewById(R.id.select_user);
        beginSelect.setOnClickListener(this);
        dismiss = mDialog.findViewById(R.id.dismiss_add_face_dialog);
        dismiss.setOnClickListener(this);
        ensureAdd = mDialog.findViewById(R.id.sure_add_face);
        ensureAdd.setOnClickListener(this);
        cancelAdd = mDialog.findViewById(R.id.cancel_add_face);
        cancelAdd.setOnClickListener(this);
        selectAllUser = mDialog.findViewById(R.id.select_all_face);
        selectAllUser.setOnCheckedChangeListener(this);
        addFaceBottomBar = mDialog.findViewById(R.id.add_face_bottom_option);
        mRecyclerView = mDialog.findViewById(R.id.group_face_list);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.group_search:
                String key = editText.getText().toString();
                mAdapter.setUserList(inGroupList,DBMaster.getInstance().mFeatureTable.searchUser(key));
                mAdapter.notifyDataSetChanged();
                beginSelect.setVisibility(View.GONE);
                cancelSearch.setVisibility(View.VISIBLE);
                break;
            case R.id.group_clean_EditText:
                editText.setText("");
                break;
            case R.id.cancel_select_user:
                mAdapter.setUserList(inGroupList,allUserList);
                mAdapter.notifyDataSetChanged();
                cancelSearch.setVisibility(View.GONE);
                beginSelect.setVisibility(View.VISIBLE);
                break;
            case R.id.select_user:
                showAllCheckBox();
                addFaceBottomBar.setVisibility(View.VISIBLE);
                dismiss.setVisibility(View.GONE);
                break;//要记得break,不然会继续执行下面的dismiss语句
            case R.id.dismiss_add_face_dialog:
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                break;
            case R.id.sure_add_face:
                List<String> deleteIdList = new ArrayList<>();
                for(UserInfo user : allUserList){
                    if(user.isSelected()){
                        deleteIdList.add(user.getUserId());
                    }
                }
                mCallback.addUserCallback(mField,deleteIdList);
                Toast.makeText(mContext,"添加成功",Toast.LENGTH_SHORT).show();
                addFaceBottomBar.setVisibility(View.GONE);
                dismiss.setVisibility(View.VISIBLE);
                mDialog.dismiss();;//偷个懒，向组中增加人脸后直接退出会话，不用考虑数据刷新的问题
                break;
            case R.id.cancel_add_face:
                hideAllCheckBox();
                addFaceBottomBar.setVisibility(View.GONE);
                dismiss.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
    private void hideAllCheckBox(){
        for(UserInfo user : allUserList){
            user.setShow(false);
            mAdapter.notifyDataSetChanged();
        }
    }
    private void showAllCheckBox(){
        for(UserInfo user : allUserList){
            user.setShow(true);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            for (UserInfo user : allUserList){
                user.setSelected(true);
                mAdapter.notifyDataSetChanged();
            }
        }
        else{
            for (UserInfo user : allUserList){
                user.setSelected(false);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setCallback(AddUserToGroupCallback callback){
        this.mCallback = callback;
    }

    public interface AddUserToGroupCallback{
        void addUserCallback(String field,List<String> id);
    }
}
