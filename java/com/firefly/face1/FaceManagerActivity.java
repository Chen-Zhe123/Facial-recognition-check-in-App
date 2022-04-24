package com.firefly.face1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.aip.utils.ConfigUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.firefly.face1.FragmentView.FaceLibrary;
import com.firefly.face1.FragmentView.FaceReg;
import com.firefly.face1.FragmentView.GroupManager;


public class FaceManagerActivity extends AppCompatActivity implements View.OnFocusChangeListener, FaceReg.BeginCameraRegCallback,FaceReg.PicRegCompleteListener, CameraRegActivity.VideoRegCompleteListener{

    //顶部状态栏
    private TextView manage;
    private RelativeLayout topBar;
    //底部导航栏按钮
    private LinearLayout groupManagerButton;
    private ImageView group_manager_icon;
    private TextView group_manager_text;
    private LinearLayout faceLibraryButton;
    private ImageView face_library_icon;
    private TextView face_library_text;

    private LinearLayout faceRegButton;
    private ImageView face_reg_icon;
    private TextView face_reg_text;

    //主要内容布局
    private LinearLayout groupManagerLayout;
    private LinearLayout faceLibraryLayout;
    private LinearLayout faceRegLayout;

    //FragmentView
    private GroupManager groupManager;
    private FaceLibrary faceLibrary;
    private FaceReg faceReg;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_manager);
        findView();
        faceLibrary = new FaceLibrary(faceLibraryLayout, FaceManagerActivity.this);
        faceReg = new FaceReg(faceRegLayout, FaceManagerActivity.this);
        faceReg.setCameraRegCallback(this);
        groupManager = new GroupManager(groupManagerLayout, FaceManagerActivity.this);
    }

    private void findView(){
        manage = findViewById(R.id.manage);
        topBar = findViewById(R.id.top_bar);
        groupManagerButton = findViewById(R.id.GroupManagerButton);
        groupManagerButton.setOnFocusChangeListener(this);
        group_manager_icon = findViewById(R.id.group_manager_icon);
        group_manager_text = findViewById(R.id.group_manager_text);

        faceLibraryButton = findViewById(R.id.FaceLibraryButton);
        faceLibraryButton.setOnFocusChangeListener(this);
        face_library_icon = findViewById(R.id.face_library_icon);
        face_library_text = findViewById(R.id.face_library_text);
        faceRegButton = findViewById(R.id.face_reg_button);
        faceRegButton.setOnFocusChangeListener(this);
        face_reg_icon = findViewById(R.id.face_reg_icon);
        face_reg_text = findViewById(R.id.face_reg_text);

        groupManagerLayout = findViewById(R.id.group_manager);
        faceLibraryLayout = findViewById(R.id.face_library);
        faceRegLayout = findViewById(R.id.image_registration);
    }

    @SuppressLint("NonConstantResourceId")
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
                setLayoutHide();
                switch (v.getId()) {
                    case R.id.GroupManagerButton:
                        groupManagerLayout.setVisibility(View.VISIBLE);
                        setBottomBarColor(group_manager_icon,group_manager_text,(int)R.drawable.group_manager2);
                        manage.setVisibility(View.VISIBLE);
                        topBar.setVisibility(View.VISIBLE);
                        break;
                    case R.id.FaceLibraryButton:
                        faceLibraryLayout.setVisibility(View.VISIBLE);
                        setBottomBarColor(face_library_icon,face_library_text,(int)R.drawable.face_library2);
                        topBar.setVisibility(View.GONE);
                        break;
                    case R.id.face_reg_button:
                        faceRegLayout.setVisibility(View.VISIBLE);
                        setBottomBarColor(face_reg_icon,face_reg_text,(int)R.drawable.add_user_2);
                        topBar.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
//            }
        }
    }

    private void setBottomBarColor(ImageView imageView,TextView textView,int rid){
        group_manager_icon.setImageDrawable(getResources().getDrawable((int)R.drawable.group_manager1));
        group_manager_text.setTextColor((int)R.color.BottomNavigationBar1);

        face_library_icon.setImageDrawable(getResources().getDrawable((int)R.drawable.face_library1));
        face_library_text.setTextColor((int)R.color.BottomNavigationBar1);

        face_reg_icon.setImageDrawable(getResources().getDrawable((int)R.drawable.add_user_1));
        face_reg_text.setTextColor((int)R.color.BottomNavigationBar1);

        imageView.setImageDrawable(getResources().getDrawable(rid));
        textView.setTextColor((int)R.color.BottomNavigationBar2);
    }

    private void setLayoutHide() {
        groupManagerLayout.setVisibility(View.GONE);
        faceLibraryLayout.setVisibility(View.GONE);
        faceRegLayout.setVisibility(View.GONE);
    }

    //图片注册事务完成后对人脸库进行更新
    @Override
    public void picRegCompleteListen() {
        if (faceLibrary !=null)
            faceLibrary.init();
    }
    @Override
    public void videoRegCompleteListen() {
        if (faceLibrary !=null)
            faceLibrary.init();
    }

    public void beginCameraReg() {
        Log.d("beginReg", "beginCameraReg: 进入beginCameraReg放法");
        int type = PreferencesUtil.getInt(ConfigUtils.TYPE_LIVENSS, ConfigUtils
                .TYPE_NO_LIVENSS);
        if (type == ConfigUtils.TYPE_NO_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：无活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(FaceManagerActivity.this, CameraRegActivity.class);
            startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：单目RGB活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(FaceManagerActivity.this, CameraRegActivity.class);
            startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_IR_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：双目RGB+IR活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(FaceManagerActivity.this, CameraRegActivity.class);
            startActivity(intent);
        } else if (type == ConfigUtils.TYPE_RGB_DEPTH_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：双目RGB+Depth活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(FaceManagerActivity.this, CameraRegActivity.class);
            startActivity(intent);
        }
    }
}
