package com.firefly.face1;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firefly.face1.DataBase.DBMaster;
import com.firefly.face1.adapter.AllRecordAdapter;
import com.firefly.face1.bean.AllRecord;

import java.util.ArrayList;
import java.util.List;
/*
目前的想法是只对已注册用户进行人脸捕获事件的记录，这样就不用存储人脸图片，节省内存空间，
带来的问题是查询记录时，要通过文件查找相应的人脸图，以及通过feature表查询用户的附加信息，耗费大量时间。
 */
public class HistoricalRecordActivity extends AppCompatActivity {

    private List<AllRecord> allRecordList = new ArrayList<>();
    private List<AllRecord> allRecordList1 = new ArrayList<>();
    private TextView noRecord;
    private TextView itemCount;
    private ImageView cleanAllButton;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historical_record);
        mContext = this.getApplicationContext();
        DBMaster.getInstance().init(mContext);
        itemCount = findViewById(R.id.record_count);
        cleanAllButton = findViewById(R.id.clean_all_record);
        cleanAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DBMaster.getInstance().mRecordTable.cleanAllRecord()){
                    Toast.makeText(mContext,"删除成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(mContext,"删除失败！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //在子线程中进行耗时操作//加载数据用了将近10秒，需要优化
        new Thread(new Runnable() {
            @Override
            public void run() {
                noRecord = findViewById(R.id.no_record);
                allRecordList = DBMaster.getInstance().mRecordTable.queryAllRecord();
                if(allRecordList.size() > 0) {
                    for (int pos = allRecordList.size() - 1; pos >= 0; pos--)
                        allRecordList1.add(allRecordList.get(pos));
                }
                final AllRecordAdapter recordAdapter = new AllRecordAdapter(HistoricalRecordActivity.this,
                        R.layout.historical_record_item, allRecordList1);
                final ListView listView = findViewById(R.id.historical_record_list_view);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(allRecordList.size() <= 0){
                            noRecord.setVisibility(View.VISIBLE);
                        }else {
                            listView.setAdapter(recordAdapter);
                        }
                        itemCount.setText("共有"+allRecordList.size()+"条记录");
                    }
                });

            }
        }).start();

//        initHistoricalRecord();
    }

    private void initHistoricalRecord() {
        noRecord = findViewById(R.id.no_record);
        allRecordList = DBMaster.getInstance().mRecordTable.queryAllRecord();
        if(allRecordList.size() <= 0){
            noRecord.setVisibility(View.VISIBLE);
        }
        else {
            for (int pos = allRecordList.size() - 1; pos >= 0; pos--)
                allRecordList1.add(allRecordList.get(pos));
        }
    }



}