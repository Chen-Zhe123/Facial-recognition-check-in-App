package com.firefly.face1.FragmentView;

import android.content.Context;

import com.firefly.face1.adapter.GroupListAdapter;
import com.firefly.face1.bean.Group;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;
import com.firefly.face1.dialog.AddUserToGroupDialog;
import com.firefly.face1.dialog.CreateGroupDialog;
import com.firefly.face1.dialog.DeleteGroupDialog;
import com.firefly.face1.dialog.ModifyGroupDialog;
import com.firefly.face1.dialog.ScanGroupDialog;

import java.util.ArrayList;
import java.util.List;

import static com.firefly.face1.api.Constants.DEFAULT_GROUP_FIELD;

public class GroupManager implements View.OnClickListener,CreateGroupDialog.CreateGroupCallback, GroupListAdapter.SetPriorityCallback{
    private Context mContext;
    private GroupListAdapter groupListAdapter;
    private List<Group> groupList = new ArrayList<>();
    private LinearLayout root;
    private RecyclerView groupManagerList;
    private CreateGroupDialog mCreateGroupDialog;
    private ScanGroupDialog mScanGroupDialog;
    private ModifyGroupDialog mModifyGroupDialog;
    private DeleteGroupDialog mDeleteGroupDialog;
    private AddUserToGroupDialog mAddUserDialog;
    private ImageView addGroupButton;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public GroupManager(LinearLayout layout, Context context){
        root = layout;
        mContext = context;
        init();
    }
    private void init(){
        findView();
        DBMaster.getInstance().init(mContext.getApplicationContext());
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        initGroup();
        setCallback();
        groupListAdapter.setGroupList(groupList);
        groupListAdapter.setCallback(this);

    }
    private void initGroup(){
        Group group = new Group();
        group.setField("group_0");
        group.setGroupName("系统组");
        group.setGroupDescription("该组包含人脸库中所有成员，由系统自动生成，不可修改或删除");
        group.setPriority(0);
        group.setShow(1);
        groupList = DBMaster.getInstance().mGroupInfoTable.queryAllGroups();
        groupList.add(group);
        String field = pref.getString("field","group_0");
        DEFAULT_GROUP_FIELD = field;
        for(Group group1:groupList){
            if(group1.getField().equals(field)){
                group1.setPriority(1);
            }else{
                group1.setPriority(0);
            }
        }
    }
    private void setCallback(){
        mCreateGroupDialog = new CreateGroupDialog(mContext);
        mCreateGroupDialog.setCallback(this);
        mScanGroupDialog = new ScanGroupDialog(mContext);
        mModifyGroupDialog = new ModifyGroupDialog(mContext);
        mDeleteGroupDialog = new DeleteGroupDialog(mContext);
        mAddUserDialog = new AddUserToGroupDialog(mContext);
    }
    private void findView(){
        groupManagerList  =root.findViewById(R.id.group_list);
        addGroupButton = root.findViewById(R.id.add_group);
        addGroupButton.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        groupManagerList.setLayoutManager(layoutManager);
        groupListAdapter = new GroupListAdapter(mContext);
        groupManagerList.setAdapter(groupListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_group:
                String field = DBMaster.getInstance().mFieldTable.getIdleField();
                if(field == null){
                    Toast.makeText(mContext,"已达组上限，创建组失败",Toast.LENGTH_LONG).show();
                }else{
                    mCreateGroupDialog.show();
                }
                break;
            default:
        }
    }

    @Override
    public void createCallback(String name, String description) {//在创建组对话中点击确认按钮后，调用该回调函数进行创建组的操作
        String field = DBMaster.getInstance().mFieldTable.getIdleField();
        Group group = new Group();
        group.setField(field);
        group.setGroupName(name);
        group.setGroupDescription(description);
        group.setPriority(0);
        group.setShow(1);
        DBMaster.getInstance().mGroupInfoTable.addGroup(group);
        Toast.makeText(mContext,"组域名为："+field,Toast.LENGTH_LONG).show();
        groupList.add(group);
        groupListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onPriorityCallback(String field) {
        for(Group group:groupList){
            if(group.getField().equals(field)){
                group.setPriority(1);
            }else{
                group.setPriority(0);
            }
        }
        groupListAdapter.setGroupList(groupList);
        groupListAdapter.notifyDataSetChanged();
        editor = pref.edit();
        editor.putString("field",field);
        editor.apply();
        DEFAULT_GROUP_FIELD = field;
        Toast.makeText(mContext,"当前默认组域名为"+field,Toast.LENGTH_SHORT).show();
    }
}
