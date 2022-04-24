package com.firefly.face1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;
import com.firefly.face1.bean.DetailedResult;
import com.firefly.face1.bean.PreviewResult;
import com.firefly.face1.bean.UserInfo;
import com.firefly.face1.dialog.DetailedResultDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PreviewResultAdapter extends RecyclerView.Adapter<PreviewResultAdapter.ViewHolder> {

    private Context mContext;
    private List<PreviewResult> mPreResultList;

    public PreviewResultAdapter(Context context){
        mContext = context;
    }

    public void setPreResultList(List<PreviewResult> previewResultList){
        mPreResultList = previewResultList;
        this.notifyDataSetChanged();
    }
    public List<PreviewResult> getPreResultList(){
        return mPreResultList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_result_item,parent,false);
        PreviewResultAdapter.ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PreviewResult preResult = mPreResultList.get(position);
        List<String> needSignList = preResult.getNeedSignList();
        Map<String,Long> alreadySignMap = preResult.getAlreadySignMap();
        Map<String,Long> lateSignMap = preResult.getLateSignMap();
        List<String> neverSignMap = preResult.getNeverSignList();
        Date date = new Date(Long.valueOf(preResult.getWorkTime()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = dateFormat.format(date);
        holder.workTime.setText("时间： "+time);
        holder.groupName.setText("组名： "+preResult.getGroupName());
        holder.needSignCount.setText("需到人数： "+needSignList.size());
        holder.alreadySignCount.setText("已到人数： "+alreadySignMap.size());
        holder.lateSignCount.setText("迟到人数： "+lateSignMap.size());
        holder.neverSignCount.setText("未到人数： "+neverSignMap.size());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DetailedResultDialog(mContext).show(preResult);
            }
        });
        if(preResult.getShow()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    preResult.setIsSelected(isChecked);
                }
            });
            holder.checkBox.setChecked(preResult.getIsSelected());
        }
        else{
            holder.checkBox.setVisibility(View.GONE);
        }

        List<DetailedResult> detailedList = new ArrayList<>();
        //此处遇到过java.lang.IndexOutOfBoundsException: Index: 0, Size: 0异常，
        // 原因是人脸库中对某人脸进行了删除操作，导致加载签到结果时查找不到该人脸数据
        //解决方案：1.对人脸库中的人脸进行假性删除，即备份数据，导致的问题是，系统会越用越慢
        //2.不再加载已删除人脸的数据
        for(String id:preResult.getNeedSignList()){
            DetailedResult bean = new DetailedResult();
            List<UserInfo> infoList = DBMaster.getInstance().mFeatureTable.queryUserInfoById(id);
            if(infoList.size() > 0) {
                UserInfo info = infoList.get(0);
                bean.setUserId(id);
                bean.setUserName(info.getUserName());
                bean.setStudentId(info.getStudentId());
                if (alreadySignMap.containsKey(id)) {
                    bean.setSignState("已到");
                    bean.setSignTime(alreadySignMap.get(id));
                } else if (lateSignMap.containsKey(id)) {
                    bean.setSignState("迟到");
                    bean.setSignTime(alreadySignMap.get(id));
                } else {
                    bean.setSignState("未到");
                    bean.setSignTime(0);
                }
                detailedList.add(bean);
            }
        }
        MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter(mContext,detailedList);
        holder.spinner.setAdapter(spinnerAdapter);
    }

    @Override
    public int getItemCount() {
        return mPreResultList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView workTime;
        TextView groupName;
        CheckBox checkBox;
        TextView needSignCount;
        TextView alreadySignCount;
        TextView lateSignCount;
        TextView neverSignCount;
        Spinner spinner;

        public ViewHolder(View itemView) {
            super(itemView);
            workTime = itemView.findViewById(R.id.work_time);
            groupName = itemView.findViewById(R.id.work_group_name);
            checkBox = itemView.findViewById(R.id.preview_checkbox);
            needSignCount = itemView.findViewById(R.id.need_sign_count);
            alreadySignCount = itemView.findViewById(R.id.already_sign_count);
            lateSignCount = itemView.findViewById(R.id.late_sign_count);
            neverSignCount = itemView.findViewById(R.id.never_sign_count);
            spinner = itemView.findViewById(R.id.preview_view_spinner);
        }
    }

}
