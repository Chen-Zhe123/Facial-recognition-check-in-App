/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceFeature;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.ConfigUtils;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;
import com.example.checkvendor.CheckVendor;
import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.Feature;
import com.firefly.face1.dialog.CameraRegDialog;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.baidu.aip.manager.FaceSDKManager.ACTIVATING_SUCCESS;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_ID_PHOTO;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_LIVE;
import static com.baidu.aip.utils.ConfigUtils.TYPE_MODEL;

public class CameraRegActivity extends Activity implements
        CameraRegDialog.RegCallback {

    private static final int FEATURE_DATAS_UNREADY = 1;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    // ??????View;
    private PreviewView previewView;
    // textureView???????????????????????????
    private TextureView textureView;
    // ?????????????????????
    private FaceDetectManager faceDetectManager;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;
    private String userIdOfMaxScore = "";
    private float maxScore = 0;

    //?????????
    private Bitmap face_detect_bg;
    //????????????????????????
    private Bitmap face_recog_bg;

    private ImageView backButton;
    //??????????????????
    private LinearLayout addLayout;

    //??????dialog
    private CameraRegDialog mDialogUtils;
    //??????RegDialogUtils?????????
    private boolean isSHow = true;
    //????????????????????????
    private static boolean isRegist = false;
    //??????????????????Activity
    private boolean isQuit = false;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_reg_interface);
        face_detect_bg = BitmapFactory.decodeResource(getResources(), R.drawable.face_detect_bg);
        face_recog_bg =  BitmapFactory.decodeResource(getResources(), R.drawable.recogition);
        mDialogUtils = new CameraRegDialog(this);
        mDialogUtils.setActivationCallback(this);
        findView();
        init();
        addListener();
        PreferencesUtil.initPrefs(getApplicationContext());
        DBMaster.getInstance().init(getApplicationContext());
        loadFeature2Memery();
        CheckVendor checkVendor = new CheckVendor();
        if (checkVendor.check()!=1) {
            FaceSDKManager.getInstance().init(getApplicationContext(),
                    new FaceSDKManager.SdkInitListener() {

                        @Override
                        public void initStart(FaceDetector mFaceDetector, FaceFeature mFaceFeature) {
                            //?????????????????????????????????????????????
                            //??????
                            mFaceDetector.init(CameraRegActivity.this);
                            mFaceFeature.init(CameraRegActivity.this);
                            //????????????
                            //mFaceDetector.init(RgbVideoIdentityActivity.this, FaceEnvironment);
                            //mFaceFeature.init(RgbVideoIdentityActivity.this, FaceSDK.RecognizeType.RECOGNIZE_LIVE);

                            faceDetectManager.setUseDetect(true);
                        }

                        @Override
                        public void initSuccess() {
                        }

                        @Override
                        public void initStatus(final int statusCode) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (statusCode!=ACTIVATING_SUCCESS)
                                        Toast.makeText(CameraRegActivity.this,
                                                R.string.please_activate, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void initActive(boolean success) {

                        }

                        @Override
                        public void initIrCamera() {

                        }
                    });
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void findView() {
        previewView = findViewById(R.id.preview_view1);
        textureView = findViewById(R.id.texture_view1);
        backButton = findViewById(R.id.back_from_camera_reg);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //??????????????????
        addLayout = findViewById(R.id.register);
        addLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    isRegist = true;
            }
        });
        //???????????????
    }

    private void init() {
        faceDetectManager = new FaceDetectManager(getApplicationContext());
        // ?????????????????????????????????
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // ?????????????????????????????????????????????640 * 480 ??????????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????? camera.getParameters().getSupportedPreviewSizes()?????????????????????
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1280, 720);
        // cameraImageSource.getCameraControl().setPreferredPreviewSize(640, 480);

        // ??????????????????????????????????????????????????????????????????????????????????????????????????????80-200
        FaceSDKManager.getInstance().getFaceDetector().setMinFaceSize(100);
        // FaceSDKManager.getInstance().getFaceDetector().setNumberOfThreads(4);
        // ????????????
        cameraImageSource.setPreviewView(previewView);
        // ???????????????
        faceDetectManager.setImageSource(cameraImageSource);
        // ??????????????????????????????????????????????????????????????????????????????
        faceDetectManager.getFaceFilter().setAngle(20);

        textureView.setOpaque(false);
        // ??????????????????????????????
        textureView.setKeepScreenOn(true);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // ??????????????????
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // ??????????????????
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }
        setCameraType(cameraImageSource);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        // TODO ???????????????????????????
        // cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);
        // TODO ????????????usb?????????
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        // ???????????????????????????????????????????????????
        previewView.getTextureView().setScaleX(-1);
    }

    private RectF rectF = new RectF();
    //?????????????????????????????????????????????
    private Bitmap bitmap = null;
    private String userName = "";

    private void addListener() {
        // ??????????????????????????????????????????
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(final int retCode, final FaceInfo[] infos, final ImageFrame frame) {
                // TODO ???????????????????????????????????????????????????sdk?????????????????????????????????????????????????????????
                if (!isQuit) {
                    if (isRegist) {//face1 register//***??????addLayout??????isRegister??????true,????????????????????????***
                        checkFace(retCode, infos, frame);
                        showFrame(frame, infos, null, null);
                    } else {//face1 detect
                        if (retCode == FaceTracker.ErrCode.OK.ordinal() && infos != null) {
                            Feature tempFeature =  asyncIdentity(frame, infos);
                            if (tempFeature != null) {
                                String user_id = tempFeature.getUserId();
                                File faceDir = FileUitls.getFaceDirectory();
                                if (faceDir != null && faceDir.exists() && !TextUtils.isEmpty(user_id)) {
                                    userName = tempFeature.getUserName();
                                    Bitmap temp = BitmapFactory.decodeFile(faceDir.getPath() + "/" + user_id);
                                    bitmap = Bitmap.createScaledBitmap(temp, (int) CameraRegActivity.this.
                                                    getResources().getDimension(R.dimen.canvas_bitmap_w),
                                            (int) CameraRegActivity.this.getResources().
                                                    getDimension(R.dimen.canvas_bitmap_h), true);
                                    temp.recycle();
                                } else {
                                    if (bitmap!=null) {
                                        bitmap.recycle();
                                        bitmap = null;
                                    }
                                    userName = null;
                                }
                            } else {
                                if (bitmap!=null) {
                                    bitmap.recycle();
                                    bitmap = null;
                                }
                                userName = null;
                            }
                        }else{
                            if (bitmap!=null){
                                bitmap.recycle();
                                bitmap = null;
                            }
                            userName = null;
                        }
                        showFrame(frame, infos, bitmap, userName);
                        if (bitmap!=null) {
                            bitmap.recycle();
                        }
                    }

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        faceDetectManager.start();
        isQuit = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isQuit = true;
        faceDetectManager.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ???????????????
        faceDetectManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetectManager.stop();
    }


    /**
     * ??????????????????????????????
     */
    private void loadFeature2Memery() {

        if (identityStatus != FEATURE_DATAS_UNREADY) {
            return;
        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                FaceApi.getInstance().loadFaceFromLibrary();
                identityStatus = IDENTITY_IDLE;
            }
        });
    }

    /**
     *????????????
     */
    private Feature asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos) {
        if (identityStatus != IDENTITY_IDLE) {
            return null;
        }
        if (faceInfos == null || faceInfos.length == 0) {
            return null;
        }
        Feature feature = null;
        int liveType = PreferencesUtil.getInt(ConfigUtils.TYPE_LIVENSS, ConfigUtils
                .TYPE_NO_LIVENSS);
        if (liveType ==  ConfigUtils.TYPE_NO_LIVENSS) {
            feature = identity(imageFrame, faceInfos[0]);
        }
        else if (liveType ==  ConfigUtils.TYPE_RGB_LIVENSS) {
            if (rgbLiveness(imageFrame, faceInfos[0]) > FaceEnvironment.LIVENESS_RGB_THRESHOLD) {
                //TODO ???????????????????????????????????????????????????????????????
                feature = identity(imageFrame, faceInfos[0]);
            }
        }
//        attrCheck(faceInfos[0],imageFrame);
        return feature;
    }

    private float rgbLiveness(ImageFrame imageFrame, FaceInfo faceInfo) {

        final float rgbScore = FaceLiveness.getInstance().rgbLiveness(imageFrame.getArgb(), imageFrame
                .getWidth(), imageFrame.getHeight(), faceInfo.landmarks);
        return rgbScore;
    }

    @SuppressLint("StringFormatInvalid")
    private Feature identity(ImageFrame imageFrame, FaceInfo faceInfo) {
        float raw  = Math.abs(faceInfo.headPose[0]);
        float patch  = Math.abs(faceInfo.headPose[1]);
        float roll  = Math.abs(faceInfo.headPose[2]);
        // ???????????????????????????20???????????????
        if (raw > 20 || patch > 20 ||  roll > 20) {
            return null;
        }
        identityStatus = IDENTITYING;
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        IdentifyRet identifyRet = null;
        int type = PreferencesUtil.getInt(TYPE_MODEL, RECOGNIZE_LIVE);
        if (type == RECOGNIZE_LIVE) {
            identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks);
        } else if (type == RECOGNIZE_ID_PHOTO) {
            identifyRet = FaceApi.getInstance().identityForIDPhoto(argb, rows, cols, landmarks);
        }
        Feature feature = displayUserOfMaxScore(identifyRet.getUserId(), identifyRet.getScore());
        identityStatus = IDENTITY_IDLE;
        return feature;
    }

    private Feature displayUserOfMaxScore(final String userId, final float score) {
        if (score < 80) {
            return null;
        }
        if (userIdOfMaxScore.equals(userId) ) {
            if (score >= maxScore) {
                maxScore = score;
            }
        } else {
            userIdOfMaxScore = userId;
            maxScore = score;
        }

        final List<Feature> featureList = FaceApi.getInstance().getFeatures(userId);
        if (featureList != null && featureList.size() > 0) {
            Feature feature = featureList.get(0);
            File faceDir = FileUitls.getFaceDirectory();
            if (faceDir != null && faceDir.exists()) {

                return feature;
            }else{
                return null;
            }
        }else{
            return null;
        }

    }

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraRegActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
    }

    /**
     * ??????????????????
     */
    @SuppressLint("ResourceAsColor")
    private void showFrame(ImageFrame frame, FaceInfo[] faceInfos, Bitmap pBitmap, String pUserName) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            return;
        }
        if (faceInfos == null || faceInfos.length == 0) {
            // ??????canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        rectF.set(getFaceRect(faceInfos[0], frame));
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        FaceInfo faceInfo = faceInfos[0];
        // ??????????????????????????????????????????????????????????????????
        previewView.mapFromOriginalRect(rectF);

        float yaw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        if (yaw > 20 || patch > 20 || roll > 20) {
            // ??????????????????????????????
            paint.setColor(Color.YELLOW);
            String text = getString(R.string.Please_face_screen);
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            // ?????????
            canvas.drawBitmap(face_detect_bg,null,rectF, null);
            textureView.unlockCanvasAndPost(canvas);
        } else {
            // ?????????????????????????????????
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            // ????????????????????????
            if (!isRegist) {//****!isRegist????????????????????????***
                canvas.drawBitmap(face_recog_bg, rectF.right, rectF.top - 57, null);
                if (pBitmap != null) {
                    canvas.drawBitmap(pBitmap, rectF.right + 82, rectF.top - 58, null);
                }
                String text = getString(R.string.user_no_register);
                if (pUserName!=null) {
                    text = pUserName;
                }
                //???????????????????????????
                float x = rectF.right + 82 + 70;
                paint.reset();
                paint.setColor(R.color.color_f0f0f0);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(18);
                canvas.drawText(text, x, rectF.top - 20, paint);
            }
            // ?????????
            canvas.drawBitmap(face_detect_bg,null,rectF, null);
            textureView.unlockCanvasAndPost(canvas);
        }
    }


    Rect rect = new Rect();
    /**
     * ????????????????????????
     *
     * @return ???????????????
     */
    // TODO padding?
    public Rect getFaceRect(FaceInfo faceInfo, ImageFrame frame) {

        int[] points = new int[8];
        faceInfo.getRectPoints(points);

        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];

        int width = (right - left);
        int height = (bottom - top);

        left = (int) (faceInfo.mCenter_x - width / 2);
        top = (int) (faceInfo.mCenter_y - height  / 2);

        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width) ;
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);

        return rect;
    }

    @Override
    public void regCallback() {
        isSHow = true;
        isRegist = false;
    }

    @Override
    public void callback(boolean success, Bitmap bitmap) {
        if (success){
            isRegist = false;
            //TODO???????????????
            mDialogUtils.register(bitmap);

        }else {
            isSHow = true;
            isRegist = false;
        }
    }

    /**
     * ???????????????????????????
     */
    private void checkFace(int retCode, FaceInfo[] faceInfos, ImageFrame frame) {
        if ( retCode == FaceTracker.ErrCode.OK.ordinal() && faceInfos != null) {
            FaceInfo faceInfo = faceInfos[0];
            String tip = filter(faceInfo, frame);//***?????????????????????????????????

        } else {
            String tip = checkFaceCode(retCode);

        }
    }

    /**
     * ????????????
     */
    private String filter(FaceInfo faceInfo, ImageFrame imageFrame) {

        String tip = "";
        if (faceInfo.mConf < 0.6) {
            tip = getString(R.string.face_too_low);
            return tip;
        }

        float[] headPose = faceInfo.headPose;
        if (Math.abs(headPose[0]) > 20 || Math.abs(headPose[1]) > 20 || Math.abs(headPose[2]) > 20) {
            tip = getString(R.string.face_angle_too_large);
            return tip;
        }

        int width = imageFrame.getWidth();
        int height = imageFrame.getHeight();
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????????????????????????????
        float ratio = faceInfo.mWidth / (float) height;
        if (ratio > 0.6) {
            tip = getString(R.string.face_too_close);
            return tip;
        } else if (ratio < 0.2) {
            tip = getString(R.string.face_too_far);
            return tip;
        } else if (faceInfo.mCenter_x > width * 3 / 4 ) {
            tip = getString(R.string.face_too_right);
            return tip;
        } else if (faceInfo.mCenter_x < width / 4 ) {
            tip = getString(R.string.face_too_left);
            return tip;
        } else if (faceInfo.mCenter_y > height * 3 / 4 ) {
            tip = getString(R.string.face_too_down) ;
            return tip;
        } else if (faceInfo.mCenter_x < height / 4 ) {
            tip = getString(R.string.face_too_up);
            return tip;
        }
        int liveType = PreferencesUtil.getInt(ConfigUtils.TYPE_LIVENSS, ConfigUtils
                .TYPE_NO_LIVENSS);
        if (liveType ==  ConfigUtils.TYPE_NO_LIVENSS) {
            //???????????????????????????????????????
            List<Feature> featureList = mDialogUtils.getFeatures(faceInfo ,imageFrame);
            if (featureList.size()<=0) {
                isRegist = false;
                saveFace(faceInfo, imageFrame);
            }else{
                tip = getString(R.string.user_face_registered);
                isRegist = false;
                toast(tip);
            }
        } else if (liveType ==  ConfigUtils.TYPE_RGB_LIVENSS) {
            if (rgbLiveness(imageFrame, faceInfo) > FaceEnvironment.LIVENESS_RGB_THRESHOLD) {
                //???????????????????????????????????????
                List<Feature> featureList = mDialogUtils.getFeatures(faceInfo ,imageFrame);
                if (featureList.size()<=0) {
                    isRegist = false;
                    saveFace(faceInfo, imageFrame);
                }else{
                    tip = getString(R.string.user_face_registered);
                    isRegist = false;
                    toast(tip);
                }
            }
        }
        return tip;
    }

    private String checkFaceCode(int errCode) {
        String tip = "";
//        if (errCode == FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal() ) {
//        } else
        if (errCode == FaceTracker.ErrCode.IMG_BLURED.ordinal() ||
                errCode == FaceTracker.ErrCode.PITCH_OUT_OF_DOWN_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.PITCH_OUT_OF_UP_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.YAW_OUT_OF_LEFT_MAX_RANGE.ordinal() ||
                errCode == FaceTracker.ErrCode.YAW_OUT_OF_RIGHT_MAX_RANGE.ordinal())  {
            tip = getString(R.string.still_view_screen);
        } else if (errCode == FaceTracker.ErrCode.POOR_ILLUMINATION.ordinal()) {
            tip = getString(R.string.light_too_dark);
        } else if (errCode == FaceTracker.ErrCode.UNKNOW_TYPE.ordinal()){
            tip =  getString(R.string.detect_no_face);
        }
        return tip;
    }

    private void saveFace(FaceInfo faceInfo, ImageFrame imageFrame) {
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());//????????????????????????????????????????????????
        if (isSHow){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDialogUtils.show(bitmap,true);
                }
            });
            isSHow = false;
        }
    }

    public interface VideoRegCompleteListener{
        void videoRegCompleteListen();
    }
}
