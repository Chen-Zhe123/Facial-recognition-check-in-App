/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1.activate;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.license.AndroidLicenser;
import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;

public class Activation extends Activate {

    private Context context;
    private Button activateBtn;
    private Button backBtn;
    private TextView deviceIdTv;
    public Dialog activationDialog;
    private String device = "";
    private EditText keyEt;
    private int lastKeyLen = 0;

    public Activation(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void startActive() {
        super.startActive();
        activationDialog = new Dialog(context);
        activationDialog.setContentView(initView());
        activationDialog.setCancelable(false);
        activationDialog.show();
        addLisenter();
    }

    public LinearLayout initView(){
        device = AndroidLicenser.get_device_id(context.getApplicationContext());
        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootParams.gravity = Gravity.CENTER;
        root.setBackgroundColor(context.getResources().getColor(R.color.color_181820));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        TextView titleTv = new TextView(context);
        titleTv.setText("设备激活");
        titleTv.setGravity(Gravity.CENTER);
        titleTv.setTextColor(context.getResources().getColor(R.color.color_cccccc));
        titleTv.setTextSize(dip2px(22));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                dip2px(550), LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER;
        titleParams.topMargin = dip2px(35);
        titleParams.bottomMargin = dip2px(25);


        deviceIdTv = new TextView(context);
        deviceIdTv.setText(device);
        deviceIdTv.setGravity(Gravity.CENTER);
        deviceIdTv.setTextColor(context.getResources().getColor(R.color.color_cccccc));
        deviceIdTv.setTextSize(dip2px(16));

        LinearLayout.LayoutParams deviceIdParams = new LinearLayout.LayoutParams(
                dip2px(550), LinearLayout.LayoutParams.WRAP_CONTENT);
        deviceIdParams.gravity = Gravity.CENTER;
        deviceIdParams.bottomMargin = dip2px(25);

        keyEt = new EditText(context);
//        keyEt.setHint(R.string.activate_et_hide);
        keyEt.setHint("QNKX-LOII-TRW9-U4C7");
        keyEt.setText("QNKX-LOII-TRW9-U4C7");
//        keyEt.setText(PreferencesUtil.getString("activate_key", ""));
        // keyEt.setText("VMVY-PLkd-OsJN-veIc");

        LinearLayout.LayoutParams keyParams = new LinearLayout.LayoutParams(
                dip2px(400), LinearLayout.LayoutParams.WRAP_CONTENT);
        keyParams.gravity = Gravity.CENTER;
        keyParams.bottomMargin = dip2px(30);
        keyEt.setTransformationMethod(new AllCapTransformationMethod(true));
        keyEt.setWidth(dip2px(460));
        keyEt.setTextColor(context.getResources().getColor(R.color.color_cccccc));
        keyEt.setTextSize(dip2px(17));
        keyEt.setPadding(0, dip2px(5), 0, dip2px(5));
        keyEt.setBackgroundResource(R.drawable.editttext_bg);



        LinearLayout.LayoutParams activateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dip2px(50));
        activateParams.gravity = Gravity.CENTER;
        activateBtn = new Button(context);
        activateBtn = new Button(context);
        activateBtn.setBackgroundResource(R.drawable.reg_btn_bg);
        activateBtn.setTextColor(Color.WHITE);
        activateBtn.setTextSize(dip2px(17));
        activateBtn.setGravity(Gravity.CENTER);
        activateBtn.setText(R.string.activate_text);

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dip2px(50));
        backParams.leftMargin = dip2px(30);
        backParams.gravity = Gravity.CENTER;

        backBtn = new Button(context);
        backBtn.setText(R.string.allCancel);
        backBtn.setBackgroundResource(R.drawable.reg_btn_bg);
        backBtn.setTextColor(Color.WHITE);
        backBtn.setTextSize(dip2px(17));
        backBtn.setGravity(Gravity.CENTER);


        final LinearLayout btnLayout = new LinearLayout(context);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLayoutParams.gravity = Gravity.CENTER;
        btnLayoutParams.bottomMargin = dip2px(35);

        btnLayout.addView(activateBtn, activateParams);
        btnLayout.addView(backBtn, backParams);

        root.addView(titleTv, titleParams);
        root.addView(deviceIdTv, deviceIdParams);
        root.addView(keyEt, keyParams);
        root.addView(btnLayout, btnLayoutParams);

        return root;
    }

    @Override
    public void success(String msg) {

        toast(context.getResources().getString(R.string.activate_success));
        activationDialog.dismiss();
        if (activationCallback != null) {
            activationCallback.callback(true);
        }
    }

    @Override
    public void failture(String msg) {
        if (TextUtils.equals(msg,"failture" ))
            toast(context.getResources().getString(R.string.activate_failture));
        else{
            toast(msg);
        }
        if (activationCallback != null) {
            activationCallback.callback(false);
        }
    }

    private void addLisenter() {
        keyEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 19) {
                    keyEt.setText(s.toString().substring(0, 19));
                    keyEt.setSelection(keyEt.getText().length());
                    lastKeyLen = s.length();
                    return;
                }
                if (s.toString().length() < lastKeyLen ) {
                    lastKeyLen = s.length();
                    return;
                }
                String text = s.toString().trim();
                if (keyEt.getSelectionStart() < text.length()) {
                    return;
                }
                if (text.length() == 4 || text.length() == 9 || text.length() == 14) {
                    keyEt.setText(text + "-");
                    keyEt.setSelection(keyEt.getText().length());
                }

                lastKeyLen = s.length();
            }
        });
        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = keyEt.getText().toString().trim().toUpperCase();
                if (TextUtils.isEmpty(key)) {
                    Toast.makeText(context, R.string.key_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                request(key);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activationDialog != null) {
                    activationDialog.dismiss();
                }
                if (activationCallback != null) {
                    activationCallback.cancelActivate();
                }
            }
        });

    }

    private int dip2px(int dip) {
        Resources resources = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, dip, resources.getDisplayMetrics());

        return px;
    }

}
