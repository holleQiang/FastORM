package com.zhangqiang.fastdatabase.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.fastdatabase.entity.DBEntity;
import com.zhangqiang.fastdatabase.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Dao<E extends DBEntity> {

    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_CREATE_TIME = "_create_time";
    public static final String COLUMN_NAME_UPDATE_TIME = "_update_time";
    private String mTableName;
    private SQLiteOpenHelper mSqLiteOpenHelper;

    public Dao(String tableName, SQLiteOpenHelper sqLiteOpenHelper) {
        this.mTableName = tableName;
        this.mSqLiteOpenHelper = sqLiteOpenHelper;
    }

    protected abstract List<ColumnEntry> buildPrivateColumnEntries();

    @NonNull
    protected abstract E toDBEntity(Cursor cursor);

    protected abstract void fillContentValues(ContentValues contentValues, E entity);

    @Nullable
    private SQLiteDatabase getWriteDatabase() {
        try {

            return mSqLiteOpenHelper.getWritableDatabase();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private SQLiteDatabase getReadDatabase() {
        try {

            return mSqLiteOpenHelper.getReadableDatabase();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(E entity) {

        if (!update(entity)) {
            return insert(entity) != -1;
        }
        return true;
    }

    public boolean delete(E entity) {

        String uniqueId = checkUniqueId(entity);

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return false;
        }

        String tableName = getTableName();
        int effectRows = database.delete(tableName, COLUMN_NAME_ID + "=?", new String[]{uniqueId});
        return effectRows > 0;
    }

    public boolean update(E entity) {

        String uniqueId = checkUniqueId(entity);

        return update(entity, COLUMN_NAME_ID + "=?", new String[]{uniqueId}) > 0;
    }

    public int update(E entity, String whereClause, String[] args) {

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return 0;
        }

        ContentValuesRecord record = ContentValuesRecord.obtain();
        ContentValues contentValues = fillContentValuesInternal(record.value, entity);

        int updateSize = database.update(getTableName(), contentValues, whereClause, args);
        record.recycle();
        return updateSize;
    }

    @NonNull
    private ContentValues fillContentValuesInternal(ContentValues contentValues, E entity) {

        fillContentValues(contentValues, entity);
        contentValues.put(COLUMN_NAME_ID, entity.getId());
        contentValues.put(COLUMN_NAME_UPDATE_TIME, System.currentTimeMillis());
        return contentValues;
    }

    public long insert(E entity) {

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return -1;
        }

        ContentValuesRecord record = ContentValuesRecord.obtain();
        ContentValues contentValues = fillContentValuesInternal(record.value, entity);
        contentValues.put(COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());
        long rowId = database.insert(getTableName(), null, contentValues);
        record.recycle();
        return rowId;
    }

    public boolean insert(List<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return false;
        }
        try {
            database.beginTransaction();

            for (int i = 0; i < entities.size(); i++) {
                E entity = entities.get(i);

                ContentValuesRecord record = ContentValuesRecord.obtain();
                ContentValues contentValues = fillContentValuesInternal(record.value, entity);
                contentValues.put(COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());
                long rowId = database.insert(getTableName(), null, contentValues);
                record.recycle();
                if (rowId == -1) {
                    return false;
                }
            }
            database.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        return false;
    }

    public E queryById(String id) {

        List<E> list = query(null, COLUMN_NAME_ID + "=?", new String[]{id}, null, null, null, null);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<E> queryAll() {

        return query(null, null, null, null, null, null, null);
    }

    public List<E> query(String[] columns, String selection, String[] args, String groupBy,
                         String having, String orderBy, String limit) {

        SQLiteDatabase database = getReadDatabase();
        if (database == null) {
            return null;
        }
        String tableName = getTableName();
        Cursor cursor = null;
        try {

            cursor = database.query(tableName, columns, selection, args, groupBy, having, orderBy, limit);
            List<E> entityList = null;
            while (cursor.moveToNext()) {

                E dbEntity = toDBEntityInternal(cursor);
                if (entityList == null) {
                    entityList = new ArrayList<>();
                }
                entityList.add(dbEntity);
            }
            return entityList;
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private E toDBEntityInternal(Cursor cursor) {
        E entity = toDBEntity(cursor);
        entity.setId(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ID)));
        entity.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_CREATE_TIME)));
        entity.setUpdateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_NAME_UPDATE_TIME)));
        return entity;
    }


    public String getTableName() {
        return mTableName;
    }

    private static <E extends DBEntity> String checkUniqueId(E entity) {

        if (entity == null) {
            throw new NullPointerException("entity cannot be null");
        }
        String uniqueId = entity.getId();
        if (StringUtils.isEmpty(uniqueId)) {
            throw new IllegalArgumentException("uniqueId of entity:" + entity + " cannot be null");
        }
        return uniqueId;
    }

    public boolean exists(E entity) {

        String uniqueId = checkUniqueId(entity);

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return false;
        }
        String tableName = getTableName();
        Cursor cursor = null;
        try {

            cursor = database.query(tableName,
                    new String[]{COLUMN_NAME_ID},
                    COLUMN_NAME_ID + "= ?",
                    new String[]{uniqueId},
                    null,
                    null,
                    null);
            if (cursor == null) {
                return false;
            }
            if (cursor.moveToNext()) {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public String makeTableCreateSql() {

        List<ColumnEntry> mColumnEntries = buildCommonColumnEntries();
        List<ColumnEntry> privateColumnEntries = buildPrivateColumnEntries();
        if (privateColumnEntries != null) {
            for (int i = 0; i < privateColumnEntries.size(); i++) {
                if (COLUMN_NAME_ID.equals(privateColumnEntries.get(i).getName())) {
                    throw new IllegalArgumentException("duplicate column name for " + COLUMN_NAME_ID);
                }
            }
            mColumnEntries.addAll(privateColumnEntries);
        }

        StringBuilder sqlBuilder = new StringBuilder("create table if not exists " + getTableName() + "(");

        int entryCount = mColumnEntries.size();
        for (int i = 0; i < entryCount; i++) {
            ColumnEntry entry = mColumnEntries.get(i);
            sqlBuilder.append(entry.getName()).append(" ")
                    .append(entry.getType().getValue()).append(" ");
            if (entry.isPrimaryKey()) {
                sqlBuilder.append("primary key ");
            } else if (entry.isUnique()) {
                sqlBuilder.append("unique ");
            } else if (entry.isIndex()) {
                sqlBuilder.append("index ");
            }
            if (entry.isAutoIncrement()) {
                sqlBuilder.append("autoincrement ");
            }
            if (i != entryCount - 1) {
                sqlBuilder.append(",");
            }
        }

        sqlBuilder.append(");");
        return sqlBuilder.toString();
    }

    public E queryByRowId(long rowId) {

        List<E> list = query(null,
                "rowid = ?", new String[]{rowId + ""},
                null,
                null,
                null,
                null);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private static ColumnEntry buildIDEntry() {
        ColumnEntry entry = new ColumnEntry();
        entry.setUnique(true);
        entry.setName(COLUMN_NAME_ID);
        entry.setType(ColumnType.TEXT);
        entry.setIndex(true);
        return entry;
    }

    private static ColumnEntry buildCreateTimeEntry() {
        ColumnEntry entry = new ColumnEntry();
        entry.setName(COLUMN_NAME_CREATE_TIME);
        entry.setType(ColumnType.INTEGER);
        return entry;
    }

    private static ColumnEntry buildUpdateTimeEntry() {
        ColumnEntry entry = new ColumnEntry();
        entry.setName(COLUMN_NAME_UPDATE_TIME);
        entry.setType(ColumnType.INTEGER);
        return entry;
    }

    private List<ColumnEntry> buildCommonColumnEntries() {
        ArrayList<ColumnEntry> columnEntries = new ArrayList<>();

        columnEntries.add(buildIDEntry());
        columnEntries.add(buildCreateTimeEntry());
        columnEntries.add(buildUpdateTimeEntry());
        return columnEntries;
    }

    public void deleteAll() {
        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return;
        }
        database.delete(getTableName(), null, null);
    }
}
