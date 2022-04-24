package com.firefly.face1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.utils.FileUitls;
import com.firefly.face1.R;
import com.firefly.face1.bean.UserInfo;
import com.firefly.face1.FragmentView.FaceLibrary;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddUserToGroupAdapter extends RecyclerView.Adapter<AddUserToGroupAdapter.ViewHolder> {

    private List<UserInfo> userList1 = new ArrayList<>();
    private List<UserInfo> userList = new ArrayList<>();
    private FaceLibrary.OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private String format = "%02d";
    private String TAG = "AddUserToGroupAdapter";

    public AddUserToGroupAdapter(Context context){
        mContext = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentId;
        TextView userName;
        ImageView imageView;
        TextView registrationTime;
        TextView userNumber;
        TextView userId;
        ImageView selectedTag;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.user_image);
            userName = view.findViewById(R.id.user_name);
            studentId = view.findViewById(R.id.student_id);
            registrationTime = view.findViewById(R.id.registration_time);
            userNumber = view.findViewById(R.id.user_number);
            userId = view.findViewById(R.id.user_id);
            selectedTag = view.findViewById(R.id.already_in_group);
            checkBox = view.findViewById(R.id.single_select_CheckBox);
        }
    }

    public void setUserList(List<UserInfo> inGroupList,List<UserInfo> allUserList) {
        this.userList = allUserList;
        for(UserInfo user : allUserList){
            if(inGroupList.contains(user)){
                user.setInGroup(true);
            }
        }
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_library_item, parent,
                false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final UserInfo user = userList.get(position);
        holder.userId.setText(mContext.getString(R.string.user_id) + user.getUserId());
        holder.studentId.setText(mContext.getString(R.string.student_id) + user.getStudentId());
        Date date = new Date(user.getRegTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = dateFormat.format(date);
        holder.userName.setText(mContext.getString(R.string.db_user_name) + user.getUserName());
        holder.registrationTime.setText(mContext.getString(R.string.db_user_regist_time) + time);
        holder.userNumber.setText(mContext.getString(R.string.user_number)+String.format(format, position+1));
        if(user.getInGroup()){
            holder.selectedTag.setVisibility(View.VISIBLE);
        }else {
            if (user.isShow()) {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            user.setSelected(true);
                        } else {
                            user.setSelected(false);
                        }
                    }
                });
                holder.checkBox.setChecked(user.isSelected());//用于设置全选
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }
        }
        File faceDir = FileUitls.getFaceDirectory();
        if (faceDir != null && faceDir.exists()) {
            File file = new File(faceDir, user.getUserId());
            if (file != null && file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                holder.imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}