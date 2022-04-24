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

public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.ViewHolder> {

    private List<UserInfo> userList = new ArrayList<>();
    private FaceLibrary.OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private String format = "%02d";
    public UserInfoAdapter(Context context){
        mContext = context;
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

    public void setUserList(List<UserInfo> userList) {
        this.userList = userList;
        this.notifyDataSetChanged();
    }

    public List<UserInfo> getUserList() {
        return userList;
    }

    public void setOnItemClickListener(FaceLibrary.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_library_item, parent,
                false);
        return new ViewHolder(view);
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
        if(user.isShow()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    user.setSelected(isChecked);
                }
            });
            holder.checkBox.setChecked(user.isSelected());//用于设置全选
        }else {
            holder.checkBox.setVisibility(View.GONE);
        }
        File faceDir = FileUitls.getFaceDirectory();
        if (faceDir != null && faceDir.exists()) {
            File file = new File(faceDir, user.getUserId());
            if (file != null && file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                holder.imageView.setImageBitmap(bitmap);
            }
        }
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, pos);//方法在Utils中实现
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(holder.checkBox, pos);//方法在Utils中实现
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}