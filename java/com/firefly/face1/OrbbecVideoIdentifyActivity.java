/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.callback.ILivenessCallBack;
import com.baidu.aip.entity.IdentifyRet;
import com.baidu.aip.entity.LivenessModel;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceFeature;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceAttribute;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.adapter.PartRecordAdapter;
import com.firefly.face1.api.FaceApi;
import com.firefly.face1.bean.Feature;
import com.firefly.face1.dialog.CameraRegDialog;
import com.firefly.face1.view.HorizontalListView;
import com.orbbec.Native.DepthUtils;
import com.orbbec.view.OpenGLView;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static com.baidu.aip.manager.FaceSDKManager.ACTIVATING_SUCCESS;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_ID_PHOTO;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_LIVE;
import static com.baidu.aip.utils.ConfigUtils.TYPE_MODEL;
import static java.lang.System.currentTimeMillis;

public class OrbbecVideoIdentifyActivity extends Activity implements OpenNIHelper.DeviceOpenListener,
        ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener, CameraRegDialog.RegCallback {
    private static final int MSG_WHAT = 5;
    private static final String MSG_KEY = "YUV";
    private static final int FEATURE_DATAS_UNREADY = 1;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private static final String TAG = "OrbbecVideoIdentify";

    private Activity mContext;

    //title布局
    private MyHandler mHandler;
    private TextView timeTv,dateTv,xingQiTv,versionCodeTv;
    private ImageView settingIv;
    private  static String xingQis[] ;
    //日期线程是否退出
    private boolean isTimeThread = false;

    private TextView detectDurationTv;
    private TextView rgbLivenssDurationTv;
    private TextView rgbLivenessScoreTv;
    private TextView depthLivenessScoreTv;
    private TextView depthLivenssDurationTv;
    private TextView tvAttr;

    private OpenGLView mDepthGLView;

    private Device device;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream depthStream;
    private VideoStream rgbStream;

    //摄像头默认预览大小
    private int mWidth = com.orbbec.utils.GlobalDef.RESOLUTION_X;
    private int mHeight = com.orbbec.utils.GlobalDef.RESOLUTION_Y;
    private static final int DEPTH_NEED_PERMISSION = 33;
    //depth摄像头线程锁
    private final Object sync = new Object();

    private boolean exit = false;

    private volatile int identityStatus = FEATURE_DATAS_UNREADY;
    //
    private String userIdOfMaxScore = "";
    private float maxScore = 0;

    //root布局
    private HorizontalListView mScrollView;
    private PartRecordAdapter mAdapter;
    private List<Feature>recordLists = new ArrayList<>();

    //uvcTest
    private final Object mSync = new Object();
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;

    private Handler handler = new Handler();

    //绘制人脸框画布
    private TextureView canvasTextureView;
    //绘制人脸框的人脸信息
    private static Feature tempFeature = null;
    //人脸框
    private Bitmap face_detect_bg;
    //绘制人脸信息背景
    private Bitmap face_recog_bg;

    private LinearLayout addUserLayout;
    //判断是否开始注册
    private static boolean isRegist = false;

    //判断是否退出Activity
    private boolean isQuit = false;

    //注册dialog
    private CameraRegDialog mDialogUtils;
    //控制RegDialogUtils的显示
    private boolean isSHow = true;
    private Matrix matrix = new Matrix();

    //人脸属性
    private FaceAttribute mFaceAttribute;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orbbec_video_identity);
        matrix.postScale(-1, 1);   //镜像水平翻转
        findView();
        initUvc();

        mContext = this;
        // 使用人脸1：n时使用
