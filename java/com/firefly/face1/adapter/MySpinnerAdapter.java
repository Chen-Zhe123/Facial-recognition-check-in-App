package com.firefly.face1.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.utils.FileUitls;
import com.firefly.face1.R;
import com.firefly.face1.bean.DetailedResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MySpinnerAdapter extends BaseAdapter implements android.widget.SpinnerAdapter {

    private Context mContext;
    private List<DetailedResult> detailedList;

    public MySpinnerAdapter(Context context, List<DetailedResult> list){
        mContext = context;
        detailedList = list;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.detailed_result_item,null);
        final DetailedResult bean = detailedList.get(position);
        TextView nameAndId = convertView.findViewById(R.id.sign_student_name_id);
        ImageView faceImage = convertView.findViewById(R.id.sign_face_image);
        TextView signTime = convertView.findViewById(R.id.sign_time_text);
        TextView signState = convertView.findViewById(R.id.sign_state);
        nameAndId.setText(bean.getNameId());
        signTime.setText(bean.getSignTime());
        signState.setText(bean.getSignState());
        File faceDir = FileUitls.getFaceDirectory();
        if (faceDir != null && faceDir.exists()) {
            File file = new File(faceDir, bean.getUserId());
            if (file != null && file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                faceImage.setImageBitmap(bitmap);
            }
        }
        return convertView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return detailedList.size();
    }

    @Override
    public Object getItem(int position) {
        return detailedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.get_view,null);
        TextView getView = convertView.findViewById(R.id.get_view);
        getView.setText("");
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
