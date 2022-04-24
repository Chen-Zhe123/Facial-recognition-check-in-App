package com.firefly.face1.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;
import com.firefly.face1.bean.Group;
import com.firefly.face1.bean.UserInfo;
import com.firefly.face1.dialog.DeleteGroupDialog;
import com.firefly.face1.dialog.ModifyGroupDialog;
import com.firefly.face1.dialog.ScanGroupDialog;

import java.util.ArrayList;
import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {
    private List<Group> groupList;
    private Context mContext;
    private SetPriorityCallback mCallback;
    private ScanGroupDialog mScanGroupDialog;
    private DeleteGroupDialog mDeleteGroupDialog;
    private ModifyGroupDialog mModifyGroupDialog;
    private String TAG = "GroupListAdapter";

    public GroupListAdapter(Context context){
        mContext = context;
    }

    @Override
    public GroupListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent,
                false);
        GroupListAdapter.ViewHolder holder = new GroupListAdapter.ViewHolder(view);
        return holder;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Group group = groupList.get(position);
        final int pos = position;
        int count;
        count = DBMaster.getInstance().mGroupTable.queryCountByField(group.getField());
        holder.groupName.setText(group.getGroupName());
        holder.groupDescription.setText(group.getGroupDescription());
        if(group.getPriority() > 0){
            holder.isPriority.setChecked(true);
        }
        else {
            holder.isPriority.setChecked(false);
        }
        holder.isPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onPriorityCallback(group.getField()
                );
            }
        });
        holder.userNumber.setText(String.valueOf(count));
        holder.scanGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> userIdList;
                userIdList = DBMaster.getInstance().mGroupTable.queryIdListByField(group.getField());
                List<UserInfo> userInfoList = new ArrayList<>();
                if(userIdList.size() > 0) {
                    for (String userId : userIdList) {
                        Log.d(TAG, "onClick: "+"userID："+userId);
                        List<UserInfo> userInfoList1;
                        userInfoList1 =  DBMaster.getInstance().mFeatureTable.queryUserInfoById(userId);
                        if(userInfoList1.size() > 0 && userInfoList1 != null) {
                            userInfoList.add(userInfoList1.get(0));
                        }
                    }
                }else{
                    userInfoList = null;
                }
                mScanGroupDialog = new ScanGroupDialog(mContext);
                mScanGroupDialog.show(group.getField(),userInfoList,group.getGroupName());
            }
        });
        holder.modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!group.getField().equals("group_0")) {
                    mModifyGroupDialog = new ModifyGroupDialog(mContext);
                    mModifyGroupDialog.setCallback(new ModifyGroupDialog.ModifyGroupCallback() {
                        @Override
                        public void modifyCallback(String name, String description, String field) {
                            if (DBMaster.getInstance().mGroupInfoTable.modifyGroup(name, description, field)) {
                                Toast.makeText(mContext, "修改成功", Toast.LENGTH_LONG).show();
                                group.setGroupName(name);
                                group.setGroupDescription(description);
                                notifyItemChanged(pos);
                            }
                        }

                    });
                    mModifyGroupDialog.show(group.getField());
                }else{
                    Toast.makeText(mContext,"系统组无法修改!",Toast.LENGTH_LONG).show();
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,group.getField(),Toast.LENGTH_LONG).show();
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!group.getField().equals("group_0")){
                    mDeleteGroupDialog = new DeleteGroupDialog(mContext);
                    mDeleteGroupDialog.setCallback(new DeleteGroupDialog.DeleteGroupCallback() {
                        @Override
                        public void deleteCallback(String field) {
                            DBMaster.getInstance().mGroupInfoTable.deleteGroup(field);
                            DBMaster.getInstance().mFieldTable.setNotInUsing(field);
                            DBMaster.getInstance().mGroupTable.deleteUserByField(field);
                            groupList.remove(pos);
                            notifyDataSetChanged();
                        }
                    });
                    mDeleteGroupDialog.show(group.getField());
                } else {
                    Toast.makeText(mContext,"系统组无法删除!",Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView groupName;
        TextView groupDescription;
        RadioButton isPriority;
        ImageView modifyButton;
        TextView userNumber;
        ImageView scanGroup;

        public ViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            groupDescription = itemView.findViewById(R.id.group_description);
            isPriority = itemView.findViewById(R.id.is_priority);
            modifyButton = itemView.findViewById(R.id.modify_group);
            userNumber = itemView.findViewById(R.id.number_2);
            scanGroup = itemView.findViewById(R.id.scan_group);
        }
    }
    public void setCallback(SetPriorityCallback callback){
        mCallback = callback;
    }
    public interface SetPriorityCallback {
        void onPriorityCallback(String field);
    }
}
