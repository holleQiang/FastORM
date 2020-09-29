package com.zhangqiang.fastdatabase;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.fastdatabase.dao.Dao;
import com.zhangqiang.fastdatabase.dao.ReflectDao;
import com.zhangqiang.fastdatabase.entity.DBEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DBOpenHelper extends SQLiteOpenHelper {

    private final Map<Class<? extends DBEntity>, Dao> daoMap = new HashMap<>();

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        init();
    }

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        init();
    }

    @TargetApi(Build.VERSION_CODES.P)
    public DBOpenHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
        init();
    }

    @SuppressWarnings("unchecked")
    public <E extends DBEntity> Dao<E> getDao(Class<E> entityClass) {
        return daoMap.get(entityClass);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Map.Entry<Class<? extends DBEntity>, Dao> entry : daoMap.entrySet()) {
            Dao dao = entry.getValue();
            db.execSQL(dao.makeTableCreateSql());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for (Map.Entry<Class<? extends DBEntity>, Dao> entry : daoMap.entrySet()) {
            Dao dao = entry.getValue();
            db.execSQL("drop table if exists " + dao.getTableName());
        }
        onCreate(db);
    }

    protected abstract void onRegisterEntity(List<Class<? extends DBEntity>> entityClasses);

    private void init() {
        ArrayList<Class<? extends DBEntity>> entityClasses = new ArrayList<>();
        onRegisterEntity(entityClasses);
        for (Class<? extends DBEntity> entityClass : entityClasses) {
            daoMap.put(entityClass, new ReflectDao<>(entityClass, this));
        }
    }
}
