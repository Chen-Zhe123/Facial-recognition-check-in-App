package com.firefly.face1.adapter;

import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.face1.bean.Feature;
import com.baidu.aip.utils.FileUitls;
import com.firefly.face1.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PartRecordAdapter extends BaseAdapter {

    private List<Feature> mFeatureList;
    private Date date = new Date();

    public PartRecordAdapter(List<Feature> pFeatureList) {
        mFeatureList = pFeatureList==null?new ArrayList<Feature>():pFeatureList;
    }

    @Override
    public int getCount() {
        return mFeatureList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFeatureList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<Feature> pFeatureList){
        mFeatureList = (pFeatureList==null?new ArrayList<Feature>():pFeatureList);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mVH = null;
        if (convertView==null){
            convertView = View.inflate(parent.getContext(), R.layout.record_item,null );
            mVH = new ViewHolder(convertView);
            convertView.setTag(mVH);
        }else{
            mVH = (ViewHolder) convertView.getTag();
        }
        if (mFeatureList.get(position).getUpdateTime()==-1){
            mVH.timeTv.setText(R.string.record_time);
            mVH.mImageView.setBackgroundResource(R.drawable.emty_user);
        }else {
            date.setTime(mFeatureList.get(position).getUpdateTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String time = dateFormat.format(date);
            mVH.timeTv.setText(time);
            File faceDir = FileUitls.getFaceDirectory();
            mVH.mImageView.setImageBitmap(BitmapFactory.decodeFile(
                    faceDir.getPath() + "/" +
                            mFeatureList.get(position).getUserId()));
        }

        mVH.nameTv.setText(mFeatureList.get(position).getUserName());
        return convertView;
    }

    private class ViewHolder{
        ImageView mImageView;
        TextView nameTv;
        TextView timeTv;

        public ViewHolder(View convertView) {
            mImageView = convertView.findViewById(R.id.record_iv);
            nameTv = convertView.findViewById(R.id.record_name_tv);
            timeTv = convertView.findViewById(R.id.record_time_tv);
        }
    }
}
