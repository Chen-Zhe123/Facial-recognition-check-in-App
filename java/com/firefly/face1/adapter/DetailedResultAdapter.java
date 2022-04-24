package com.firefly.face1.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.utils.FileUitls;
import com.firefly.face1.R;
import com.firefly.face1.bean.DetailedResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailedResultAdapter extends RecyclerView.Adapter<DetailedResultAdapter.ViewHolder> {

    private Context mContext;
    private static List<DetailedResult> mDetailedResultList = new ArrayList<>();

    public DetailedResultAdapter(Context context){
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detailed_result_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DetailedResult result = mDetailedResultList.get(position);
        File faceDir = FileUitls.getFaceDirectory();
        if (faceDir != null && faceDir.exists()) {
            File file = new File(faceDir, result.getUserId());
            if (file != null && file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                holder.faceImage.setImageBitmap(bitmap);
            }
        }
        holder.nameAndId.setText(result.getNameId());
        holder.signTime.setText(result.getSignTime());
        holder.signState.setText(result.getSignState());
    }

    @Override
    public int getItemCount() {
        return mDetailedResultList.size();
    }

    public void setResultList(List<DetailedResult> detailedResultList){
        mDetailedResultList = detailedResultList;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView nameAndId;
        ImageView faceImage;
        TextView signTime;
        TextView signState;

        public ViewHolder(View itemView) {
            super(itemView);
            faceImage = itemView.findViewById(R.id.sign_face_image);
            nameAndId = itemView.findViewById(R.id.sign_student_name_id);
            signTime = itemView.findViewById(R.id.sign_time_text);
            signState = itemView.findViewById(R.id.sign_state);
        }

    }
}
