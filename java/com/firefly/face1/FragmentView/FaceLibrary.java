package com.firefly.face1.FragmentView;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.DividerItemDecoration;
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
import com.firefly.face1.adapter.UserInfoAdapter;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.Feature;
import com.firefly.face1.bean.UserInfo;
import com.firefly.face1.dialog.DeleteUserDialog;
import com.firefly.face1.dialog.ModifyUserDialog;

import java.util.ArrayList;
import java.util.List;

public class FaceLibrary implements ModifyUserDialog.UpdateCallback, DeleteUserDialog.DeleteUserListener {
    private LinearLayout root;
    private Context mContext;
    private RecyclerView faceLibraryLists;
    private TextView emptyView;
    private UserInfoAdapter adapter;
    private List<Feature> mFeatureList = new ArrayList<>();
    private List<UserInfo> userInfoList = new ArrayList<>();
    private ArrayList<UserInfo> deleteList = new ArrayList<>();
    private ModifyUserDialog mDialogUtils;
    private DeleteUserDialog mDeleteUserDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private String format = "%02d";
    private LinearLayout bottomOption;
    private TextView deleteButton;
    private TextView cancelButton;
    private CheckBox selectAllButton;
    private TextView manageButton;
    private TextView cancelSearchButton;
    private EditText searchEditText;
    private ImageView cancelSearch;
    private ImageView searchButton;
    public static int code = 1;

    public FaceLibrary(LinearLayout pRoot, Context pContext) {
        DBMaster.getInstance().init(pContext.getApplicationContext());
        root = pRoot;
        mContext = pContext;
        mDialogUtils = new ModifyUserDialog(mContext);
        mDialogUtils.setCallback(this);
        mDeleteUserDialog = new DeleteUserDialog(mContext);
        mDeleteUserDialog.setDeleteUserListener(this);
        findView();
        addListener();
        init();
    }

    public void init(){
        userInfoList = DBMaster.getInstance().mFeatureTable.queryAllUserInfo();
        adapter.setUserList(userInfoList);
        int count = 1;
        for (int i = 0; i < (int) Math.log10(mFeatureList.size()); i++) {
            count++;
        }
        format = "%0"+count+"d";
        if (adapter.getItemCount()>0){
            emptyView.setVisibility(View.GONE);
        }else{
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void findView() {//待整改
        faceLibraryLists = root.findViewById(R.id.FaceLibraryLists);
        emptyView = root.findViewById(R.id.empty_text);
        manageButton = root.findViewById(R.id.manage);
        searchEditText = root.findViewById(R.id.search_bar);
        cancelSearchButton = root.findViewById(R.id.cancel_search);
        cancelSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageButton.setVisibility(View.VISIBLE);
                cancelSearchButton.setVisibility(View.GONE);
                searchEditText.setText("");
                adapter.setUserList(DBMaster.getInstance().mFeatureTable.queryAllUserInfo());
            }
        });
        cancelSearch = root.findViewById(R.id.clean_EditText);
        cancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
            }
        });
        searchButton = root.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = searchEditText.getText().toString();
                adapter.setUserList(DBMaster.getInstance().mFeatureTable.searchUser(key));
                cancelSearchButton.setVisibility(View.VISIBLE);
                manageButton.setVisibility(View.GONE);
            }
        });
        bottomOption = root.findViewById(R.id.bottom_option);
        deleteButton = root.findViewById(R.id.delete_Button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int pos =  0;pos < userInfoList.size();pos++){
                    if(userInfoList.get(pos).isSelected()){
                        deleteList.add(userInfoList.get(pos));
                    }
                }
                showAlertDialog(deleteList);
            }
        });
        cancelButton = root.findViewById(R.id.cancel_Button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomOption.setVisibility(View.GONE);
                hideAllCheckBox();
            }
        });
        selectAllButton = root.findViewById(R.id.all_select_CheckBox);
        selectAllButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (UserInfo info:userInfoList) {
                        info.setSelected(true);
                    }
                } else{
                    for (UserInfo info:userInfoList) {
                        info.setSelected(false);
                    }
                }
                adapter.setUserList(userInfoList);
                adapter.notifyDataSetChanged();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        //设置RecyclerView 布局
        faceLibraryLists.setLayoutManager(layoutManager);
        faceLibraryLists.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        adapter = new UserInfoAdapter(mContext);
        faceLibraryLists.setAdapter(adapter);
    }

    private void addListener() {//待整改

        adapter.setOnItemClickListener(new FaceLibrary.
                OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                List<UserInfo> userList = adapter.getUserList();
                if (userList.size() > position) {
                    UserInfo user = userList.get(position);
                    if (mDialogUtils!=null){
                        mDialogUtils.show(user,position);
                    }
                }

            }

            @Override
            public void onItemLongClick(CheckBox checkBox, int position) {
                if (position <= adapter.getUserList().size()) {
                     showAllCheckBox();
                     bottomOption.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void hideAllCheckBox(){
        for(int pos = 0;pos < userInfoList.size();pos++){//待优化
            userInfoList.get(pos).setShow(false);
            adapter.notifyDataSetChanged();
        }
    }
    private void showAllCheckBox(){
        for(int pos = 0;pos < userInfoList.size();pos++){//待优化
            userInfoList.get(pos).setShow(true);
            adapter.notifyDataSetChanged();
        }
    }

    private void showAlertDialog(final ArrayList deleteList) {
        mDeleteUserDialog.show(deleteList);
    }

    @Override
    public void updateCallback(int pos,String updateName,String updateStudentId) {
        userInfoList.get(pos).setUserName(updateName);
        userInfoList.get(pos).setStudentId(updateStudentId);
        if (FaceApi.getInstance().updateUserInfo(userInfoList.get(pos))) {
            adapter.notifyItemChanged(pos);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.modify_user_success, Toast.LENGTH_LONG).show();
                }
            });
        }else{
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.modify_user_failture, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void deleteUserListen(ArrayList<UserInfo> deleteList) {
        boolean deleteSuccess = false;
        bottomOption.setVisibility(View.GONE);
        if (deleteList!=null){
            for(int pos = 0;pos < deleteList.size();pos++){
                deleteSuccess = FaceApi.getInstance().deleteFeatureByUserId(deleteList.get(pos).getUserId());
            }
            if (deleteSuccess) {
                Toast.makeText(mContext, R.string.delete_user_success, Toast.LENGTH_SHORT).show();
                adapter.getUserList().removeAll(deleteList);
                adapter.notifyDataSetChanged();
            }else{
                Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface OnItemClickListener{//不该在这里

        void onItemClick(View view, int position);
        void onItemLongClick(CheckBox checkBox, int position);
    }

}
