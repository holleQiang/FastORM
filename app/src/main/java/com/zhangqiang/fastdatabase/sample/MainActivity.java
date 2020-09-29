package com.zhangqiang.fastdatabase.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.fastdatabase.dao.Dao;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CellRVAdapter mAdapter = new CellRVAdapter();
        recyclerView.setAdapter(mAdapter);

        Dao<ReadRecordEntity> baseDao = new AppDBOpenHelper(this).getDao(ReadRecordEntity.class);

        long currentTimeMillis = System.currentTimeMillis();
        baseDao.deleteAll();
        Log.i("Test","=====删除耗时=====" + (System.currentTimeMillis() - currentTimeMillis));
        currentTimeMillis = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            ReadRecordEntity recordEntity = new ReadRecordEntity();
            recordEntity.setBookName("卧槽了");
            recordEntity.setChapterIndex(11111);
            recordEntity.setChapterName("第100章");
            baseDao.insert(recordEntity);
        }
        Log.i("Test","=====插入耗时=====" + (System.currentTimeMillis() - currentTimeMillis));
        currentTimeMillis = System.currentTimeMillis();

        ArrayList<Cell> dataList = new ArrayList<>();
        List<ReadRecordEntity> readRecordEntities = baseDao.queryAll();
        int count = readRecordEntities != null ? readRecordEntities.size() : 0;
        Log.i("Test",count + "=====查询耗时=====" + (System.currentTimeMillis() - currentTimeMillis));

        if (readRecordEntities != null) {
            for (int i = readRecordEntities.size() - 1; i >= 0; i--) {
                ReadRecordEntity entity = readRecordEntities.get(i);
                dataList.add(new MultiCell<>(R.layout.item_read_record, entity, new ViewHolderBinder<ReadRecordEntity>() {
                    @Override
                    public void onBind(ViewHolder viewHolder, ReadRecordEntity readRecordEntity) {
                        viewHolder.setText(R.id.tv_book_name,readRecordEntity.getBookName());
                    }
                }));
            }
        }
        mAdapter.setDataList(dataList);
    }
}
