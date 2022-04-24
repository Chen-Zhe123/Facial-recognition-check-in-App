package com.firefly.face1.utils;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.NetRequest;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.license.AndroidLicenser;
import com.firefly.face1.R;
import com.firefly.face1.activate.Activate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ActivateSettingUtils implements View.OnClickListener {

    private LinearLayout root;
    private Context mContext;
    private EditText mEdittext;
    private TextView deviceCodeTv;
    private String device = "";
    private int lastKeyLen = 0;
    private Handler handler = new Handler();

    public ActivateSettingUtils(LinearLayout pRoot, Context pContext) {
        PreferencesUtil.initPrefs(pContext.getApplicationContext());
        root = pRoot;
        mContext = pContext;
        mEdittext = root.findViewById(R.id.activateEt);
        root.findViewById(R.id.activateBtn).setOnClickListener(this);
        deviceCodeTv = root.findViewById(R.id.device_code_tv);
        device = AndroidLicenser.get_device_id(mContext.getApplicationContext());
        deviceCodeTv.setText(device);

        mEdittext.setHint(R.string.et_hide);
        mEdittext.setText(PreferencesUtil.getString("activate_key", ""));
        mEdittext.setTransformationMethod(new Activate.AllCapTransformationMethod(true));
        mEdittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 19) {
                    mEdittext.setText(s.toString().substring(0, 19));
                    mEdittext.setSelection(mEdittext.getText().length());
                    lastKeyLen = s.length();
                    return;
                }
                if (s.toString().length() < lastKeyLen ) {
                    lastKeyLen = s.length();
                    return;
                }
                String text = s.toString().trim();
                if (mEdittext.getSelectionStart() < text.length()) {
                    return;
                }
                if (text.length() == 4 || text.length() == 9 || text.length() == 14) {
                    mEdittext.setText(text + "-");
                    mEdittext.setSelection(mEdittext.getText().length());
                }

                lastKeyLen = s.length();
            }
        });
    }


    @Override
    public void onClick(View v) {
        String key = mEdittext.getText().toString().trim().toUpperCase();
        if (TextUtils.isEmpty(key)) {
            toast( mContext.getString(R.string.key_empty));
            return;
        }
        request(key);
    }

    public void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public final void request(final String key) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                netRequest(key);

            }
        });
    }
    public final void netRequest(final String key) {
        if(NetRequest.isConnected(mContext)) {
            boolean success = NetRequest.request(new NetRequest.RequestAdapter() {
                public String getURL() {
                    return "https://ai.baidu.com/activation/key/activate";
                }

                public String getRequestString() {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("deviceId", device);
                        jsonObject.put("key", key);
                        jsonObject.put("platformType", 2);
                        jsonObject.put("version", "3.4.2");

                        return jsonObject.toString();
                    } catch (JSONException var10) {
                        var10.printStackTrace();
                        return null;
                    }
                }

                public void parseResponse(InputStream in) throws IOException, JSONException {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

                    try {
                        int e;
                        while ((e = in.read(buffer)) > 0) {
                            out.write(buffer, 0, e);
                        }
                        out.flush();
                        JSONObject json = new JSONObject(new String(out.toByteArray(), "UTF-8"));
                        int errorCode = json.optInt("error_code");
                        if (errorCode != 0) {
                            String errorMsg = json.optString("error_msg");
                            toast(errorMsg);
                        } else {
                            parse(json, key);
                        }
                    } catch (Exception e) {
                        toast(mContext.getString(R.string.activate_failture));
                    } finally {
                        if(out != null) {
                            try {
                                out.close();
                            } catch (IOException var12) {
                                var12.printStackTrace();
                            }
                        }
                    }
                }
            });

        } else {
            toast(mContext.getString(R.string.activate_failture));
        }
    }

    public final void parse(JSONObject json, String key) {
        boolean success = false;
        JSONObject result = json.optJSONObject("result");
        if (result != null) {
            String license = result.optString("license");
            if (!TextUtils.isEmpty(license)) {
                String[] licenses = license.split(",");
                if (licenses != null && licenses.length == 2) {
                    PreferencesUtil.putString("activate_key", key);
                    ArrayList<String> list = new ArrayList<>();
                    list.add(licenses[0]);
                    list.add(licenses[1]);
                    success = FileUitls.c(mContext, FaceSDKManager.LICENSE_NAME, list);
                }
            }
        }

        if (success) {
            toast(mContext.getString(R.string.activate_success));
        } else {
            toast(mContext.getString(R.string.activate_failture));

        }
    }
}
