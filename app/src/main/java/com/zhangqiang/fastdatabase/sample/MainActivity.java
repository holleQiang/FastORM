package com.zhangqiang.fastdatabase.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.fastdatabase.dao.Dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final CellRVAdapter mAdapter = new CellRVAdapter();
        recyclerView.setAdapter(mAdapter);

        final AppDBOpenHelper dbOpenHelper = new AppDBOpenHelper(this);
        final Dao<ReadRecordEntity> baseDao = dbOpenHelper.getDao(ReadRecordEntity.class);

        long currentTimeMillis = System.currentTimeMillis();
        baseDao.deleteAll();
        Log.i("Test","=====删除耗时=====" + (System.currentTimeMillis() - currentTimeMillis));
        currentTimeMillis = System.currentTimeMillis();

        List<ReadRecordEntity> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ReadRecordEntity recordEntity = new ReadRecordEntity();
            recordEntity.setBookName("卧槽了");
            recordEntity.setChapterIndex(11111);
            recordEntity.setChapterName("第100章");
            recordEntity.setId(String.valueOf(i));
            list.add(recordEntity);
            baseDao.insert(recordEntity);
        }
        Log.i("Test","=====插入耗时=====" + (System.currentTimeMillis() - currentTimeMillis));
        currentTimeMillis = System.currentTimeMillis();

        baseDao.insert(list);

        Log.i("Test","=====插入耗时2=====" + (System.currentTimeMillis() - currentTimeMillis));
        currentTimeMillis = System.currentTimeMillis();

        ArrayList<Cell> dataList = new ArrayList<>();
        List<ReadRecordEntity> readRecordEntities = baseDao.queryAll();
        int count = readRecordEntities != null ? readRecordEntities.size() : 0;
        Log.i("Test",count + "=====查询耗时=====" + (System.currentTimeMillis() - currentTimeMillis));

        if (readRecordEntities != null) {
            for (int i = readRecordEntities.size() - 1; i >= 0; i--) {
                final ReadRecordEntity entity = readRecordEntities.get(i);
                final MultiCell<ReadRecordEntity> cell = new MultiCell<>(R.layout.item_read_record, entity, null);
                cell.setViewHolderBinder( new ViewHolderBinder<ReadRecordEntity>() {
                    @Override
                    public void onBind(ViewHolder viewHolder, final ReadRecordEntity readRecordEntity) {
                        viewHolder.setText(R.id.tv_book_name,readRecordEntity.getBookName());
                        viewHolder.setText(R.id.tv_create_time,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(readRecordEntity.getCreateTime())));
                        viewHolder.setText(R.id.tv_update_time,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(readRecordEntity.getUpdateTime())));
                        viewHolder.setOnClickListener(R.id.bt_update, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                baseDao.save(readRecordEntity);
                                ReadRecordEntity entity1 = baseDao.queryById(readRecordEntity.getId());
                                cell.setData(entity1);
                            }
                        });
                    }
                });
                dataList.add(cell);
            }
        }
        mAdapter.setDataList(dataList);
    }
}
