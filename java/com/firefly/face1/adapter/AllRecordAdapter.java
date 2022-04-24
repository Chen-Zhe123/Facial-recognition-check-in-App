package com.firefly.face1.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.face1.R;
import com.firefly.face1.bean.AllRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AllRecordAdapter extends ArrayAdapter<AllRecord> {

    private int resourceId;
    private final static String TAG = "AllRecordAdapter";

    public AllRecordAdapter(@NonNull Context context, int resource, List<AllRecord> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        AllRecord item = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        viewHolder.item_image.setImageBitmap(item.getImageRes());
        Date date = new Date(item.getCaptureTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = dateFormat.format(date);
        viewHolder.item_time.setText("时间： "+time);
        if(item.getUserName() == null){//判断条件还要改
            viewHolder.item_name_id.setText("未注册人员");
        }
        else {
            viewHolder.item_name_id.setText(item.getNameId());
        }
        return view;
    }

    static class ViewHolder{
        ImageView item_image;
        TextView item_name_id;
        TextView item_time;

        public ViewHolder(View view){
            item_image = view.findViewById(R.id.face_image);
            item_name_id = view.findViewById(R.id.face_name_id);
            item_time = view.findViewById(R.id.face_time);
        }

    }

}

