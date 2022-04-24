/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.firefly.face1.activate;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.NetRequest;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.aip.utils.ZipUtil;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.license.AndroidLicenser;
import com.example.checkvendor.CheckVendor;
import com.firefly.face1.DataBase.DBMaster;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public abstract class Activate {

    private Context context;
    private String device = "";

    private Handler handler = new Handler();
    public ActivationCallback activationCallback;
    private int check = 1;
    private ArrayList<String> list = new ArrayList<>();
    private boolean success = false;

    public Activate(Context context) {
        this.context = context;
        device = AndroidLicenser.get_device_id(context.getApplicationContext());
        CheckVendor checkVendor = new CheckVendor();
        check = checkVendor.check();
    }

    public void setActivationCallback(ActivationCallback callback) {
        this.activationCallback = callback;
    }


    public void startActive() {
        PreferencesUtil.initPrefs(context.getApplicationContext());
    }


    public abstract LinearLayout initView();


    public void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
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
        if(NetRequest.isConnected(context)&&check!=1) {
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
                            failture(errorMsg);
                        } else {
                            parse(json, key);
                        }
                    } catch (Exception e) {
                        failture("failture");
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
            failture("failture");
        }
    }

    public final void parse(JSONObject json, String key) {
        boolean success = false;
        JSONObject result = json.optJSONObject("result");
        if (result != null&&check!=1) {
            String license = result.optString("license");
            if (!TextUtils.isEmpty(license)) {
                String[] licenses = license.split(",");
                if (licenses != null && licenses.length == 2) {
                    PreferencesUtil.putString("activate_key", key);

                    list.add(licenses[0]);
                    list.add(licenses[1]);
                    success = FileUitls.c(context, FaceSDKManager.LICENSE_NAME, list);
                }
            }
        }

        if (success) {
            DBMaster.getInstance().init(context);
            if(DBMaster.getInstance().mFieldTable.initField()){
                success("success");
            }else {
                failture("failture");
            }
        } else {
            failture("failture");

        }
    }

    public abstract void success(String msg);
    public abstract void failture(String msg);

    public interface ActivationCallback {

        public void callback(boolean success);
        public void cancelActivate();
    }

    public static final class AllCapTransformationMethod extends ReplacementTransformationMethod {

        private char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        private boolean allUpper = false;

        public AllCapTransformationMethod(boolean needUpper) {
            this.allUpper = needUpper;
        }

        @Override
        protected char[] getOriginal() {
            if (allUpper) {
                return lower;
            } else {
                return upper;
            }
        }

        @Override
        protected char[] getReplacement() {
            if (allUpper) {
                return upper;
            } else {
                return lower;
            }
        }
    }

    //离线激活
    public void offlineActive(){
        String path = getSDPath();
        offLine_Active(path);
    }

    //离线激活：将激活文件置于SD卡根目录（/storage/emulated/0）中，SDK会自动进行激活
    private void offLine_Active(String path) {

        if (FaceSDK.getAuthorityStatus() == AndroidLicenser.ErrorCode.SUCCESS.ordinal()) {
            Toast.makeText(context, "已经激活成功", Toast.LENGTH_LONG).show();
            return;
        }

        String firstPath = path + "/" + "License.zip";
        if (fileIsExists(firstPath)) {
            if (!TextUtils.isEmpty(firstPath)) {
                ZipUtil.unzip(firstPath);
            }
            if (ZipUtil.isSuccess) {
                String secondPath = path + "/" + "Win.zip";
                if (!TextUtils.isEmpty(secondPath)) {
                    ZipUtil.unzip(secondPath);
                }
            }
            String keyPath = path + "/" + "license.key";
            String key = readFile(keyPath, "key");
            PreferencesUtil.putString("activate_key", key);
            String liscensePaht = path + "/" + "license.ini";
            String liscense = readFile(liscensePaht, "liscense");
            success = FileUitls.c(context, FaceSDKManager.LICENSE_NAME, list);
            if (success) {
                toast("激活成功");
                FaceSDKManager.initStatus = FaceSDKManager.SDK_UNINIT;
                FaceSDKManager.getInstance().init(context,null);
            } else {
                toast("激活失败");
            }
        } else {
            toast("授权文件不存在!");
        }
    }

    //读取文本文件中的内容
    public String readFile(String strFilePath, String mark) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                        if (mark.equals("liscense")) {
                            list.add(line);
                        }
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

    //判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

}
