/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
import com.baidu.aip.utils.ConfigUtils;
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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.baidu.aip.manager.FaceSDKManager.ACTIVATING_SUCCESS;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_ID_PHOTO;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_LIVE;
import static com.baidu.aip.utils.ConfigUtils.TYPE_MODEL;
import static java.lang.System.currentTimeMillis;

public class RgbIrVideoIdentifyActivity extends Activity implements ILivenessCallBack, View.OnClickListener, CameraRegDialog.RegCallback {
	
	private static final String TAG = "FaceRgbIrLiveness";
	private static final int FEATURE_DATAS_UNREADY = 1;
	private static final int IDENTITY_IDLE = 2;
	private static final int IDENTITYING = 3;
	// ???????????????????????????????????????????????????640*480??? 1280*720
	private static final int PREFER_WIDTH = 640;
	private static final int PERFER_HEIGH = 480;

	Preview[] mPreview;
	Camera[] mCamera;
	private int mCameraNum;
	private ExecutorService es = Executors.newSingleThreadExecutor();

	//???????????????????????????????????????????????????
	private TextView detectDurationTv;
	private TextView rgbLivenssDurationTv;
	private TextView rgbLivenessScoreTv;
	private TextView irLivenssDurationTv;
	private TextView irLivenessScoreTv;
	private TextView tvAttr;

	private volatile int identityStatus = FEATURE_DATAS_UNREADY;
	private String userIdOfMaxScore = "";
	private float maxScore = 0;

	//???????????????view
	private SurfaceView camera0_surfaceview;
	private SurfaceView camera1_surfaceview;

	//???????????????View
	private TextureView camera0_textureView;
	private TextureView camera1_textureView;

	private volatile int[] niRargb;
	private volatile int[] rgbData;
	private volatile byte[] irData;
	private int camemra1DataMean;
	private int camemra2DataMean;
	private volatile boolean camemra1IsRgb = false;
	private volatile boolean rgbOrIrConfirm = false;

	//title??????
	private MyHandler mHandler;
	private TextView timeTv,dateTv,xingQiTv,versionCodeTv;
	private ImageView settingIv;
	private  static String xingQis[] ;
	private boolean isTimeThread = false;

	//root??????
	private HorizontalListView mScrollView;
	private PartRecordAdapter mAdapter;
	private List<Feature>recordLists = new ArrayList<>();

	//?????????camera???surfaceview??????LayoutParams??????
	private FrameLayout.LayoutParams lp_250 = new FrameLayout.LayoutParams(250, 250);

	private LinearLayout addUserLayout;
	//????????????????????????
	private static boolean isRegist = false;
	//??????????????????Activity
	private boolean isQuit = false;

	//??????dialog
	private CameraRegDialog mDialogUtils;
	//??????RegDialogUtils?????????
	private boolean isSHow = true;

	//??????????????????????????????
	private static Feature tempFeature = null;

	private final Object syncFaceDetect = new Object();

	//??????????????????bitmap,??????????????????????????????
	private Bitmap detectbitmap = null;
	private Bitmap drawbitmap = null;
    private String UserName = "";
	//????????????
	private FaceAttribute mFaceAttribute;

	//???????????????
	private Bitmap face_detect_bg;
	//????????????????????????
	private Bitmap face_recog_bg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		mCameraNum = Camera.getNumberOfCameras();
		PreferencesUtil.initPrefs(getApplicationContext());
		//??????????????????
		DBMaster.getInstance().init(getApplicationContext());

