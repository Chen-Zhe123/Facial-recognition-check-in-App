package com.firefly.face1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.adapter.PreviewResultAdapter;
import com.firefly.face1.bean.PreviewResult;

import java.util.ArrayList;
import java.util.List;

/*
问题：在进行一次签到事件后，进入该活动后签到结果事件没有更新，只有重启App后才显示更新后的结果
原因：
 */
public class ViewResultsActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private List<PreviewResult> preResultList = new ArrayList<>();
    private static final String TAG = "ViewResultsActivity";
    private PreviewResultAdapter mPreviewAdapter;
    private RecyclerView preRecyclerView;
    private TextView managerButton;
    private LinearLayout bottomBar;
    private TextView deleteButton;
    private TextView cancelButton;
    private CheckBox allSelectCheckBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_results);
        Log.d(TAG, "onCreate: 调用onCreate方法");
        findView();
        init();
    }
    public void init(){
        DBMaster.getInstance().init(this);
        List<PreviewResult> tempList = DBMaster.getInstance().mResultTable.queryResult();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        preRecyclerView.setLayoutManager(layoutManager);
        mPreviewAdapter = new PreviewResultAdapter(this);
        preRecyclerView.setAdapter(mPreviewAdapter);
        //将List逆序，使最新结果呈现在最上面
        for (int pos = tempList.size() - 1; pos >= 0; pos--){
            preResultList.add(tempList.get(pos));
        }
        mPreviewAdapter.setPreResultList(preResultList);
    }
    public void findView(){
        preRecyclerView = findViewById(R.id.preview_view_list);
        managerButton = findViewById(R.id.manager_in_view_result);
        managerButton.setOnClickListener(this);
        bottomBar = findViewById(R.id.bottom_option_in_view_result);
        deleteButton = findViewById(R.id.delete_button_in_view_result);
        deleteButton.setOnClickListener(this);
        cancelButton = findViewById(R.id.cancel_button_in_view_result);
        cancelButton.setOnClickListener(this);
        allSelectCheckBox = findViewById(R.id.all_select_checkBox_in_view_result);
        allSelectCheckBox.setOnCheckedChangeListener(this);
    }

    public void showAllCheckBox(){
        for(PreviewResult preResult:preResultList){
            preResult.setShow(true);
        }
        mPreviewAdapter.setPreResultList(preResultList);
        mPreviewAdapter.notifyDataSetChanged();
    }

    public void hideAllCheckBox(){
        for(PreviewResult preResult:preResultList){
            preResult.setShow(false);
        }
        mPreviewAdapter.setPreResultList(preResultList);
        mPreviewAdapter.notifyDataSetChanged();
    }
    public void deleteEvent(){
        List<PreviewResult> deleteList = new ArrayList<>();
        Boolean success = false;
        int count = 0;//记录删除的数量
        for (PreviewResult preResult:preResultList) {
            if(preResult.getIsSelected()) {
                success = DBMaster.getInstance().mResultTable.deleteEventByTime(preResult.getWorkTime());
                deleteList.add(preResult);
                count++;
            }
        }
        if(success){
            Toast.makeText(ViewResultsActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
        }else{
            if(count > 0) {
                Toast.makeText(ViewResultsActivity.this, "删除失败！", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ViewResultsActivity.this, "无效的删除操作", Toast.LENGTH_SHORT).show();
            }
        }
        preResultList.removeAll(deleteList);
        mPreviewAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.manager_in_view_result:
                showAllCheckBox();
                bottomBar.setVisibility(View.VISIBLE);
                break;
            case R.id.delete_button_in_view_result:
                deleteEvent();
                hideAllCheckBox();
                bottomBar.setVisibility(View.GONE);
                break;
            case R.id.cancel_button_in_view_result:
                hideAllCheckBox();
                bottomBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            for (PreviewResult preResult:preResultList) {
                preResult.setIsSelected(true);
            }
        } else{
            for (PreviewResult preResult:preResultList) {
                preResult.setIsSelected(false);
            }
        }
        mPreviewAdapter.setPreResultList(preResultList);
        mPreviewAdapter.notifyDataSetChanged();
    }
}
