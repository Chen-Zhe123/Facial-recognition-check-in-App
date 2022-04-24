package com.firefly.face1.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.face1.R;
import com.firefly.face1.bean.Picture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangkd on 18-7-25.
 */
public class PictureListAdapter extends BaseAdapter {
    private List<Picture> pathLists;
    private List<Bitmap> mBitmaps;
    private BitmapFactory.Options mOptions;

    public PictureListAdapter(List<Picture> pPathLists) {
        mBitmaps = new ArrayList<>();
        pathLists = pPathLists==null ? new ArrayList<Picture>() : pPathLists;
        mOptions = new BitmapFactory.Options();
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        if (mBitmaps.size()>0) {
            for (Bitmap bitmap : mBitmaps) {
                bitmap.recycle();
            }
        }

        mBitmaps.removeAll(mBitmaps);
        if (pathLists.size()>0){
            for (Picture picture : pathLists)
                mBitmaps.add(BitmapFactory.decodeFile(picture.getPicturePath(),mOptions));
        }
    }

    public void setData(List<Picture> pPathLists,List<Bitmap> pBitmaps) {
        pathLists = pPathLists==null ? new ArrayList<Picture>() : pPathLists;
        mBitmaps = pBitmaps;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return pathLists.size();
    }

    @Override
    public Object getItem(int position) {
        return pathLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView==null){
            convertView = View.inflate(parent.getContext(), R.layout.image_registration_item,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.fileName.setText(pathLists.get(position).getFileName());
        viewHolder.mImageView.setImageBitmap(mBitmaps.get(position));
        if (pathLists.get(position).isSelected())
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
        else
            viewHolder.mCheckBox.setVisibility(View.GONE);
        return convertView;
    }

    public class ViewHolder{
        ImageView mImageView;
        public ImageView mCheckBox;
        TextView fileName;

        public ViewHolder(View convertView) {
            mImageView = convertView.findViewById(R.id.pic_imageView);
            mCheckBox = convertView.findViewById(R.id.checkBox);
            fileName = convertView.findViewById(R.id.pic_fileName);
        }
    }

}