		//?????????????????????sdk
		FaceSDKManager.getInstance().init(getApplicationContext(),new FaceSDKManager.SdkInitListener() {

			@Override
			public void initStart(FaceDetector mFaceDetector, FaceFeature mFaceFeature) {
				//?????????????????????????????????????????????
				//??????
				mFaceDetector.init(RgbIrVideoIdentifyActivity.this);
				mFaceFeature.init(RgbIrVideoIdentifyActivity.this);
				//????????????
				//mFaceDetector.init(RgbIrVideoIdentifyActivity.this, FaceEnvironment);
				//mFaceFeature.init(RgbIrVideoIdentifyActivity.this, FaceSDK.RecognizeType.RECOGNIZE_LIVE);
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
							Toast.makeText(RgbIrVideoIdentifyActivity.this,
									R.string.please_activate, Toast.LENGTH_LONG).show();
					}
				});
			}

			@Override
			public void initActive(boolean success) {

			}

			@Override
			public void initIrCamera() {
				if (mCameraNum==2)
					if (PreferencesUtil.getInt(ConfigUtils.TYPE_LIVENSS, ConfigUtils.TYPE_NO_LIVENSS)==ConfigUtils.TYPE_RGB_IR_LIVENSS)
						FaceSDK.initModel(RgbIrVideoIdentifyActivity.this);
			}
		});
        setContentView(R.layout.activity_rgb_ir_video_identify);
		face_detect_bg = BitmapFactory.decodeResource(getResources(), R.drawable.face_detect_bg);
		face_recog_bg =  BitmapFactory.decodeResource(getResources(), R.drawable.recogition);
		mDialogUtils = new CameraRegDialog(this);
		mDialogUtils.setActivationCallback(this);
		findView();

		FaceLiveness.getInstance().setLivenessCallBack(this);
		loadFeature2Memery();

		timeTv = findViewById(R.id.timeTv);
		dateTv = findViewById(R.id.dateTv);
		xingQiTv = findViewById(R.id.xingqiTv);
		settingIv = findViewById(R.id.settingIv);
		versionCodeTv = findViewById(R.id.versionCodeTv);
		versionCodeTv.setText(getResources().getString(R.string.versionCode)+getVerName(this));
		xingQis = getResources().getStringArray(R.array.XingQis);
		mHandler = new MyHandler(this);

		// ????????????1???n?????????
		lp_250.gravity = Gravity.END;
	}

	@SuppressLint({"WrongViewCast", "StringFormatInvalid"})
	private void findView() {
		detectDurationTv = findViewById(R.id.detect_duration_tv);
		rgbLivenssDurationTv = findViewById(R.id.rgb_liveness_duration_tv);
		rgbLivenessScoreTv = findViewById(R.id.rgb_liveness_score_tv);
		irLivenssDurationTv = findViewById(R.id.ir_liveness_duration_tv);
		irLivenessScoreTv = findViewById(R.id.ir_liveness_score_tv);
		detectDurationTv.setText(getString(R.string.rgb_detectduration,""));
		rgbLivenessScoreTv.setText(getString(R.string.rgb_liveness_score,""));
		rgbLivenssDurationTv.setText(getString(R.string.rgb_liveness_duration,""));
		irLivenessScoreTv.setText(getString(R.string.ir_liveness_score,""));
		irLivenssDurationTv.setText(getString(R.string.ir_liveness_duration,""));
		tvAttr = findViewById(R.id.tvAttr);

		camera0_surfaceview = findViewById(R.id.camera0);
		camera1_surfaceview = findViewById(R.id.camera1);
		camera0_textureView = findViewById(R.id.texture0);
		camera1_textureView = findViewById(R.id.texture1);

			//????????????
		addUserLayout = findViewById(R.id.addUserLayout);
		addUserLayout.setOnClickListener(this);

		//root??????
		mScrollView = findViewById(R.id.recordScrollView);
		//????????????
		Feature firstFeature = new Feature();
		firstFeature.setUpdateTime(-1);
		firstFeature.setUserName("?????????");
		recordLists.add(firstFeature);
		mAdapter = new PartRecordAdapter(recordLists);
		mScrollView.setAdapter(mAdapter);

		if (mCameraNum == 2) {
			mPreview = new Preview[mCameraNum];
			mCamera = new Camera[mCameraNum];
			mPreview[0] = new Preview(this, camera0_surfaceview);
			mPreview[1] = new Preview(this, camera1_surfaceview);
		}
		if (camera0_textureView != null) {
			camera0_textureView.setOpaque(false);
		}
		if (camera1_textureView!=null){
			camera1_textureView.setOpaque(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		isTimeThread = true;
		new TimeThread().start(); //??????????????????
		isQuit = false;
		if (mCameraNum != 2) {
			Toast.makeText(this, "????????????2????????????", Toast.LENGTH_LONG).show();
			return;
		}else {
			try {
				mCamera[0] = Camera.open(0);
				mPreview[0].setCamera(mCamera[0], PREFER_WIDTH, PERFER_HEIGH);
				mCamera[0].setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						if (!isQuit) {
							if (rgbOrIrConfirm) {
								choiceRgbOrIrType(0, data);
							} else if (camemra1DataMean == 0) {
								rgbOrIr(0, data);
							}
						}
					}
				});
				mCamera[1] = Camera.open(1);
				mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGH);
				mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						if (!isQuit) {
							if (rgbOrIrConfirm) {
								choiceRgbOrIrType(1, data);
							} else if (camemra2DataMean == 0) {
								rgbOrIr(1, data);
							}
						}

					}
				});
			} catch (Exception pE) {
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		isQuit = true;
		isTimeThread = false;
		if (mCameraNum==2) {
			if (mPreview[0] != null) {
				mPreview[0].setCamera(null, PREFER_WIDTH, PERFER_HEIGH);
				mPreview[0].release();
			}
			if (mPreview[1] != null) {
				mPreview[1].setCamera(null, PREFER_WIDTH, PERFER_HEIGH);
				mPreview[1].release();
			}
		}
		finish();
		System.exit(0);

	}

	//rgb????????????Ir??????????????????
	private void rgbOrIr(int index, byte[] data) {
		byte[] tmp = new byte[PREFER_WIDTH * PERFER_HEIGH];
		System.arraycopy(data, 0 ,  tmp, 0, PREFER_WIDTH * PERFER_HEIGH);
		int count = 0;
		int total = 0;
		for (int i = 0; i < PREFER_WIDTH * PERFER_HEIGH; i = i + 100) {
			total +=  byteToInt(tmp[i]);
			count++;
		}

		if (index == 0) {
			camemra1DataMean = total / count;
		} else {
			camemra2DataMean = total / count;
		}
		if (camemra1DataMean != 0 && camemra2DataMean != 0) {
			if (camemra1DataMean > camemra2DataMean) {
				camemra1IsRgb = true;
			} else {
				camemra1IsRgb = false;
			}
			rgbOrIrConfirm = true;
		}
	}

	public int byteToInt(byte b) {
		//Java ????????? byte ???????????????????????????????????????????????? 0xFF ???????????????????????????????????????
		return b & 0xFF;
	}

    /**
     * ????????????????????????????????????????????????
     */
	private void choiceRgbOrIrType(int index, byte[] data) {
		// camera1?????????rgb???????????????dealRgb????????????Ir???????????????Ir
		if (index == 0) {
			if (camemra1IsRgb) {
				dealRgb(data);
			} else {
				dealIr(data);
			}
		} else {
			if (camemra1IsRgb) {
				dealIr(data);
			} else {
				dealRgb(data);
			}
		}
	}

	//??????Rgb?????????????????????
	private void dealRgb(byte[] data) {
		if (rgbData == null) {
			int[] argb = new int[PREFER_WIDTH * PERFER_HEIGH];
			FaceSDK.getARGBFromYUVimg(data, argb, PREFER_WIDTH, PERFER_HEIGH, 0, 0);
			rgbData = argb;
			checkData();
		}
	}

    //??????IR?????????????????????
	private void dealIr(byte[] data) {
		if (irData == null) {
			niRargb = new int[PREFER_WIDTH * PERFER_HEIGH];
			FaceSDK.getARGBFromYUVimg(data, niRargb, PERFER_HEIGH, PREFER_WIDTH, 0, 0);

			byte[] ir = new byte[PREFER_WIDTH * PERFER_HEIGH];
			System.arraycopy(data, 0, ir, 0, PREFER_WIDTH * PERFER_HEIGH);
			irData = ir;
			checkData();
		}
	}

	private synchronized void checkData() {
		if (rgbData != null && irData != null) {
			FaceLiveness.getInstance().setNirRgbInt(niRargb);
			FaceLiveness.getInstance().setRgbInt(rgbData);
			FaceLiveness.getInstance().setIrData(irData);
			FaceLiveness.getInstance().livenessCheck(PREFER_WIDTH, PERFER_HEIGH, 0x0011);
			rgbData = null;
			irData = null;
		}
	}


    /**
     * ????????????????????????
     */
	@Override
	public void onCallback(LivenessModel livenessModel) {
		if (!isQuit)
			checkResult(livenessModel);
	}

    /**
     * ???????????????????????????
     */
	@Override
	public void onCallback(ImageFrame pFrame, FaceInfo infos) {
		try {
			if (tempFeature != null) {
                String user_id = tempFeature.getUserId();
                UserName = tempFeature.getUserName();
                tempFeature = null;
                File faceDir = FileUitls.getFaceDirectory();
                if (faceDir != null && faceDir.exists()&&!TextUtils.isEmpty(user_id)) {
					detectbitmap = BitmapFactory.decodeFile(faceDir.getPath() + "/" + user_id);
					drawbitmap = Bitmap.createScaledBitmap(detectbitmap,
                            (int) RgbIrVideoIdentifyActivity.this.getResources().getDimension(R.dimen.canvas_bitmap_w),
                            (int) RgbIrVideoIdentifyActivity.this.getResources().getDimension(R.dimen.canvas_bitmap_h),
                            true);
                    showFrame(pFrame,infos,drawbitmap,UserName);
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
	public void onTip(int code, String msg) {

	}

	/**
	 * ??????????????????
	 * ??????????????????
	 */
	private void attrCheck(FaceInfo faceInfo, ImageFrame imageFrame) {
		// todo ????????????????????????
		mFaceAttribute = FaceSDK.faceAttribute(imageFrame.getArgb(), imageFrame.getWidth(), imageFrame.getHeight(), FaceSDK.ImgType.ARGB,
				faceInfo.landmarks);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvAttr.setText("???????????????" + getMsg(mFaceAttribute));
			}
		});
	}

	public String getMsg(FaceAttribute attribute) {
		StringBuilder msg = new StringBuilder();
		if (attribute != null) {
			msg.append("").append((int) attribute.age).append(",")
					.append(attribute.race == 0 ? "?????????" : attribute.race == 1 ? "?????????" :
							attribute.race == 2 ? "??????" : attribute.race == 3 ? "?????????" : "?????????").append(",")
					.append(attribute.expression == 0 ? "??????" : attribute.expression == 1 ? "??????" : "??????").append(",")
					.append(attribute.gender == 0 ? "???" : attribute.gender == 1 ? "???" : "??????").append(",")
					.append(attribute.glasses == 0 ? "????????????" : attribute.glasses == 1 ? "??????????????????" : "?????????");
		}
		return msg.toString();
	}

	/**
	 * ??????????????????
	 * @param model
	 */
	private void checkResult(final LivenessModel model) {
		if (model == null) {
			return;
		}
		//????????????????????????????????????????????????
		displayResult(model);
			if (isRegist){//face1 register
//
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
							Toast.makeText(RgbIrVideoIdentifyActivity.this,
									R.string.user_face_registered, Toast.LENGTH_SHORT).show();
							}
							});
					isSHow = false;
				}
			}else {//face1 detect
				int type = model.getLiveType();
				boolean livenessRgb = false;
				boolean livenessIr = false;
				// ????????????????????????????????????????????????????????????????????????????????????????????????
				if ((type & FaceLiveness.MASK_RGB) == FaceLiveness.MASK_RGB) {
					livenessRgb = (model.getRgbLivenessScore() > FaceEnvironment.LIVENESS_RGB_THRESHOLD);
					}
					if ((type & FaceLiveness.MASK_IR) == FaceLiveness.MASK_IR) {
					livenessIr = (model.getIrLivenessScore() > FaceEnvironment.LIVENESS_IR_THRESHOLD);
				}
				if (livenessRgb&&livenessIr) {
					asyncIdentity(model.getImageFrame(), model.getFaceInfo()); }
//								attrCheck( model.getFaceInfo(),model.getImageFrame());
//								isStartDetect = true;
			}
	}

	/**
	 * ???????????????????????????
	 * @param livenessModel
	 */
	private void displayResult(final LivenessModel livenessModel) {
		runOnUiThread(new Runnable() {
			@SuppressLint({"StringFormatInvalid", "StringFormatMatches"})
			@Override
			public void run() {
				int type = livenessModel.getLiveType();

				if ((type & FaceLiveness.MASK_RGB) == FaceLiveness.MASK_RGB) {
				    detectDurationTv.setText(getString(R.string.rgb_detectduration,livenessModel.getRgbDetectDuration()));
					rgbLivenessScoreTv.setText(getString(R.string.rgb_liveness_score,livenessModel.getRgbLivenessScore()));
					rgbLivenssDurationTv.setText(getString(R.string.rgb_liveness_duration,livenessModel.getRgbLivenessDuration()));
				}

				if ((type & FaceLiveness.MASK_IR) == FaceLiveness.MASK_IR) {
					irLivenessScoreTv.setText(getString(R.string.ir_liveness_score,livenessModel.getIrLivenessScore()));
					irLivenssDurationTv.setText(getString(R.string.ir_liveness_duration,livenessModel.getIrLivenessDuration()));
				}
			}
		});
	}

	private void displayTip(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (detectDurationTv!=null)
					detectDurationTv.setText(text);
			}
		});
	}

	private void toast(final String tip) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(RgbIrVideoIdentifyActivity.this, tip, Toast.LENGTH_LONG).show();
			}
		});
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
				// android.os.Process.setThreadPriority (-4);
				FaceApi.getInstance().loadFaceFromLibrary();
				identityStatus = IDENTITY_IDLE;
			}
		});
	}

	/**
	 * ??????????????????
	 */
	private void asyncIdentity(final ImageFrame imageFrame, final FaceInfo faceInfo) {
		if (identityStatus != IDENTITY_IDLE) {
			return;
		}
		identity(imageFrame, faceInfo);
	}

    /**
     * ?????????????????????????????????????????????????????????????????????
     */
	private void identity(ImageFrame imageFrame, FaceInfo faceInfo) {

		if (imageFrame == null || faceInfo == null) {
			return ;
		}
		float raw  = Math.abs(faceInfo.headPose[0]);
		float patch  = Math.abs(faceInfo.headPose[1]);
		float roll  = Math.abs(faceInfo.headPose[2]);
		// ???????????????????????????20???????????????
		if (raw > 20 || patch > 20 ||  roll > 20) {
			return ;
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
		}
		else if (type == RECOGNIZE_ID_PHOTO) {
			identifyRet = FaceApi.getInstance().identityForIDPhoto(argb, rows, cols, landmarks);
		}
		displayUserOfMaxScore(identifyRet.getUserId(), identifyRet.getScore());
		identityStatus = IDENTITY_IDLE;
	}

	private Paint paint = new Paint();
	private RectF rectF = new RectF();
	{
		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.STROKE);
		paint.setTextSize(30);
	}

	/**
	 * ??????????????????
	 *
	 */
	@SuppressLint("ResourceAsColor")
	private void showFrame(ImageFrame imageFrame, FaceInfo faceInfo, Bitmap pBitmap,String userName) {
		Canvas canvas = null;
		if (camemra1IsRgb) {
			canvas = camera0_textureView.lockCanvas();
		}else{
			canvas = camera1_textureView.lockCanvas();
		}

		if (canvas == null) {
			return;
		}
		if (faceInfo == null ) {
			// ??????canvas
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			if (camemra1IsRgb) {
				camera0_textureView.unlockCanvasAndPost(canvas);
			}else{
				camera1_textureView.unlockCanvasAndPost(canvas);
			}
			return;
		}
		rectF.set(getFaceRect(faceInfo, imageFrame));
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

		// ??????????????????????????????????????????????????????????????????
//		previewView.mapFromOriginalRect(rectF);

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
			if (camemra1IsRgb) {
				camera0_textureView.unlockCanvasAndPost(canvas);
			}else{
				camera1_textureView.unlockCanvasAndPost(canvas);
			}
		} else {
			// ?????????????????????????????????
			paint.setColor(Color.GREEN);
			paint.setStyle(Paint.Style.STROKE);
			// ????????????????????????
			if (!isRegist) {
				canvas.drawBitmap(face_recog_bg, rectF.right, rectF.top - 57, null);
				if (pBitmap != null) {
					canvas.drawBitmap(pBitmap, rectF.right + 82, rectF.top - 58, null);
				}
				String text = getString(R.string.user_no_register);
				if (userName!=null) {
					text = userName;
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
			canvas.drawBitmap(face_detect_bg, null, rectF, null);
			if (camemra1IsRgb) {
				camera0_textureView.unlockCanvasAndPost(canvas);
			}else{
				camera1_textureView.unlockCanvasAndPost(canvas);
			}
		}
	}

	/**
	 * ????????????????????????
	 *
	 * @return ???????????????
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
		int previewWidth;
		int previewHeight;

		if (camemra1IsRgb){
			previewWidth = camera0_surfaceview.getMeasuredWidth();
			previewHeight = camera0_surfaceview.getMeasuredHeight();
		}else{
			previewWidth = camera1_surfaceview.getMeasuredWidth();
			previewHeight = camera1_surfaceview.getMeasuredHeight();
		}

		float scaleW = 1.0f * previewWidth / frame.getWidth();
		float scaleH = 1.0f * previewHeight / frame.getHeight();
		int width = (right - left);
		int height = (bottom - top);
		left = (int) ((faceInfo.mCenter_x - width/2) * scaleW);
		top = (int) ((faceInfo.mCenter_y - height/2) * scaleH);
		rect.top = top < 0 ? 0 : top;
		rect.left = left < 0 ? 0 : left;
		rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width);
		rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);
		return rect;
	}

	private void displayUserOfMaxScore(final String userId, final float score) {
				if (score < 80) {
					return ;
				}
				if (!TextUtils.isEmpty(userIdOfMaxScore)&&userIdOfMaxScore.equals(userId)) {
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
							mHandler.removeMessages(2,null);
							Message msg = mHandler.obtainMessage();
							msg.getData().putSerializable("data", featureList.get(0));
							msg.what =2;
							mHandler.sendMessage(msg);
						}
					}
				}


	}

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
	 *
	 */
	@Override
	public void callback(boolean success, Bitmap bitmap) {
		if (success){
			//TODO???????????????
			mDialogUtils.register(bitmap);
			isSHow = true;
			isRegist = false;
		}else {
			isSHow = true;
			isRegist = false;
		}
	}

	/**
	 * ????????????????????????
	 */
	class TimeThread extends Thread {
		@Override
		public void run() {
			while (isTimeThread){
				try {
					Message msg = new Message();
					Bundle data = new Bundle();
					msg.what = 1;  //??????
					long sysTime = currentTimeMillis();//??????????????????
					CharSequence sysTimeStr = DateFormat.format("hh:mm:ss", sysTime);//??????????????????
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
					mHandler.sendMessage(msg);// ??????1???????????????msg???mHandler
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static boolean isDetect = true;
	//???????????????????????????????????????UI??????
	private static class MyHandler extends Handler {
		private WeakReference<RgbIrVideoIdentifyActivity> mWeakReference;
		private Thread mThread;

		private MyHandler(RgbIrVideoIdentifyActivity pWeakReference) {
			mWeakReference = new WeakReference<>(pWeakReference);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1://??????????????????
					Bundle data = msg.getData();
					mWeakReference.get().timeTv.setText(data.getCharSequence("sysTimeStr","")); //????????????
					mWeakReference.get().dateTv.setText(data.getCharSequence("sysDateStr",""));
					mWeakReference.get().xingQiTv.setText(data.getCharSequence("mWay",""));
					break;
				case 2://????????????
					Feature temp = (Feature) msg.getData().getSerializable("data");
					if (temp==null)
						return;
					mWeakReference.get().tempFeature = temp;
					if (mWeakReference.get().recordLists.size()>0) {
						if (mWeakReference.get().recordLists.get(0).getUpdateTime()==-1){
							mWeakReference.get().recordLists.remove(0);
						}
						if (!mWeakReference.get().recordLists.contains(temp)) {
							//????????????
							temp.setUpdateTime(System.currentTimeMillis());
							mWeakReference.get().recordLists.add(0, temp);
							mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
						}else{
							if (mWeakReference.get().recordLists.indexOf(temp)>=6) {
								//?????????????????????????????????????????????
								int pos = mWeakReference.get().recordLists.indexOf(temp);
								mWeakReference.get().recordLists.add(0, mWeakReference.get().recordLists.get(pos));
								mWeakReference.get().recordLists.remove(pos + 1);
								mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
							}
						}
					}else{
						//????????????
						temp.setUpdateTime(System.currentTimeMillis());
						mWeakReference.get().recordLists.add(temp);
						mWeakReference.get().mAdapter.setData(mWeakReference.get().recordLists);
					}
					break;
				default:
					break;
			}
		}
	}

	/**
	 * ?????????????????????
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