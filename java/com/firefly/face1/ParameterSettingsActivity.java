package com.firefly.face1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;

import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceFeature;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.ConfigUtils;
import com.baidu.aip.utils.PreferencesUtil;

import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_ID_PHOTO;
import static com.baidu.aip.utils.ConfigUtils.RECOGNIZE_LIVE;
import static com.baidu.aip.utils.ConfigUtils.TYPE_LIVENSS;
import static com.baidu.aip.utils.ConfigUtils.TYPE_MODEL;
import static com.baidu.aip.utils.ConfigUtils.TYPE_NO_LIVENSS;
import static com.baidu.aip.utils.ConfigUtils.TYPE_RGB_DEPTH_LIVENSS;
import static com.baidu.aip.utils.ConfigUtils.TYPE_RGB_IR_LIVENSS;
import static com.baidu.aip.utils.ConfigUtils.TYPE_RGB_LIVENSS;

public class ParameterSettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private RadioButton radioButton4;
    private RadioButton radioButton5;
    private RadioButton radioButton6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameter_settings);
        findView();
        setRadioButtonToFalse();
        int livingType = PreferencesUtil.getInt(TYPE_LIVENSS, TYPE_NO_LIVENSS);
        int featureType = PreferencesUtil.getInt(TYPE_MODEL, RECOGNIZE_LIVE);
        defaultLiveness(livingType,featureType);

        FaceSDKManager.getInstance().init(getApplicationContext(), new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart(FaceDetector mFaceDetector, FaceFeature mFaceFeature) {
                //开发者可以在此配置人脸识别参数
                //默认
                mFaceDetector.init(ParameterSettingsActivity.this);
                mFaceFeature.init(ParameterSettingsActivity.this);
                //比如修改
                //mFaceDetector.init(SettingsActivity.this, FaceEnvironment);
                //mFaceFeature.init(SettingsActivity.this, FaceSDK.RecognizeType.RECOGNIZE_LIVE);
            }

            @Override
            public void initSuccess() {
            }

            @Override
            public void initStatus(final int statusCode) {

            }

            @Override
            public void initActive(boolean success) {

            }

            @Override
            public void initIrCamera() {

            }
        });
    }

    private void findView() {
        radioButton1 = (RadioButton) findViewById(R.id.no_liveness_rb);
        radioButton2 = (RadioButton) findViewById(R.id.rgb_liveness_rb);
        radioButton4 = (RadioButton) findViewById(R.id.rgb_depth_liveness_rb);
        radioButton3 = (RadioButton) findViewById(R.id.rgb_ir_liveness_rb);
        radioButton5 = (RadioButton) findViewById(R.id.feature_liveness_rb);
        radioButton6 = (RadioButton) findViewById(R.id.feature_id_photo_rb);

        findViewById(R.id.oneCamera_no_livenessLayout).setOnClickListener(this);
        findViewById(R.id.oneCamera_livenessLayout).setOnClickListener(this);
        findViewById(R.id.TwoCamera_livenessLayout).setOnClickListener(this);
        findViewById(R.id.orbbec_livenessLayout).setOnClickListener(this);
        findViewById(R.id.feature_livenessLayout).setOnClickListener(this);
        findViewById(R.id.feature_id_photoLayout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.oneCamera_no_livenessLayout:
                radioButton1.setChecked(true);
                radioButton2.setChecked(false);
                radioButton3.setChecked(false);
                radioButton4.setChecked(false);
                PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_NO_LIVENSS);
                break;
            case R.id.oneCamera_livenessLayout:
                radioButton1.setChecked(false);
                radioButton2.setChecked(true);
                radioButton3.setChecked(false);
                radioButton4.setChecked(false);
                PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_RGB_LIVENSS);
                break;
            case R.id.TwoCamera_livenessLayout:
                radioButton1.setChecked(false);
                radioButton2.setChecked(false);
                radioButton3.setChecked(true);
                radioButton4.setChecked(false);
                PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_RGB_IR_LIVENSS);
                break;
            case R.id.orbbec_livenessLayout:
                radioButton1.setChecked(false);
                radioButton2.setChecked(false);
                radioButton3.setChecked(false);
                radioButton4.setChecked(true);
                PreferencesUtil.putInt(TYPE_LIVENSS, TYPE_RGB_DEPTH_LIVENSS);
                break;
            case R.id.feature_livenessLayout:
                radioButton5.setChecked(true);
                radioButton6.setChecked(false);
                PreferencesUtil.putInt(TYPE_MODEL, RECOGNIZE_LIVE);
                break;
            case R.id.feature_id_photoLayout:
                radioButton5.setChecked(false);
                radioButton6.setChecked(true);
                PreferencesUtil.putInt(TYPE_MODEL, RECOGNIZE_ID_PHOTO);
                break;
            default:
                break;
        }
    }
    private void defaultLiveness(int type1,int type2) {
        if (type1 == TYPE_NO_LIVENSS) {
            radioButton1.setChecked(true);
        } else if (type1 == TYPE_RGB_LIVENSS) {
            radioButton2.setChecked(true);
        } else if (type1 == TYPE_RGB_DEPTH_LIVENSS) {
            radioButton4.setChecked(true);
        } else if (type1 == TYPE_RGB_IR_LIVENSS) {
            radioButton3.setChecked(true);
        }
        if (type2 == RECOGNIZE_LIVE) {
            radioButton5.setChecked(true);
        } else if (type2 == RECOGNIZE_ID_PHOTO) {
            radioButton6.setChecked(true);
        }
    }

    private void setRadioButtonToFalse(){
        radioButton1.setChecked(false);
        radioButton2.setChecked(false);
        radioButton3.setChecked(false);
        radioButton4.setChecked(false);
        radioButton5.setChecked(false);
        radioButton6.setChecked(false);
    }
    /**
     * 根据用户设置的摄像头类型，选择不同的人脸识别界面
     */
    private void choiceIdentityType() {
        int type = PreferencesUtil.getInt(ConfigUtils.TYPE_LIVENSS, ConfigUtils
                .TYPE_NO_LIVENSS);
        Intent intent = new Intent(ParameterSettingsActivity.this, RgbVideoIdentityActivity.class);
        startActivity(intent);
        /*
        if (type == ConfigUtils.TYPE_NO_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：无活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ParameterSettingsActivity.this, RgbVideoIdentityActivity.class);
            startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：单目RGB活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ParameterSettingsActivity.this, RgbVideoIdentityActivity.class);
            startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_IR_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：双目RGB+IR活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ParameterSettingsActivity.this, RgbIrVideoIdentifyActivity.class);
            startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_DEPTH_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：双目RGB+Depth活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ParameterSettingsActivity.this, OrbbecVideoIdentifyActivity.class);
            startActivity(intent);
        }
         */
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            choiceIdentityType();
            finish();
//            System.exit(0);
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }


}