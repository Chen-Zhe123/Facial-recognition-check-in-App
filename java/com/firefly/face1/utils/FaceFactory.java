package com.firefly.face1.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.utils.ConfigUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceAttribute;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;
import com.firefly.face1.R;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.Feature;
import com.firefly.face1.dialog.BeginWorkingDialog;
import com.firefly.face1.dialog.CameraRegDialog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_ID_PHOTO;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_LIVE;
import static com.baidu.aip.utils.ConfigUtils.TYPE_MODEL;

public class FaceFactory extends Activity {

    private SharedPreferences pref;
    private static String DEFAULT_GROUP_FIELD;
    private static String DEFAULT_GROUP_NAME;
    private static Boolean isWorking = false;
    private static long startWorkTime;
    private static long endWorkTime;

    private static final int FEATURE_DATAS_UNREADY = 1;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;
    private String userIdOfMaxScore = "";
    private float maxScore = 0;

    //声明dialog
    private CameraRegDialog mDialogUtils;

    //控制registerDialog的显示
    private boolean isSHow = true;

    //人脸属性
    private FaceAttribute mFaceAttribute;

    //判断是否退出Activity
    private boolean isQuit = false;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    private RectF rectF = new RectF();

    /**
     *人脸识别
     */
    public Feature asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos) {
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
                //TODO 活体判断成功之后，再绘画人脸框和人脸信息。
                feature = identity(imageFrame, faceInfos[0]);
            }
        }
//        attrCheck(faceInfos[0],imageFrame);
        return feature;
    }

    public float rgbLiveness(ImageFrame imageFrame, FaceInfo faceInfo) {

        final float rgbScore = FaceLiveness.getInstance().rgbLiveness(imageFrame.getArgb(), imageFrame
                .getWidth(), imageFrame.getHeight(), faceInfo.landmarks);
        return rgbScore;
    }

    @SuppressLint("StringFormatInvalid")
    public Feature identity(ImageFrame imageFrame, FaceInfo faceInfo) {
        float raw  = Math.abs(faceInfo.headPose[0]);
        float patch  = Math.abs(faceInfo.headPose[1]);
        float roll  = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
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
//        Feature feature = displayUserOfMaxScore(identifyRet.getUserId(), identifyRet.getScore());回调
        identityStatus = IDENTITY_IDLE;
        Feature feature = null;///
        return feature;
    }

    public void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(CameraRegActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public Paint paint = new Paint();

    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
    }


    Rect rect = new Rect();
    /**
     * 获取人脸框区域。
     *
     * @return 人脸框区域
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

    /**
     * 人脸注册：检测人脸
     */
    public void checkFace(int retCode, FaceInfo[] faceInfos, ImageFrame frame) {
        if ( retCode == FaceTracker.ErrCode.OK.ordinal() && faceInfos != null) {
            FaceInfo faceInfo = faceInfos[0];
            String tip = filter(faceInfo, frame);//***在此步进行人脸注册事务

        } else {
            String tip = checkFaceCode(retCode);

        }
    }

    /**
     * 过滤人脸
     */
    public String filter(FaceInfo faceInfo, ImageFrame imageFrame) {

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
        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
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
            //判断该人脸是否已经注册过了
            List<Feature> featureList = mDialogUtils.getFeatures(faceInfo ,imageFrame);
            if (featureList.size()<=0) {
//                isRegist = false;
                saveFace(faceInfo, imageFrame);
            }else{
                tip = getString(R.string.user_face_registered);
//                isRegist = false;
                toast(tip);
            }
        } else if (liveType ==  ConfigUtils.TYPE_RGB_LIVENSS) {
            if (rgbLiveness(imageFrame, faceInfo) > FaceEnvironment.LIVENESS_RGB_THRESHOLD) {
                //判断该人脸是否已经注册过了
                List<Feature> featureList = mDialogUtils.getFeatures(faceInfo ,imageFrame);
                if (featureList.size()<=0) {
//                    isRegist = false;
                    saveFace(faceInfo, imageFrame);
                }else{
                    tip = getString(R.string.user_face_registered);
//                    isRegist = false;
                    toast(tip);
                }
            }
        }
        return tip;
    }

    public String checkFaceCode(int errCode) {
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
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());//人脸注册对话框中要显示的人脸图片//在此处处理加工图像帧，得到人脸图
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


}

