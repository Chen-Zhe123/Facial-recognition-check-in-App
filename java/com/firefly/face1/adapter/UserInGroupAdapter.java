package com.firefly.face1.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import java.util.Date;
import java.util.List;

public class UserInGroupAdapter extends RecyclerView.Adapter<UserInGroupAdapter.ViewHolder> {

    private Context mContext;
    private List<UserInfo> userInfoList;
    private String format = "%02d";
    private String Tag = "UserInGroupAdapter";

    public UserInGroupAdapter(Context context){
        mContext = context;
    }
    @Override
    public UserInGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_library_item, parent,
                false);
        UserInGroupAdapter.ViewHolder holder = new UserInGroupAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UserInfo userInfo = userInfoList.get(position);
        holder.userId.setText(userInfo.getUserId());
        holder.studentId.setText("学号："+userInfo.getStudentId());
        Date date = new Date(userInfo.getRegTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = dateFormat.format(date);
        holder.userName.setText(mContext.getString(R.string.db_user_name) + userInfo.getUserName());
        holder.registrationTime.setText(mContext.getString(R.string.db_user_regist_time) + time);
        holder.userNumber.setText(mContext.getString(R.string.user_number)+String.format(format, position+1));
        if(userInfo.isShow()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            if(FaceLibrary.code == 1) {//待改进
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            userInfo.setSelected(true);
                        } else {
                            userInfo.setSelected(false);
                        }
                    }
                });
            }
            holder.checkBox.setChecked(userInfo.isSelected());
        }
        else{
            holder.checkBox.setVisibility(View.GONE);
        }

        File faceDir = FileUitls.getFaceDirectory();
        if (faceDir != null && faceDir.exists()) {
            File file = new File(faceDir, userInfo.getUserId());
            if (file != null && file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                holder.imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    public void setUserInfoList(List<UserInfo> userInfoList) {
        this.userInfoList = userInfoList;
        this.notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentId;
        TextView userName;
        ImageView imageView;
        TextView registrationTime;
        TextView userNumber;
        TextView userId;
        CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.user_image);
            userName = view.findViewById(R.id.user_name);
            studentId = view.findViewById(R.id.student_id);
            registrationTime = view.findViewById(R.id.registration_time);
            userNumber = view.findViewById(R.id.user_number);
            userId = view.findViewById(R.id.user_id);
            checkBox = view.findViewById(R.id.single_select_CheckBox);
        }
    }
}
