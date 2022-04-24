package com.firefly.face1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceFeature;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.ConfigUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.example.checkvendor.CheckVendor;
import com.firefly.face1.activate.Activation;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.aip.manager.FaceSDKManager.ACTIVATING_SUCCESS;

public class MainActivity extends Activity implements Activation.ActivationCallback {


    //请求权限
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();
    private static int MY_PERMISSIONS_REQUEST = 1;
    private Activation mActivation ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesUtil.initPrefs(getApplicationContext());
        mActivation = new Activation(MainActivity.this);
        mActivation.setActivationCallback(this);

        //gpio测试
//        boolean GPIO = MainApp.getFireflyApi().gpioCtrl(263,"out",0);
//        Log.group_manager1("lkdong", "GPIO = "+GPIO);

        CheckVendor mCheckVendor = new CheckVendor();
        int check = mCheckVendor.check();
        if (check!=1) {
            FaceSDKManager.getInstance().init(getApplicationContext(), new FaceSDKManager.SdkInitListener() {
                @Override
                public void initStart(FaceDetector mFaceDetector, FaceFeature mFaceFeature) {
                    //开发者可以在此配置人脸识别参数
                    //默认
                    mFaceDetector.init(MainActivity.this);
                    mFaceFeature.init(MainActivity.this);
                    //比如修改
                    //mFaceDetector.init(MainActivity.this, FaceEnvironment);
                    //mFaceFeature.init(MainActivity.this, FaceSDK.RecognizeType.RECOGNIZE_LIVE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setPermissions();
                        }
                    });
                }

                @Override
                public void initSuccess() {
                }

                @Override
                public void initStatus(final int statusCode) {

                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           if (statusCode!=ACTIVATING_SUCCESS) {
//                               Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                               mActivation.startActive();
                           }
                       }
                   });

                }

                @Override
                public void initActive(boolean success) {
                    if (!success){
                        mActivation.startActive();
                    }else{
                        setPermissions();
                    }

                }

                @Override
                public void initIrCamera() {

                }
            });

        }
//        setPermissions();//***为了调试添加的代码，跳过了激活环节，同时人脸对比功能也会失效***

    }

    private void setPermissions(){
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {
            //已经授予了
            choiceIdentityType(MainActivity.this);
            finish();
            System.exit(0);
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean isHasPermission = true;
        if (requestCode == MY_PERMISSIONS_REQUEST){
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                    isHasPermission = isHasPermission&&showRequestPermission;
                }
            }
            if (isHasPermission) {
                //授权成功
                choiceIdentityType(this);
                finish();
                System.exit(0);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static void choiceIdentityType(Context pContext) {
        int type = PreferencesUtil.getInt(ConfigUtils.TYPE_LIVENSS, ConfigUtils
                .TYPE_NO_LIVENSS);
        if (type == ConfigUtils.TYPE_NO_LIVENSS) {
//            Toast.makeText(pContext, "当前活体策略：无活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(pContext, RgbVideoIdentityActivity.class);
            pContext.startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_LIVENSS) {
//            Toast.makeText(pContext, "当前活体策略：单目RGB活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(pContext, RgbVideoIdentityActivity.class);
            pContext.startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_IR_LIVENSS) {
//            Toast.makeText(pContext, "当前活体策略：双目RGB+IR活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(pContext, RgbIrVideoIdentifyActivity.class);
            pContext.startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_DEPTH_LIVENSS) {
//            Toast.makeText(pContext, "当前活体策略：双目RGB+Depth活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(pContext, OrbbecVideoIdentifyActivity.class);
            pContext.startActivity(intent);
        }
    }

    @Override
    public void callback(boolean success) {
        if (success){
            setPermissions();
        }
    }

    @Override
    public void cancelActivate() {
        setPermissions();
    }
}
