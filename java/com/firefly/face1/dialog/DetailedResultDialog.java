package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.firefly.face1.R;
import com.firefly.face1.bean.PreviewResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailedResultDialog {

    private Context mContext;
    private List<String> needSignList = new ArrayList<>();
    private Map<String,Long> alreadySignMap = new HashMap<>();
    private Map<String,Long> lateSignMap = new HashMap<>();
    private List<String> neverSignList = new ArrayList<>();
    private StringBuilder stringBuilder1 = new StringBuilder();
    private StringBuilder stringBuilder2 = new StringBuilder();
    private StringBuilder stringBuilder3 = new StringBuilder();
    private StringBuilder stringBuilder4 = new StringBuilder();

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;

    public DetailedResultDialog(Context context){
        mContext = context;
    }

    public void show(PreviewResult preResult){
        Dialog mDialog = new Dialog(mContext);
        mDialog.setContentView(R.layout.detailed_result_dialog);
        mDialog.setCancelable(true);
        mDialog.show();
        needSignList = preResult.getNeedSignList();
        alreadySignMap = preResult.getAlreadySignMap();
        lateSignMap = preResult.getLateSignMap();
        neverSignList = preResult.getNeverSignList();
        for(String str1:needSignList){
            stringBuilder1.append(str1).append("    ");
        }
        for (Map.Entry<String, Long> entry : alreadySignMap.entrySet()){
            stringBuilder2.append(entry.getKey()).append(" , ").append(entry.getValue()).append("    ");
        }
        for (Map.Entry<String, Long> entry : lateSignMap.entrySet()){
            stringBuilder3.append(entry.getKey()).append(" , ").append(entry.getValue()).append("    ");
        }
        for(String str4:neverSignList){
            stringBuilder4.append(str4).append("    ");
        }
        textView1 = mDialog.findViewById(R.id.text_1);
        textView2 = mDialog.findViewById(R.id.text_2);
        textView3 = mDialog.findViewById(R.id.text_3);
        textView4 = mDialog.findViewById(R.id.text_4);
        textView1.setText(stringBuilder1);
        textView2.setText(stringBuilder2);
        textView3.setText(stringBuilder3);
        textView4.setText(stringBuilder4);
    }
}
