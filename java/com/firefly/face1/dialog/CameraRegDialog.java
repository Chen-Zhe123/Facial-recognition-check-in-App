package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.ConfigUtils;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.Feature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class CameraRegDialog {

    private Button regBtn;
    private Button backBtn;
    private Context context;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private Dialog activationDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private RegCallback regCallback;
    private Matrix matrix = new Matrix();


    public CameraRegDialog(Context pContext) {
        context = pContext;
        PreferencesUtil.initPrefs(pContext);
        matrix.postScale(-1, 1);   //镜像水平翻转
    }

    private LinearLayout initView(Bitmap pBitmap){
        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootParams.gravity = Gravity.CENTER;
        root.setBackgroundColor(context.getResources().getColor(R.color.color_181820 ));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        TextView titleTv = new TextView(context);
        titleTv.setText(R.string.cameraReg_dialog_title);
        titleTv.setTextSize(dip2px(10));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER;
        titleParams.topMargin = dip2px(10);
        titleParams.rightMargin = dip2px(30);
        titleParams.leftMargin = dip2px(30);

        mImageView = new ImageView(context);
        mImageView.setImageBitmap(pBitmap);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dip2px(300), dip2px(300));
        imageParams.gravity = Gravity.CENTER;
        imageParams.topMargin = dip2px(40);
        imageParams.rightMargin = dip2px(30);
        imageParams.leftMargin = dip2px(30);

        LinearLayout.LayoutParams regParams = new LinearLayout.LayoutParams(dip2px(260), dip2px(48));
        regParams.gravity = Gravity.CENTER;
        regParams.topMargin = dip2px(40);
        regParams.rightMargin = dip2px(40);
        regParams.leftMargin = dip2px(40);

        regBtn = new Button(context);
        // activateBtn.setId(100);
        regBtn.setText(R.string.reg_ok);
        regBtn.setBackgroundResource(R.drawable.reg_btn_bg);
        regBtn.setTextColor(Color.WHITE);
        regBtn.setTextSize(dip2px(17));
        regBtn.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dip2px(260), dip2px(48));
        backParams.gravity = Gravity.CENTER;
        backParams.topMargin = dip2px(5);
        backParams.bottomMargin = dip2px(20);
        backParams.rightMargin = dip2px(40);
        backParams.leftMargin = dip2px(40);
        backBtn = new Button(context);
        // activateBtn.setId(100);
        backBtn.setText(R.string.reg_cancel);
        backBtn.setTextColor(Color.WHITE);
        backBtn.setTextSize(dip2px(17));
        backBtn.setGravity(Gravity.CENTER);
        backBtn.setBackgroundResource(R.drawable.reg_btn_bg);

        root.addView(titleTv, titleParams);
        root.addView(mImageView, imageParams);
        root.addView(regBtn, regParams);
        root.addView(backBtn, backParams);
        return root;
    }

    public void show(Bitmap pBitmap,boolean isMatrix) {
        if (isMatrix)
            mBitmap = Bitmap.createBitmap(pBitmap, 0, 0, pBitmap.getWidth(), pBitmap.getHeight(), matrix, true);
        else
            mBitmap = pBitmap;
        activationDialog = new Dialog(context);
        activationDialog.setContentView(initView(mBitmap));
        activationDialog.setCancelable(false);
        activationDialog.show();
        regBtn.requestFocus();
        addListener();
    }


    private void addListener() {
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (regCallback != null) {
                    regCallback.callback(true,mBitmap);
                    activationDialog.dismiss();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activationDialog != null) {
                    regCallback.callback(false,mBitmap);
                    activationDialog.dismiss();
                }
            }
        });

    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int dip2px(int dip) {
        Resources resources = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics());
        return px;
    }

    public void setActivationCallback(RegCallback callback) {
        this.regCallback = callback;
    }

    public interface RegCallback {
        void regCallback();
        void callback(boolean success,Bitmap pBitmap);
    }

    /**
     * 用bitmap进行人脸注册
     * @param bitmap
     */
    public void register(final Bitmap bitmap) {
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         *
         */
        final String uid = UUID.randomUUID().toString();

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                byte[] bytes = new byte[512];
                int ret = 0;
                int type = PreferencesUtil.getInt(ConfigUtils.TYPE_MODEL, ConfigUtils.RECOGNIZE_LIVE);
                if (type == ConfigUtils.RECOGNIZE_LIVE) {
                    ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, bytes, 50);
                } else if (type == ConfigUtils.RECOGNIZE_ID_PHOTO) {
                    ret = FaceSDKManager.getInstance().getFaceFeature().faceFeatureForIDPhoto(argbImg, bytes, 50);
                }
                if (ret == FaceDetector.NO_FACE_DETECTED) {
                    toast(context.getString(R.string.camera_dialog_minfacesize));
                } else if (ret != -1) {
                    Feature feature = new Feature();
                    feature.setUserId(uid);
                    feature.setFeature(bytes);
                    feature.setUserName("");
                    if (FaceApi.getInstance().addFeature(feature)) {
                        File faceDir = FileUitls.getFaceDirectory();
                        if (faceDir != null) {
                            File file = new File(faceDir, uid);
                            // 压缩人脸图片至300 * 300，减少网络传输时间
                            ImageUtils.resize(bitmap, file, context.getResources().getDimension(R.dimen.save_bitmap_w),
                                    context.getResources().getDimension(R.dimen.save_bitmap_h));
                            //将该用户添加至group表中
                            if(DBMaster.getInstance().mGroupTable.initUserToTable(uid)){

                            }
                        } else {
                            toast(context.getString(R.string.save_bitmap_failture));
                        }
                        toast(context.getString(R.string.reg_success));
                    } else {
                        toast(context.getString(R.string.reg_failture));
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException pE) {
                        pE.printStackTrace();
                    }
                    if (regCallback != null) {
                        regCallback.regCallback();
                    }
                } else {
                    toast(context.getString(R.string.decimation_failture));
                }
                FaceApi.getInstance().loadFaceFromLibrary();//重新加载人脸库
            }
        });
    }


    /**
     * 获取faceInfo人脸特征在人脸库中的相似度大于80
     * @param faceInfo
     * @param imageFrame
     * @return
     */
    public List<Feature> getFeatures(FaceInfo faceInfo, ImageFrame imageFrame){
        FaceApi.getInstance().loadFaceFromLibrary();//此处必须从总库中加载人脸
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks);
        if (identifyRet.getScore()>80.0f)//该人脸与人脸库的人脸特征对比，如果出现大于80的话，认为已经注册过了。
            return FaceApi.getInstance().getFeatures(identifyRet.getUserId());
        else return new ArrayList<>();
    }

}
