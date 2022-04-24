package com.firefly.face1.FragmentView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.FileUitls;
import com.firefly.face1.R;
import com.firefly.face1.adapter.PictureListAdapter;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.Feature;
import com.firefly.face1.bean.Picture;
import com.firefly.face1.dialog.PicRegTipDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class FaceReg implements PicRegTipDialog.TipDialogCancelListener {
    private String TAG = "FaceReg";
    private LinearLayout root;
    private Context mContext;

    private GridView picturelistview;
    private List<Picture> pathLists = new ArrayList<>();
    private List<Bitmap> mBitmaps = new ArrayList<>();
    private PictureListAdapter mAdapter;
    private PicRegTipDialog mTipDialog;
    private FaceReg cameraReg;
    private ImageView selectButton;
    private ImageView cameraRegButton;
    private BeginCameraRegCallback mCallback;

    private BitmapFactory.Options mOptions;

    private boolean isExit = false;

    public void setCameraRegCallback(BeginCameraRegCallback callback){
        mCallback = callback;
    }

    public interface BeginCameraRegCallback{
        void beginCameraReg();
    }

    public FaceReg(LinearLayout pRoot, Context pContext) {
        mOptions = new BitmapFactory.Options();
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        root = pRoot;
        mContext = pContext;
        findView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPictureLists();
            }
        }).start();

    }

    public void startReg(){
        //开始注册
        List<String> temp = new ArrayList<>();
        for (Picture picture:pathLists){
            if (picture.isSelected()){
                temp.add(picture.getPicturePath() );
            }
        }
        asyncImport(temp);
    }

    /**
     * 全部选择图片监听
     */
    public void setAllSelected(){
        for (Picture picture:pathLists){
            picture.setSelected(true);
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 全部取消图片监听
     */
    public void setAllCancel(){
        for (Picture picture:pathLists){
            picture.setSelected(false);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void findView() {
        selectButton = root.findViewById(R.id.select_image_to_reg);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"点击了选择图片按钮",Toast.LENGTH_SHORT).show();
            }
        });
        cameraRegButton = root.findViewById(R.id.camera_reg_button);
        cameraRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.beginCameraReg();
            }
        });
        mTipDialog = new PicRegTipDialog(mContext);
        mTipDialog.setListener(this);
        picturelistview = root.findViewById(R.id.picRegListview);
        picturelistview.setEmptyView(root.findViewById(R.id.picRegEmptyView));
        mAdapter = new PictureListAdapter(pathLists);
        picturelistview.setAdapter(mAdapter);
        picturelistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pathLists.get(position).setSelected(!pathLists.get(position).isSelected());
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    int totalCount = 0;
    int finishCount = 0;
    int successCount = 0;
    int failCount = 0;

    private boolean isRegisterComplete = false;
    private boolean isActivate = false;

    /**
     * 图片注册
     * @param files
     */
    @SuppressLint("StringFormatMatches")
    private void asyncImport(final List<String> files) {
        totalCount = files.size();
        isRegisterComplete = false;
        finishCount = 0;
        successCount = 0;
        failCount = 0;
        mTipDialog.show( mContext.getResources().
                getString(R.string.regPic_tip_registering,
                        totalCount,finishCount,successCount,failCount));
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i<files.size(); i++) {
                    if (isRegisterComplete)
                        break;
                    Log.e(TAG, "files = "+files.get(i));
                    String file = files.get(i);
                    boolean success = false;
                    File facePath = new File(file);
                    if (facePath.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(facePath.getAbsolutePath());
                        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                        //新的模型长度为512,旧的为2048
                        byte[] bytes = new byte[512];
                        try {
                            int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, bytes, 50);
                            if (ret == FaceDetector.NO_FACE_DETECTED) {
                                failCount++;
                            } else if ( ret == 128) {//新版本改为128
                                Feature feature = new Feature();
                                final String uid = UUID.randomUUID().toString();
                                feature.setUserId(uid);
                                feature.setFeature(bytes);
                                feature.setUserName("");

                                if (FaceApi.getInstance().addFeature(feature)) {
                                    success = true;
                                    File faceDir = FileUitls.getFaceDirectory();
                                    if (faceDir != null) {
                                        File saveFacePath = new File(faceDir, uid);
                                        if (FileUitls.saveFile(saveFacePath, bitmap)) {
                                        }
                                    }
                                }
                            } else if (ret == -1) {
    //                            progressDisplay("抽取特征失败");
                            } else {
    //                            progressDisplay("未检测到人脸");
                            }
                        } catch (Exception pE) {
                            isActivate = true;
                            pE.printStackTrace();
                        }
                    }
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
//                        Log.group_manager1(TAG, "失败图片:" + file);
                    }
                    finishCount++;
                    handler.post(new Runnable() {
                        @SuppressLint("StringFormatMatches")
                        @Override
                        public void run() {
                            mTipDialog.updateTipContent( mContext.getResources().
                                    getString(R.string.regPic_tip_registering,
                                            totalCount,finishCount,successCount,failCount));
                        }
                    });
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isActivate) {
                            mTipDialog.dismiss(mContext.getString(R.string.regPic_tip_success));
                            for (Picture picture : pathLists) {
                                picture.setSelected(false);
                            }
                            if (mListener!=null)
                                mListener.picRegCompleteListen();
                        }else{
                            mTipDialog.dismiss(mContext.getString(R.string.regPic_tip_failture));

                        }
                        mAdapter.notifyDataSetChanged();
                        isActivate = false;
                    }
                });
            }
        });
    }

    /**
     * 批量注册，中途是否停止设置
     * @param pExit
     */
    public void setExit(boolean pExit) {
        isExit = pExit;
    }

    private Handler handler = new Handler(Looper.getMainLooper());


    //获取设备中所有图片路径
    private void getPictureLists() {
        pathLists.removeAll(pathLists);
        pathLists.clear();
        if (mBitmaps.size()>0) {
            for (Bitmap bitmap : mBitmaps) {
                bitmap.recycle();
            }
        }
        mBitmaps.removeAll(mBitmaps);

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            if (isExit){
                return;
            }
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            try {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            } catch (Exception pE) {
                pE.printStackTrace();
            }
            Picture picture = new Picture();
            picture.setPicturePath(path);
            picture.setFileName(fileName);
            if (!pathLists.contains(picture)) {
                pathLists.add(picture);
                mBitmaps.add(BitmapFactory.decodeFile(picture.getPicturePath(),mOptions));
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapter!=null)
                mAdapter.setData(pathLists,mBitmaps);
            }
        });
        cursor.close();
    }


    /**
     * 图片注册取消监听
     */
    @Override
    public void cancelRegListener() {
        isRegisterComplete = true;
    }

    private PicRegCompleteListener mListener;

    public void setListener(PicRegCompleteListener pListener) {
        mListener = pListener;
    }

    public interface PicRegCompleteListener{
        void picRegCompleteListen();
    }

}
