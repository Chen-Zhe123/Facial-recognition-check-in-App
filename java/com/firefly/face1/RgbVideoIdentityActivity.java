/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.baidu.idl.facesdk.FaceAttribute;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;
import com.example.checkvendor.CheckVendor;
import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.adapter.PartRecordAdapter;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.Feature;
import com.firefly.face1.dialog.BeginWorkingDialog;
import com.firefly.face1.dialog.CameraRegDialog;
import com.firefly.face1.view.HorizontalListView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.baidu.aip.manager.FaceSDKManager.ACTIVATING_SUCCESS;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_ID_PHOTO;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_LIVE;
import static com.baidu.aip.utils.ConfigUtils.TYPE_MODEL;
import static java.lang.System.currentTimeMillis;

/**
 * 需要与另外两个识别模式进行代码整合。
 */

public class RgbVideoIdentityActivity extends Activity implements View.OnClickListener,
        CameraRegDialog.RegCallback, BeginWorkingDialog.BeginWorkingCallback {

    private static List<String> needSignIdList = new ArrayList<>();
    private static Map<String, Long> alreadySignMap = new HashMap<>();
    private static Map<String, Long> lateSign = new HashMap<>();
    private static List<String> neverSign = new ArrayList<>();

    private SharedPreferences pref;
    private static String DEFAULT_GROUP_FIELD;
    private static String DEFAULT_GROUP_NAME;
    private static Boolean isWorking = false;
    private static long startWorkTime;
    private static long endWorkTime;

    private static final int FEATURE_DATAS_UNREADY = 1;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;

    // 预览View;
    private PreviewView previewView;
    // textureView用于绘制人脸框等。
    private TextureView textureView;
    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;

    // 为了方便调式。
    private TextView detectDurationTv;
    private TextView rgbLivenssDurationTv;
    private TextView rgbLivenessScoreTv;
    private TextView tvAttr;

    //title布局
    private MyHandler mHandler;
    private TextView timeTv,dateTv,xingQiTv,versionCodeTv;
    private ImageView settingIv;
    private static String xingQis[] ;
    private boolean isTimeThread = false;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;
    private String userIdOfMaxScore = "";
    private float maxScore = 0;
    //root布局
    private HorizontalListView mScrollView;
    private PartRecordAdapter mAdapter;
    private List<Feature> recordLists = new ArrayList<>();
    //底部导航栏
    private LinearLayout bottomBar;
    private LinearLayout inWorkingStatement;
    private LinearLayout faceManagerButton;
    private LinearLayout parameterSettingsButton;
    private LinearLayout viewResultsButton;
    private LinearLayout historicalRecordButton;
    //人脸框
    private Bitmap face_detect_bg;
    //绘制人脸信息背景
    private Bitmap face_recog_bg;
    //人脸注册按钮（待移除)
    private LinearLayout addLayout;
    //开始签到按钮
    private Button beginWorkingButton;
    //结束签到按钮
    private Button endWorkButton;
    //声明dialog
    private CameraRegDialog registerDialog;
    private BeginWorkingDialog beginWorkingDialog;

    //控制registerDialog的显示
    private boolean isSHow = true;
    //判断是否开始注册
    private static boolean isRegistering = false;

//    private static Feature tempFeature = null;

    //人脸属性
    private FaceAttribute mFaceAttribute;

    //判断是否退出Activity
    private boolean isQuit = false;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_interface);
        DBMaster.getInstance().init(getApplicationContext());
        face_detect_bg = BitmapFactory.decodeResource(getResources(), R.drawable.face_detect_bg);
        face_recog_bg =  BitmapFactory.decodeResource(getResources(), R.drawable.recogition);
        registerDialog = new CameraRegDialog(this);
        registerDialog.setActivationCallback(this);
        beginWorkingDialog = new BeginWorkingDialog(this);
        beginWorkingDialog.setCallback(this);
        //获取默认组的域名和组名(域名的理解请参考数据库部分代码)
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        DEFAULT_GROUP_FIELD = pref.getString("field","group_0");
        //域名为"group_0"的“系统组”未储存在数据库中，而是在启动APP后自动生成
        if(DEFAULT_GROUP_FIELD.equals("group_0")){
            DEFAULT_GROUP_NAME = "系统组";
        }else {
            DEFAULT_GROUP_NAME = DBMaster.getInstance().mGroupInfoTable.queryGroupName(DEFAULT_GROUP_FIELD);
        }
        findView();
        init();
        addListener();
        PreferencesUtil.initPrefs(getApplicationContext());
        // 使用人脸1：n时使用
        loadFeature2Memery();//开子线程进行加载
        CheckVendor checkVendor = new CheckVendor();
        if (checkVendor.check()!=1) {
            FaceSDKManager.getInstance().init(getApplicationContext(),
                    new FaceSDKManager.SdkInitListener() {

                @Override
                public void initStart(FaceDetector mFaceDetector, FaceFeature mFaceFeature) {
                    //开发者可以在此配置人脸识别参数
                    //默认
                    mFaceDetector.init(RgbVideoIdentityActivity.this);
                    mFaceFeature.init(RgbVideoIdentityActivity.this);
                    //比如修改
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
                            Toast.makeText(RgbVideoIdentityActivity.this,
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
        previewView = findViewById(R.id.preview_view);
        textureView = findViewById(R.id.texture_view);
        detectDurationTv = findViewById(R.id.detect_duration_tv);
        rgbLivenssDurationTv = findViewById(R.id.rgb_liveness_duration_tv);
        rgbLivenessScoreTv = findViewById(R.id.rgb_liveness_score_tv);
        tvAttr = findViewById(R.id.tvAttr);
        rgbLivenessScoreTv.setText(getString(R.string.rgb_liveness_score,""));
        rgbLivenssDurationTv.setText(getString(R.string.rgb_liveness_duration, ""));
        detectDurationTv.setText(getString(R.string.rgb_detectduration,""));

        //title布局初始化
        timeTv = findViewById(R.id.timeTv);
        dateTv = findViewById(R.id.dateTv);
        xingQiTv = findViewById(R.id.xingqiTv);
        settingIv = findViewById(R.id.settingIv);
        versionCodeTv = findViewById(R.id.versionCodeTv);
        versionCodeTv.setText(getResources().getString(R.string.versionCode)+getVerName(this));
        xingQis = getResources().getStringArray(R.array.XingQis);
        mHandler = new MyHandler(this);
        //root控件
        mScrollView = findViewById(R.id.recordScrollView);
        //默认图标
        Feature firstFeature = new Feature();
        firstFeature.setUpdateTime(-1);
        firstFeature.setUserName(getString(R.string.user_name));
        recordLists.add(firstFeature);
        mAdapter = new PartRecordAdapter(recordLists);
        mScrollView.setAdapter(mAdapter);
        //添加用户按钮
        addLayout = findViewById(R.id.addUserLayout);
        addLayout.setOnClickListener(this);
        //开始签到按钮
        beginWorkingButton = findViewById(R.id.begin_working_button);
        beginWorkingButton.setOnClickListener(this);
        //结束签到按钮
        endWorkButton = findViewById(R.id.end_work_button);
        endWorkButton.setOnClickListener(this);
        //底部导航栏
        bottomBar = findViewById(R.id.main_interface_bottom_navigation_bar);
        inWorkingStatement = findViewById(R.id.in_working_statement);
        faceManagerButton = findViewById(R.id.FaceManagerButton);
        faceManagerButton.setOnClickListener(this);
        parameterSettingsButton = findViewById(R.id.ParameterSettingsButton);
        parameterSettingsButton.setOnClickListener(this);
        viewResultsButton = findViewById(R.id.ViewResultsButton);
        viewResultsButton.setOnClickListener(this);
        historicalRecordButton = findViewById(R.id.HistoricalRecordButton);
        historicalRecordButton.setOnClickListener(this);
    }

    private void init() {
        faceDetectManager = new FaceDetectManager(getApplicationContext());
        // 从系统相机获取图片帧。
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        cameraImageSource.getCameraControl().setPreferredPreviewSize(1280, 720);
        // cameraImageSource.getCameraControl().setPreferredPreviewSize(640, 480);

        // 设置最小人脸，该值越小，检测距离越远，该值越大，检测性能越好。范围为80-200
        FaceSDKManager.getInstance().getFaceDetector().setMinFaceSize(100);
        // FaceSDKManager.getInstance().getFaceDetector().setNumberOfThreads(4);
        // 设置预览
        cameraImageSource.setPreviewView(previewView);
        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);
        // 设置人脸过滤角度，角度越小，人脸越正，比对时分数越高
        faceDetectManager.getFaceFilter().setAngle(20);

        textureView.setOpaque(false);
        // 不需要屏幕自动变黑。
        textureView.setKeepScreenOn(true);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }

        setCameraType(cameraImageSource);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        // TODO 选择使用前置摄像头
        // cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);
        // TODO 选择使用usb摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
        // 如果不设置，人脸框会镜像，显示不准
        previewView.getTextureView().setScaleX(-1);
    }

    private RectF rectF = new RectF();
//    private boolean isStartReg = true;
//    private boolean isStartDetect = true;

    //在人脸框旁边绘制识别的人脸信息
    private Bitmap bitmap = null;
    private String userName = "";

    private void addListener() {
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(final int retCode, final FaceInfo[] infos, final ImageFrame frame) {//人脸探测器检测到人脸后回调此函数
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
                if (!isQuit) {
                    if (isRegistering) {//处于即将开始注册状态
                        checkFace(retCode, infos, frame);//检查探测到的人脸是否符合注册标准，从此处进入人脸注册的流程
                        showFrame(frame, infos, null, null);//展示空的信息
                    } else {//处于未注册状态，展示人脸探测结果
                        if (retCode == FaceTracker.ErrCode.OK.ordinal() && infos != null) {
                            Feature tempFeature =  asyncIdentity(frame, infos);//此处开始人脸匹配的流程
                            if (tempFeature != null) {
                                String user_id = tempFeature.getUserId();
                                File faceDir = FileUitls.getFaceDirectory();
                                if (faceDir != null && faceDir.exists() && !TextUtils.isEmpty(user_id)) {
                                    userName = tempFeature.getUserName();
                                    Bitmap temp = BitmapFactory.decodeFile(faceDir.getPath() + "/" + user_id);
                                    bitmap = Bitmap.createScaledBitmap(temp, (int) RgbVideoIdentityActivity.this.
                                                    getResources().getDimension(R.dimen.canvas_bitmap_w),
                                            (int) RgbVideoIdentityActivity.this.getResources().
                                                    getDimension(R.dimen.canvas_bitmap_h), true);
                                    temp.recycle();
                                    temp=null;
                                } else {
                                    if (bitmap!=null) {
                                        bitmap.recycle();
                                        bitmap = null;
                                    }
                                    userName = null;
                                }
                            } else {//未探测到人脸，头像与名字设空
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
    public void regCallback() {
        isSHow = true;
        isRegistering = false;
    }

    @Override
    public void callback(boolean success, Bitmap bitmap) {//由相机注册会话框回调，该回调的意义在于更改isRegistering的状态
        if (success){
            isRegistering = false;
            //TODO　人脸注册
            registerDialog.register(bitmap);
        }else {
            isSHow = true;
            isRegistering = false;
        }
    }

    @Override
    public void workingCallback() {//点击开始签到按钮后回调此函数//此处传入签到时间和签到组等参数
        bottomBar.setVisibility(View.GONE);
        inWorkingStatement.setVisibility(View.VISIBLE);
        beginWorkingButton.setVisibility(View.GONE);
        endWorkButton.setVisibility(View.VISIBLE);
        //调整系统状态
        isWorking = true;
//        DBMaster.getInstance().mResultTable.cleanAllEvent();//用于调试

    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addUserLayout:
                isRegistering = true;
                break;
            case R.id.begin_working_button:
                startWorkTime = System.currentTimeMillis();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beginWorkingDialog.show();
                    }
                });
                break;
            case R.id.end_work_button:
                endWorkTime = System.currentTimeMillis();
                bottomBar.setVisibility(View.VISIBLE);
                inWorkingStatement.setVisibility(View.GONE);
                beginWorkingButton.setVisibility(View.VISIBLE);
                endWorkButton.setVisibility(View.GONE);
                isWorking = false;
                //加载待签到组id列表
                needSignIdList = DBMaster.getInstance().mGroupTable.queryIdListByField(pref.getString("field","group_0"));
                if(needSignIdList != null){
                    StringBuilder needSignText = new StringBuilder();
                    StringBuilder alreadySignText = new StringBuilder();
                    StringBuilder neverSignText = new StringBuilder();
                    for(String id : needSignIdList){
                        needSignText.append(id).append(",");
                        if(!alreadySignMap.containsKey(id)){
                            neverSignText.append(id).append(",");
                        }
                    }
                    for (Map.Entry<String, Long> entry : alreadySignMap.entrySet()){
                        alreadySignText.append(entry.getKey()).append(",").append(entry.getValue()).append(".");
                    }
                    if(DBMaster.getInstance().mResultTable.addEvent(DEFAULT_GROUP_NAME,startWorkTime,needSignText.toString(),alreadySignText.toString(),null,neverSignText.toString())){
                        Toast.makeText(this,"本次签到记录已保存",Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(this,"签到记录保存失败！",Toast.LENGTH_SHORT).show();
                    }
                }
                //用于调试
                for(String id : needSignIdList){
                    Log.d("待签到人员id", id);
                }
                for (Map.Entry<String, Long> entry : alreadySignMap.entrySet()){
                    Log.d("已签到人员id和time", entry.getKey()+","+entry.getValue());
                }
                needSignIdList.clear();
                alreadySignMap.clear();
                break;
            case R.id.FaceManagerButton:
                isQuit = true;
                Intent intent1 = new Intent(RgbVideoIdentityActivity.this,
                        FaceManagerActivity.class);
                startActivity(intent1);
//                finish();
//                System.exit(0);
                break;
            case R.id.ParameterSettingsButton:
                isQuit = true;
                Intent intent2 = new Intent(RgbVideoIdentityActivity.this,
                        ParameterSettingsActivity.class);
                startActivity(intent2);
//                finish();
//                System.exit(0);
                break;
            case R.id.ViewResultsButton:
                isQuit = true;
                Intent intent3 = new Intent(RgbVideoIdentityActivity.this,
                        ViewResultsActivity.class);
                startActivity(intent3);
//                finish();
//                System.exit(0);
                break;
            case R.id.HistoricalRecordButton:
                isQuit = true;
                Intent intent4 = new Intent(RgbVideoIdentityActivity.this,
                        HistoricalRecordActivity.class);
                startActivity(intent4);
//                finish();
//                System.exit(0);
                break;
            default:
                break;
        }
    }

    /**
     * 加载人脸库中所有人脸
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
     *人脸识别
     */
    private Feature asyncIdentity(final ImageFrame imageFrame, final FaceInfo[] faceInfos) {//此步进行人脸匹配的预处理，分类处理//猜测此处还未确定图片帧中是否包含(活体)人脸
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

    private float rgbLiveness(ImageFrame imageFrame, FaceInfo faceInfo) {

        long startTime = System.currentTimeMillis();
        final float rgbScore = FaceLiveness.getInstance().rgbLiveness(imageFrame.getArgb(), imageFrame
                .getWidth(), imageFrame.getHeight(), faceInfo.landmarks);
        final long duration = System.currentTimeMillis() - startTime;

        runOnUiThread(new Runnable() {
            @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
            @Override
            public void run() {
                rgbLivenssDurationTv.setVisibility(View.VISIBLE);
                rgbLivenessScoreTv.setVisibility(View.VISIBLE);
                rgbLivenssDurationTv.setText(getString(R.string.rgb_liveness_duration, duration));
                rgbLivenessScoreTv.setText(getString(R.string.rgb_liveness_score,rgbScore));
            }
        });
        return rgbScore;
    }

    @SuppressLint("StringFormatInvalid")
    private Feature identity(ImageFrame imageFrame, FaceInfo faceInfo) {
        float raw  = Math.abs(faceInfo.headPose[0]);
        float patch  = Math.abs(faceInfo.headPose[1]);
        float roll  = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 ||  roll > 20) {
            return null;
        }
        identityStatus = IDENTITYING;
        long startTime = System.currentTimeMillis();
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
        Feature feature = showAndRecordToResult(identifyRet.getUserId(), identifyRet.getScore());//此处得到人脸匹配结果，并显示部分已打卡成员（需要与下面代码整合）
        recordAllUsers(identifyRet.getUserId(), identifyRet.getScore(),imageFrame,faceInfo);//将捕捉到的人脸存入历史记录（待改）
//        recordGroupUsers(identifyRet.getUserId(), identifyRet.getScore(),imageFrame,faceInfo);//判断是否开始签到，并对当前组成员打卡情况进行记录
        identityStatus = IDENTITY_IDLE;
        if (!isRegistering)
            displayTip(getString(R.string.rgb_detectduration,
                    (System.currentTimeMillis() - startTime)+""));
        return feature;
    }

    private Feature showAndRecordToResult(final String userId, final float score) {
        List<String> defaultGroupIdList = DBMaster.getInstance().mGroupTable.queryIdListByField(DEFAULT_GROUP_FIELD);
        if (score < 80) {
            return null;
        }
        if (userIdOfMaxScore.equals(userId) ) {//代码是否冗余？
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
                File file = new File(faceDir, featureList.get(0).getUserId());
                if (defaultGroupIdList.contains(userId) && isWorking && file.exists()) {//增加了是否属于默认组人脸及是否处于签到状态的判断
                    if(!alreadySignMap.containsKey(userId)) {
                        alreadySignMap.put(userId, System.currentTimeMillis());
                    }
                    Message msg = mHandler.obtainMessage();
                    msg.getData().putSerializable("data", featureList.get(0));
                    msg.what =2;
                    mHandler.sendMessage(msg);
                }
                return feature;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    private void recordAllUsers(final String userId, final float score,ImageFrame imageFrame,FaceInfo faceInfo){
        if (score < 80) {
            //直接保存当前捕捉到的图片
            Matrix matrix = new Matrix();
            matrix.postScale(-1, 1);
            Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),matrix , true);

        }
        else {
            long captureTime = System.currentTimeMillis();
            DBMaster.getInstance().mRecordTable.addRecord(userId,captureTime);
        }
    }

    private void recordGroupUsers(final String userId, final float score,ImageFrame imageFrame,FaceInfo faceInfo){

    }

    /**
     * 进行属性检测
     */
    private void attrCheck(FaceInfo faceInfo, ImageFrame imageFrame) {
        // todo 人脸属性数据获取
        mFaceAttribute = FaceSDK.faceAttribute(imageFrame.getArgb(), imageFrame.getWidth(),
                imageFrame.getHeight(), FaceSDK.ImgType.ARGB,
                faceInfo.landmarks);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAttr.setText("人脸属性：" + getMsg(mFaceAttribute));
            }
        });
    }

    public String getMsg(FaceAttribute attribute) {
        StringBuilder msg = new StringBuilder();
        if (attribute != null) {
            msg.append("年龄：").append((int) attribute.age).append(",")
                    .append(attribute.race == 0 ? "黄种人" : attribute.race == 1 ? "白种人" :
                            attribute.race == 2 ? "黑人" : attribute.race == 3 ? "印度人" : "地球人").append(",")
                    .append(attribute.expression == 0 ? "正常" : attribute.expression == 1 ? "微笑" : "大笑").append(",")
                    .append(attribute.gender == 0 ? "女" : attribute.gender == 1 ? "男" : "婴儿").append(",")
                    .append(attribute.glasses == 0 ? "不戴眼镜" : attribute.glasses == 1 ? "普通透明眼镜" : "太阳镜");
        }
        return msg.toString();
    }



    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RgbVideoIdentityActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayTip(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detectDurationTv!=null){
                    detectDurationTv.setVisibility(View.VISIBLE);
                    detectDurationTv.setText(text);
                }
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
     * 绘制人脸框。
     */
    @SuppressLint("ResourceAsColor")
    private void showFrame(ImageFrame frame, FaceInfo[] faceInfos, Bitmap pBitmap, String pUserName) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            return;
        }
        if (faceInfos == null || faceInfos.length == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        rectF.set(getFaceRect(faceInfos[0], frame));
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        FaceInfo faceInfo = faceInfos[0];
        // 检测图片的坐标和显示的坐标不一样，需要转换。
        previewView.mapFromOriginalRect(rectF);

        float yaw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        if (yaw > 20 || patch > 20 || roll > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);
            String text = getString(R.string.Please_face_screen);
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            // 绘制框
            canvas.drawBitmap(face_detect_bg,null,rectF, null);
            textureView.unlockCanvasAndPost(canvas);
        } else {
                // 符合检测要求，绘制绿框
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                // 绘制人脸识别信息
            if (!isRegistering) {//****!isRegist条件的作用是什么***
                    canvas.drawBitmap(face_recog_bg, rectF.right, rectF.top - 57, null);
                if (pBitmap != null) {
                    canvas.drawBitmap(pBitmap, rectF.right + 82, rectF.top - 58, null);
                }
                String text = getString(R.string.user_no_register);
                if (pUserName!=null) {
                    text = pUserName;
                }
                //绘制人脸对应用户名
                float x = rectF.right + 82 + 70;
                paint.reset();
                paint.setColor(R.color.color_f0f0f0);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(18);
                canvas.drawText(text, x, rectF.top - 20, paint);
            }
            // 绘制框
            canvas.drawBitmap(face_detect_bg,null,rectF, null);
            textureView.unlockCanvasAndPost(canvas);
        }
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



    class TimeThread extends Thread {
        @Override
        public void run() {
            while (isTimeThread){
                try {
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    msg.what = 1;  //消息
                    long sysTime = currentTimeMillis();//获取系统时间
                    CharSequence sysTimeStr = DateFormat.format("hh:mm:ss", sysTime);//时间显示格式
                    data.putCharSequence("sysTimeStr",sysTimeStr);
                    CharSequence sysDateStr = DateFormat.format("yyyy/MM/dd", sysTime);
                    data.putCharSequence("sysDateStr",sysDateStr);
                    final Calendar c = Calendar.getInstance();
                    String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
                    if("1".equals(mWay)){
                        mWay = xingQis[0];
                    }else if("2".equals(mWay)){
                        mWay = xingQis[1];
                    }else if("3".equals(mWay)){
                        mWay = xingQis[2];
                    }else if("4".equals(mWay)){
                        mWay = xingQis[3];
                    }else if("5".equals(mWay)){
                        mWay = xingQis[4];
                    }else if("6".equals(mWay)){
                        mWay = xingQis[5];
                    }else if("7".equals(mWay)){
                        mWay = xingQis[6];
                    }
                    msg.setData(data);
                    data.putCharSequence("mWay",mWay);
                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //在主线程里面处理消息并更新UI界面
    private static class MyHandler extends Handler {
        private WeakReference<RgbVideoIdentityActivity> mWeakReference;

        private MyHandler(RgbVideoIdentityActivity pWeakReference) {
            mWeakReference = new WeakReference<>(pWeakReference);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Bundle data = msg.getData();
                    mWeakReference.get().timeTv.setText(data.getCharSequence("sysTimeStr","")); //更新时间
                    mWeakReference.get().dateTv.setText(data.getCharSequence("sysDateStr",""));
                    mWeakReference.get().xingQiTv.setText(data.getCharSequence("mWay",""));
                    break;
                case 2:
                    Feature temp = (Feature) msg.getData().getSerializable("data");
                    if (temp==null)
                        return;
                    Feature feature = new Feature();
                    feature.setRegTime(temp.getRegTime());
                    feature.setUpdateTime(temp.getUpdateTime());
                    feature.setUserId(temp.getUserId());
                    feature.setUserName(temp.getUserName());
                    if (mWeakReference.get().recordLists.size()>0) {
                        if (mWeakReference.get().recordLists.get(0).getUpdateTime()==-1){
                            mWeakReference.get().recordLists.remove(0);
                        }
                        if (!mWeakReference.get().recordLists.contains(feature)) {//避免重复记录
                            //打卡时间
                            feature.setUpdateTime(System.currentTimeMillis());
                            mWeakReference.get().recordLists.add(0, feature);
                            mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
                        }else{
                            if (mWeakReference.get().recordLists.indexOf(feature)>=6) {
                                //将已经打卡的人脸移到第一个位置
                                int pos = mWeakReference.get().recordLists.indexOf(feature);
                                mWeakReference.get().recordLists.add(0, mWeakReference.get().recordLists.get(pos));
                                mWeakReference.get().recordLists.remove(pos + 1);
                                mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
                            }
                        }
                    }else{
                        //打卡时间
                        feature.setUpdateTime(System.currentTimeMillis());
                        mWeakReference.get().recordLists.add(feature);
                        mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
                    }
                    break;
                case 3:

                    break;
                    default:
                    break;
            }
        }
    }

    /**
     * 获取版本号名称
     *
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    /**
     * 人脸注册：检测人脸
     */
    private void checkFace(int retCode, FaceInfo[] faceInfos, ImageFrame frame) {
        if ( retCode == FaceTracker.ErrCode.OK.ordinal() && faceInfos != null) {
            FaceInfo faceInfo = faceInfos[0];
            String tip = filter(faceInfo, frame);
            displayTip(tip);
        } else {
            String tip = checkFaceCode(retCode);
            displayTip(tip);
        }
    }

    /**
     * 过滤人脸
     */
    private String filter(FaceInfo faceInfo, ImageFrame imageFrame) {//整合该代码时注意回调更改isRegistering的状态
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
            List<Feature> featureList = registerDialog.getFeatures(faceInfo ,imageFrame);
            if (featureList.size()<=0) {
                isRegistering = false;
                saveFace(faceInfo, imageFrame);
            }else{
                tip = getString(R.string.user_face_registered);
                isRegistering = false;
                toast(tip);
            }
        } else if (liveType ==  ConfigUtils.TYPE_RGB_LIVENSS) {
            if (rgbLiveness(imageFrame, faceInfo) > FaceEnvironment.LIVENESS_RGB_THRESHOLD) {
                //判断该人脸是否已经注册过了
                List<Feature> featureList = registerDialog.getFeatures(faceInfo ,imageFrame);
                if (featureList.size()<=0) {
                    isRegistering = false;
                    saveFace(faceInfo, imageFrame);
                }else{
                    tip = getString(R.string.user_face_registered);
                    isRegistering = false;
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
        final Bitmap bitmap = FaceCropper.getFace(imageFrame.getArgb(), faceInfo, imageFrame.getWidth());//人脸注册对话框中要显示的人脸图片//在此处处理加工图像帧，得到人脸图
        if (isSHow){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    registerDialog.show(bitmap,true);
                }
            });
            isSHow = false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        DEFAULT_GROUP_FIELD = pref.getString("field","group_0");
        if(DEFAULT_GROUP_FIELD.equals("group_0")){
            DEFAULT_GROUP_NAME = "系统组";
        }else {
            DEFAULT_GROUP_NAME = DBMaster.getInstance().mGroupInfoTable.queryGroupName(DEFAULT_GROUP_FIELD);
        }
        faceDetectManager.start();
        isQuit = false;
//        isStartReg = true;
//        isStartDetect = true;
        isTimeThread = true;
        new TimeThread().start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isQuit = true;
//        isStartReg = false;
//        isStartDetect = false;
        isTimeThread = false;
        // 结束检测。
        faceDetectManager.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 结束检测。
        faceDetectManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetectManager.stop();
    }

}