//        DBManager.getInstance().init(getApplicationContext());
        DBMaster.getInstance().init(getApplicationContext());
        FaceSDKManager.getInstance().init(getApplicationContext(),new FaceSDKManager.SdkInitListener() {

            @Override
            public void initStart(FaceDetector mFaceDetector, FaceFeature mFaceFeature) {
                //开发者可以在此配置人脸识别参数
                //默认
                mFaceDetector.init(OrbbecVideoIdentifyActivity.this);
                mFaceFeature.init(OrbbecVideoIdentifyActivity.this);
                //比如修改
                //mFaceDetector.init(OrbbecVideoIdentifyActivity.this, FaceEnvironment);
                //mFaceFeature.init(OrbbecVideoIdentifyActivity.this, FaceSDK.RecognizeType.RECOGNIZE_LIVE);
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
                            Toast.makeText(OrbbecVideoIdentifyActivity.this,
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
        //加载人脸库中的所有人脸
        loadFeature2Memery();

    }

    private void initUvc() {
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);//创建
        //将rgb摄像头画面左右翻转
        mTextureView.setRotationY(180);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture = surface;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                mSurfaceTexture = null;
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart:");
        //注意此处的注册和反注册  注册后会有相机usb设备的回调
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isTimeThread = true;
        isQuit = false;
        //开始时间线程，操作步系统时间
        new TimeThread().start(); //启动新的线程
    }

    /**
     * uvcCamera连接监听
     */
    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            //申请usb权限
            if (device.getDeviceClass() == 239 && device.getDeviceSubclass() == 2) {
                mUSBMonitor.requestPermission(device);
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        final UVCCamera camera = new UVCCamera();
                        camera.open(ctrlBlock);
                        if (mSurfaceTexture != null) {
                            camera.setPreviewTexture(mSurfaceTexture);
                            previewSize = camera.getPreviewSize();
                            camera.setPreviewSize(mWidth, mHeight);
                            camera.setFrameCallback(iFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);//设置回调 和回调数据类型
                            camera.startPreview();
                        }
                        synchronized (mSync) {
                            mUVCCamera = camera;
                        }
                    }
                }
            });
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Log.v(TAG, "onDisconnect:");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.close();
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Log.v(TAG, "onDettach:");
            Toast.makeText(OrbbecVideoIdentifyActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onCancel(final UsbDevice device) {

        }
    };

    //uvcCamera摄像头预览回调
    IFrameCallback iFrameCallback = new IFrameCallback() {
        byte[] yuv = new byte[mWidth*mHeight*3/2];
        @Override
        public void onFrame(final ByteBuffer byteBuffer) {
            final int len = byteBuffer.capacity();
            if (len > 0) {
                byteBuffer.get(yuv);
                if (mHandler != null) {
                    mHandler.removeMessages(MSG_WHAT);
                    Message message = mHandler.obtainMessage();
                    message.getData().putByteArray(MSG_KEY, yuv);
                    message.what = MSG_WHAT;
                    mHandler.sendMessage(message);
                }
            }
            byteBuffer.clear();
        }
    };

    /**
     * 将byte[]转为bitmap
     * @param data
     * @param rgba
     * @param width
     * @param height
     * @return
     */
    public Bitmap cameraByte2Bitmap(byte[] data,int[] rgba, int width, int height) {
        //将yuv数据转为rgb
        DepthUtils.Bytes2Ints(data, rgba, width, height);
        //将rgb转为bitmap
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }

    public Bitmap cameraByte2Bitmap(byte[] data, int width, int height) {
        //将yuv数据转为rgb
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        //将rgb转为bitmap
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }

    /**
     * 摄像头预览大小
     */
    Size previewSize;


    @SuppressLint("StringFormatInvalid")
    private void findView() {
        mDepthGLView =  findViewById(R.id.depthGlView);
        mTextureView =  findViewById(R.id.camera_surface_view);
        detectDurationTv =  findViewById(R.id.detect_duration_tv);
        rgbLivenssDurationTv =  findViewById(R.id.rgb_liveness_duration_tv);
        rgbLivenessScoreTv =  findViewById(R.id.rgb_liveness_score_tv);
        depthLivenssDurationTv =  findViewById(R.id.depth_liveness_duration_tv);
        depthLivenessScoreTv = findViewById(R.id.depth_liveness_score_tv);
        detectDurationTv.setText(getString(R.string.rgb_detectduration,""));
        rgbLivenessScoreTv.setText(getResources().getString(R.string.rgb_liveness_score,""));
        rgbLivenssDurationTv.setText(getResources().getString(R.string.rgb_liveness_duration,""));
        depthLivenessScoreTv.setText(getResources().getString(R.string.depth_liveness_score,""));
        depthLivenssDurationTv.setText(getResources().getString(R.string.depth_liveness_duration, ""));
        tvAttr = findViewById(R.id.tvAttr);

        mScrollView = findViewById(R.id.recordScrollView);

        Feature firstFeature = new Feature();
        firstFeature.setUpdateTime(-1);
        firstFeature.setUserName(getResources().getString(R.string.user_name));
        recordLists.add(firstFeature);
        mAdapter = new PartRecordAdapter(recordLists);
        mScrollView.setAdapter(mAdapter);
        timeTv =  findViewById(R.id.timeTv);
        dateTv =  findViewById(R.id.dateTv);
        xingQiTv =  findViewById(R.id.xingqiTv);
        settingIv =  findViewById(R.id.settingIv);
        versionCodeTv =  findViewById(R.id.versionCodeTv);
        versionCodeTv.setText(getResources().getString(R.string.versionCode)+getVerName(this));
        xingQis = getResources().getStringArray(R.array.XingQis);
        mHandler = new MyHandler(this);

        //添加用户按钮
        addUserLayout = findViewById(R.id.addUserLayout);
        addUserLayout.setOnClickListener(this);
        mDialogUtils = new CameraRegDialog(this);
        mDialogUtils.setActivationCallback(this);

        face_detect_bg = BitmapFactory.decodeResource(getResources(), R.drawable.face_detect_bg);
        face_recog_bg =  BitmapFactory.decodeResource(getResources(), R.drawable.recogition);

        //绘制人脸框画布
        canvasTextureView = findViewById(R.id.texture0);
        canvasTextureView.setOpaque(false);
        canvasTextureView.setKeepScreenOn(true);

        //Depth摄像头打开工具类
        mOpenNIHelper = new OpenNIHelper(this);
        mOpenNIHelper.requestDeviceOpen(this);
    }


    /**
     * 用奥比sdk打开Depth摄像头
     * 初始化Depth摄像头
     */
    private void init(UsbDevice device) {
        OpenNI.setLogAndroidOutput(false);
        OpenNI.setLogMinSeverity(0);
        OpenNI.initialize();

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            Toast.makeText(this, " openni enumerateDevices 0 devices", Toast.LENGTH_LONG).show();
            return;
        }

        this.device = null;
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.device = Device.open();
                break;
            }
        }

        if (this.device == null) {
            Toast.makeText(this, " openni open devices failed: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isTimeThread = false;
        isQuit = true;
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop:");
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
        }
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        super.onStop();
        exit = true;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
            if (depthStream != null) {
                depthStream.stop();
            }
            if (rgbStream != null) {
                rgbStream.stop();
            }

            if (device != null) {
                device.close();
            }

        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
        }
        finish();
        System.exit(0);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy:");
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera.close();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        super.onDestroy();

    }

    private int draw_bitmap_w = 0;
    private int draw_bitmap_h = 0;

    /**
     * Depth摄像头打开回调
     * 绘制人俩框操作在这个方法
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDeviceOpened(UsbDevice device) {
        init(device);
        depthStream = VideoStream.create(this.device, SensorType.DEPTH);
        List<VideoMode> mVideoModes = depthStream.getSensorInfo().getSupportedVideoModes();
        draw_bitmap_w = (int) OrbbecVideoIdentifyActivity.this.getResources().getDimension(R.dimen.canvas_bitmap_w);
        draw_bitmap_h= (int) OrbbecVideoIdentifyActivity.this.getResources().getDimension(R.dimen.canvas_bitmap_h);
        for (VideoMode mode : mVideoModes) {
            int X = mode.getResolutionX();
            int Y = mode.getResolutionY();

            if (X == mWidth && Y == mHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                depthStream.setVideoMode(mode);
            }

        }
        //开始depth摄像头预览
        startThread();

        //人脸识别结果
        FaceLiveness.getInstance().setLivenessCallBack(new ILivenessCallBack() {
            @Override
            public void onCallback(final LivenessModel livenessModel) {
                if (!isQuit)
                    checkResult(livenessModel);
            }
            Bitmap detectbitmap;
            Bitmap drawbitmap;
            @Override
            public void onCallback(ImageFrame pFrame, FaceInfo infos) {
                try {
                    if (tempFeature != null) {
                        String user_id = tempFeature.getUserId();
                        String name = tempFeature.getUserName();
                        tempFeature= null;
                        File faceDir = FileUitls.getFaceDirectory();
                        if (faceDir != null && faceDir.exists()&&!TextUtils.isEmpty(user_id)) {
                            detectbitmap = BitmapFactory.decodeFile(faceDir.getPath() + "/" + user_id);
                            drawbitmap = Bitmap.createScaledBitmap(detectbitmap,
                                    draw_bitmap_w, draw_bitmap_h, true);
                            showFrame(pFrame,infos,drawbitmap,name);
                        } else {
                            showFrame(pFrame,infos,null,null);
                        }
                    } else {
                        showFrame(pFrame,infos,null,null);
                    }
                } catch (Resources.NotFoundException pE) {
                    pE.printStackTrace();
                }finally {
                    if (drawbitmap!=null) {
                        drawbitmap.recycle();
                        drawbitmap = null;
                    }
                    if (detectbitmap!=null) {
                        detectbitmap.recycle();
                        detectbitmap = null;
                    }
                }

            }

            @Override
            public void onTip(int code, final String msg) {

            }
        });
    }

    /**
     * 绘制人脸框和人脸信息画笔
     */
    private Paint paint = new Paint();
    private RectF rectF = new RectF();
    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
    }

    /**
     * 绘制人脸框。
     *
     */
    @SuppressLint("ResourceAsColor")
    private void showFrame(ImageFrame imageFrame, FaceInfo faceInfo, Bitmap pBitmap,String name) {

        Canvas canvas = canvasTextureView.lockCanvas();
        if (canvas == null) {
            return;
        }
        if (faceInfo == null ) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvasTextureView.unlockCanvasAndPost(canvas);
            return;
        }
        rectF.set(getFaceRect(faceInfo, imageFrame));

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        float yaw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        if (yaw > 20 || patch > 20 || roll > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);
            String text = getResources().getString(R.string.Please_face_screen);
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            // 绘制框
            canvas.drawBitmap(face_detect_bg,null,rectF, null);
            canvasTextureView.unlockCanvasAndPost(canvas);
        } else {

            // 符合检测要求，绘制绿框
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            // 绘制人脸识别信息
            if (!isRegist) {
                canvas.drawBitmap(face_recog_bg, rectF.right, rectF.top - 57, null);
                if (pBitmap != null) {
                    canvas.drawBitmap(pBitmap, rectF.right + 82, rectF.top - 58, null);
                }
                String text = getResources().getString(R.string.user_no_register);
                if (name!=null) {
                    text = name;
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
            canvas.drawBitmap(face_detect_bg, null, rectF, null);
            canvasTextureView.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 获取人脸框区域。
     *
     * @return 人脸框区域
     */
    // TODO padding?
    public Rect getFaceRect(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);
        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];
        int previewWidth = mTextureView.getWidth();
        int previewHeight = mTextureView.getHeight();
        float scaleW = 1.0f * previewWidth / frame.getWidth();
        float scaleH = 1.0f * previewHeight / frame.getHeight();
        int width = (right - left);
        int height = (bottom - top);
        left = (int) ((faceInfo.mCenter_x - width / 2) * scaleW);
        top = (int) ((faceInfo.mCenter_y - height / 2) * scaleH);
        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width);
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);
        return rect;
    }


    /**
     * 进行属性检测
     * 目前还不稳定
     */
    private void attrCheck(FaceInfo faceInfo, ImageFrame imageFrame) {
        // todo 人脸属性数据获取
        mFaceAttribute = FaceSDK.faceAttribute(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight(), FaceSDK.ImgType.ARGB,
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
            msg.append((int) attribute.age).append(",")
                    .append(attribute.race == 0 ? "黄种人" : attribute.race == 1 ? "白种人" :
                            attribute.race == 2 ? "黑人" : attribute.race == 3 ? "印度人" : "地球人").append(",")
                    .append(attribute.expression == 0 ? "正常" : attribute.expression == 1 ? "微笑" : "大笑").append(",")
                    .append(attribute.gender == 0 ? "女" : attribute.gender == 1 ? "男" : "婴儿").append(",")
                    .append(attribute.glasses == 0 ? "不戴眼镜" : attribute.glasses == 1 ? "普通透明眼镜" : "太阳镜");
        }
        return msg.toString();
    }


    /**
     * 打开depth摄像头失败回调
     * @param msg
     */
    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    /**
     * depth摄像头预览数据处理
     */
    void startThread() {
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

                streams.add(depthStream);

                depthStream.start();

                while (!exit) {

                    try {
                        OpenNI.waitForAnyStream(streams, 2000);

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {
                        VideoFrameRef frame = depthStream.readFrame();
                        mDepthGLView.update(frame, com.orbbec.utils.GlobalDef.TYPE_DEPTH);

                        ByteBuffer depthByteBuf = depthStream.readFrame().getData();
                        int depthLen = depthByteBuf.remaining();

                        byte[] depthByte = new byte[depthLen];

                        depthByteBuf.get(depthByte);
                        //设置人脸识别depth数据
                        FaceLiveness.getInstance().setDepthData(depthByte);
                        frame.release();
                        depthByteBuf.clear();
                    }
                }
            }
        };

        thread.start();
    }

    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查人脸检测返回的结果
     */
    private void checkResult(final LivenessModel model) {
        if (model == null) {
            return;
        }
        displayResult(model);
        if (isRegist){//人脸注册
                //注册
            List<Feature> featureList = mDialogUtils.getFeatures(model.getFaceInfo(),
                    model.getImageFrame());
            if (featureList.size()<=0) {
                isRegist = false;
                if (isSHow){
                    final Bitmap bitmap = FaceCropper.getFace(model.getImageFrame().getArgb(),
                            model.getFaceInfo(), model.getImageFrame().getWidth());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialogUtils.show(bitmap,false);
                        }
                    });
                    isSHow = false;
                }
            }else{
                isRegist = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OrbbecVideoIdentifyActivity.this,
                                R.string.user_face_registered, Toast.LENGTH_SHORT).show();
                    }
                });
                isSHow = false;
            }

        }else {//人脸识别
            int type = model.getLiveType();
            boolean livenessRgb = false;
            boolean livenessDepth = false;
            // 同一时刻都通过才认为活体通过，开发者也可以根据自己的需求修改策略
            if ((type & FaceLiveness.MASK_RGB) == FaceLiveness.MASK_RGB) {
                livenessRgb = (model.getRgbLivenessScore() > FaceEnvironment.LIVENESS_RGB_THRESHOLD);
            }
            if ((type & FaceLiveness.MASK_DEPTH) == FaceLiveness.MASK_DEPTH) {
                livenessDepth = (model.getDepthLivenessScore() > FaceEnvironment.LIVENESS_DEPTH_THRESHOLD);
            }
            if (livenessRgb&&livenessDepth) {
//                asyncIdentity(model.getImageFrame(), model.getFaceInfo());
                identity(model.getImageFrame(), model.getFaceInfo());
            }
        }
    }

    /**
     * 显示活体得分和活体耗时
     */
    private void displayResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
            @Override
            public void run() {
                int type = livenessModel.getLiveType();
                detectDurationTv.setText(getString(R.string.rgb_detectduration,livenessModel.getRgbDetectDuration()));
                if ((type & FaceLiveness.MASK_RGB) == FaceLiveness.MASK_RGB) {
                    rgbLivenessScoreTv.setText(getResources().getString(R.string.rgb_liveness_score,livenessModel.getRgbLivenessScore()));
                    rgbLivenssDurationTv.setText(getResources().getString(R.string.rgb_liveness_duration,livenessModel.getRgbLivenessDuration()));
                }

                if ((type & FaceLiveness.MASK_DEPTH) == FaceLiveness.MASK_DEPTH) {
                    depthLivenessScoreTv.setText(getResources().getString(R.string.depth_liveness_score, livenessModel.getDepthLivenessScore()));
                    depthLivenssDurationTv.setText(getResources().getString(R.string.depth_liveness_duration, livenessModel.getDetphtLivenessDuration()));
                }
            }

        });
    }


    /**
     * 加载数据库
     */
    private void loadFeature2Memery() {
        if (identityStatus != FEATURE_DATAS_UNREADY) {
            return;
        }
        es.submit(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                // android.os.Process.setThreadPriority (-4);
                FaceApi.getInstance().loadFaceFromLibrary();
                identityStatus = IDENTITY_IDLE;
            }
        });
    }

    /**
     * 已被移除
     * 人脸识别
     */
    private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo faceInfo) {
        if (identityStatus != IDENTITY_IDLE) {
            return;
        }
        if (identityStatus != IDENTITY_IDLE) {
            return;
        }

        es.submit(new Runnable() {
            @Override
            public void run() {
                identity(imageFrame, faceInfo);
            }
        });
    }

    /**
     * 人脸识别
     */
    private void identity(ImageFrame imageFrame, FaceInfo faceInfo) {
        if (imageFrame == null || faceInfo == null) {
            return;
        }
        float raw  = Math.abs(faceInfo.headPose[0]);
        float patch  = Math.abs(faceInfo.headPose[1]);
        float roll  = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 ||  roll > 20) {
            return;
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
        displayUserOfMaxScore(identifyRet.getUserId(), identifyRet.getScore());
        identityStatus = IDENTITY_IDLE;
    }

    /**
     * 判断活体得分是否大于0.8,如大于0.8,则显示识别人脸对应的图片
     */
    private void displayUserOfMaxScore(final String userId, final float score) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (score < 80) {
                    return;
                }

                if (!TextUtils.isEmpty(userIdOfMaxScore) && userIdOfMaxScore.equals(userId)) {
                    if (score >= maxScore) {
                        maxScore = score;
                    }
                } else {
                    userIdOfMaxScore = userId;
                    maxScore = score;
                }
                final List<Feature> featureList = FaceApi.getInstance().getFeatures(userId);
                if (featureList != null && featureList.size() > 0) {
                    File faceDir = FileUitls.getFaceDirectory();
                    if (faceDir != null && faceDir.exists()) {
                        File file = new File(faceDir, featureList.get(0).getUserId());
                        if (file.exists()) {
                            mHandler.removeMessages(2, null);
                            Message msg = mHandler.obtainMessage();
                            msg.getData().putSerializable("data", featureList.get(0));
                            msg.what = 2;
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }
        });
    }

    /**
     * 注册按钮点击事件
     */
    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.addUserLayout){
            isRegist = true;
        }
    }


    @Override
    public void regCallback() {
        isSHow = true;
        isRegist = false;
    }

    /**
     * 是否需要人脸注册接口
     */
    @Override
    public void callback(boolean success, Bitmap bitmap) {
        if (success){
            //TODO　开始人脸注册
            mDialogUtils.register(bitmap);
            isSHow = true;
            isRegist = false;
        }else {
            isSHow = true;
            isRegist = false;
        }
    }

    /**
     * 日期时间更新线程
     */
    class TimeThread extends Thread {
        @Override
        public void run() {
            while (isTimeThread){
                try {
                    Message msg = mHandler.obtainMessage();
                    Bundle data = msg.getData();
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
        private WeakReference<OrbbecVideoIdentifyActivity> mWeakReference;
        private Bitmap RgbBitmap = null;
        Bitmap bitmap = null;
        int[] rgba = null;
        private MyHandler(OrbbecVideoIdentifyActivity pWeakReference) {
            mWeakReference = new WeakReference<>(pWeakReference);
            rgba = new int[mWeakReference.get().mWidth * mWeakReference.get().mHeight];
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: //日期时间更新
                    Bundle data = msg.getData();
                    mWeakReference.get().timeTv.setText(data.getCharSequence("sysTimeStr","")); //更新时间
                    mWeakReference.get().dateTv.setText(data.getCharSequence("sysDateStr",""));
                    mWeakReference.get().xingQiTv.setText(data.getCharSequence("mWay",""));
                    break;
                case 2://打卡记录
                    Feature temp = (Feature) msg.getData().getSerializable("data");
                    if (temp==null)
                        return;
                    mWeakReference.get().tempFeature = temp;
                    if (mWeakReference.get().recordLists.size()>0) {
                        if (mWeakReference.get().recordLists.get(0).getUpdateTime()==-1){
                            mWeakReference.get().recordLists.remove(0);
                        }
                        if (!mWeakReference.get().recordLists.contains(temp)) {
                            //打卡时间
                            temp.setUpdateTime(System.currentTimeMillis());
                            mWeakReference.get().recordLists.add(0, temp);
                            mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
                        }else{
                            if (mWeakReference.get().recordLists.indexOf(temp)>=6) {
                                //将已经打卡的人脸移到第一个位置
                                int pos = mWeakReference.get().recordLists.indexOf(temp);
                                mWeakReference.get().recordLists.add(0, mWeakReference.get().recordLists.get(pos));
                                mWeakReference.get().recordLists.remove(pos + 1);
                                mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
                            }
                        }
                    }else{
                        //打卡时间
                        temp.setUpdateTime(System.currentTimeMillis());
                        mWeakReference.get().recordLists.add(temp);
                        mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
                    }

                    break;

                case MSG_WHAT://人脸识别
                    if (mWeakReference.get()!=null) {
                    if (rgba==null){
                        rgba = new int[mWeakReference.get().mWidth * mWeakReference.get().mHeight];
                    }
                    byte[] yuv = msg.getData().getByteArray(MSG_KEY);
                    if (yuv==null)
                        return;
                    bitmap = mWeakReference.get().cameraByte2Bitmap(yuv, rgba,
                            mWeakReference.get().mWidth, mWeakReference.get().mHeight);
                    if (bitmap != null) {
                        RgbBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                bitmap.getWidth(), bitmap.getHeight(), mWeakReference.get().matrix, true);
                        bitmap.recycle();
                        bitmap = null;
                        //传入rgb人脸识别bitmap数据
                        FaceLiveness.getInstance().setRgbBitmap(RgbBitmap);
                        //开始人脸识别
                        FaceLiveness.getInstance().livenessCheck( mWeakReference.get().mWidth,  mWeakReference.get().mHeight, 0X0101);
                        RgbBitmap.recycle();
                        RgbBitmap = null;
                    }
                }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取版本号名称
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


}
