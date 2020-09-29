package com.zhangqiang.fastdatabase.sample;

import android.content.Context;
import android.support.annotation.Nullable;

import com.zhangqiang.fastdatabase.DBOpenHelper;
import com.zhangqiang.fastdatabase.entity.DBEntity;

import java.util.List;

public class AppDBOpenHelper extends DBOpenHelper {


    public AppDBOpenHelper(@Nullable Context context) {
        super(context, "sample", null, 1);
    }

    @Override
    protected void onRegisterEntity(List<Class<? extends DBEntity>> entityClasses) {
        entityClasses.add(ReadRecordEntity.class);
    }
}
