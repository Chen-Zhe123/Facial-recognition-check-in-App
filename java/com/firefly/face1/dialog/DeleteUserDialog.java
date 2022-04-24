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

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.R;
import com.firefly.face1.bean.UserInfo;

import java.util.ArrayList;

public class DeleteUserDialog {

    private Button deleteBtn;
    private Button backBtn;
    private Context context;
    private Dialog deleteDialog;

    private ArrayList<UserInfo> deleteList;
    private ArrayList<String> deleteIdList ;

    public DeleteUserDialog(Context pContext) {
        context = pContext;
    }

    private LinearLayout initView(final ArrayList<UserInfo> user){
        deleteList = user;
        deleteIdList = new ArrayList<>();
        for(UserInfo userInfo : user){
            deleteIdList.add(userInfo.getUserId());
        }
        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootParams.gravity = Gravity.CENTER;
        root.setBackgroundColor(context.getResources().getColor(R.color.color_181820 ));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        TextView titleTv = new TextView(context);
        titleTv.setText(R.string.deleteUserTitle);
        titleTv.setGravity(Gravity.CENTER);
        titleTv.setTextColor(context.getResources().getColor(R.color.color_cccccc));
        titleTv.setTextSize(dip2px(22));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                dip2px(427),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER;
        titleParams.topMargin = dip2px(35);
        titleParams.bottomMargin = dip2px(40);

        TextView content = new TextView(context);
        content.setText(R.string.delete_user_tip);
        content.setGravity(Gravity.CENTER);
        content.setTextColor(context.getResources().getColor(R.color.color_cccccc));
        content.setTextSize(dip2px(17));

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(dip2px(427),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        contentParams.bottomMargin = dip2px(40);
        contentParams.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams activateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dip2px(50));
        activateParams.gravity = Gravity.CENTER;

        deleteBtn = new Button(context);
        deleteBtn.setText(R.string.ok);
        deleteBtn.setBackgroundResource(R.drawable.reg_btn_bg);
        deleteBtn.setTextColor(Color.WHITE);
        deleteBtn.setTextSize(dip2px(17));
        deleteBtn.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams backBtnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dip2px(50));
        backBtnParams.leftMargin = dip2px(30);
        backBtnParams.gravity = Gravity.CENTER;

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

        btnLayout.addView(deleteBtn, activateParams);
        btnLayout.addView(backBtn, backBtnParams);


        root.addView(titleTv, titleParams);
        root.addView(content, contentParams);
        root.addView(btnLayout, btnLayoutParams);
        return root;
    }

    public void show(final ArrayList<UserInfo> deleteList) {
        deleteDialog = new Dialog(context);
        deleteDialog.setContentView(initView(deleteList));
        deleteDialog.setCancelable(false);
        deleteDialog.show();
        addListener();
    }


    private void addListener() {
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeleteUserListener!=null)
                    mDeleteUserListener.deleteUserListen(deleteList);
                //group表中的id也要同步删除
                for(String id : deleteIdList){
                    DBMaster.getInstance().mGroupTable.deleteRowById(id);
                }
                if (deleteDialog != null) {
                    deleteDialog.dismiss();
                }

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteDialog != null) {
                    deleteDialog.dismiss();
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

    private DeleteUserListener mDeleteUserListener;

    public void setDeleteUserListener(DeleteUserListener pDeleteUserListener) {
        mDeleteUserListener = pDeleteUserListener;
    }

    public interface DeleteUserListener{
        void deleteUserListen(ArrayList<UserInfo> userInfoList);
    }


}
