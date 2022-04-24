package com.firefly.face1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firefly.face1.R;

public class PicRegTipDialog {
    private Context context;
    private Dialog activationDialog;
    private TextView content;

    public PicRegTipDialog(Context pContext) {
        context = pContext;
    }

    private LinearLayout initView(String tipString){
        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootParams.gravity = Gravity.CENTER;
        root.setBackgroundColor(context.getResources().getColor(R.color.color_181820 ));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        TextView titleTv = new TextView(context);
        titleTv.setText(R.string.picRegDialog_title);
        titleTv.setGravity(Gravity.CENTER);
        titleTv.setTextColor(context.getResources().getColor(R.color.color_cccccc));
        titleTv.setTextSize(dip2px(22));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(dip2px(427),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER;
        titleParams.topMargin = dip2px(35);
        titleParams.bottomMargin = dip2px(42);

        content = new TextView(context);
        content.setText(tipString);
        content.setGravity(Gravity.CENTER);
        content.setTextColor(context.getResources().getColor(R.color.color_cccccc));
        content.setTextSize(dip2px(17));

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(dip2px(427),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        contentParams.bottomMargin = dip2px(40);
        contentParams.gravity = Gravity.CENTER;

        Button cancelBtn = new Button(context);
        cancelBtn.setText(R.string.allCancel);
        cancelBtn.setBackgroundResource(R.drawable.reg_btn_selected);
        cancelBtn.setTextColor(Color.WHITE);
        cancelBtn.setTextSize(dip2px(17));
        cancelBtn.setGravity(Gravity.CENTER);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null)
                    mListener.cancelRegListener();
            }
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(dip2px(160),
                dip2px(50));
        btnParams.bottomMargin = dip2px(35);
        btnParams.gravity = Gravity.CENTER;

        root.addView(titleTv, titleParams);
        root.addView(content, contentParams);
        root.addView(cancelBtn, btnParams);
        return root;
    }

    public void show(String tipString) {
        activationDialog = new Dialog(context);
//        activationDialog.setTitle("修改用户名");
        activationDialog.setContentView(initView(tipString));
        activationDialog.setCancelable(false);
        activationDialog.show();
    }

    public void updateTipContent(String tipString){
        if (content!=null){
            content.setText(tipString);
        }
    }

    private TipDialogCancelListener mListener;

    public void setListener(TipDialogCancelListener pListener) {
        mListener = pListener;
    }

    public interface TipDialogCancelListener{
        void cancelRegListener();
    }

    private int dip2px(int dip) {
        Resources resources = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, dip, resources.getDisplayMetrics());
        return px;
    }

    public void dismiss(String tipString){
        if (activationDialog!=null){
            activationDialog.dismiss();
        }
        Toast.makeText(context, tipString, Toast.LENGTH_LONG).show();
    }

}
