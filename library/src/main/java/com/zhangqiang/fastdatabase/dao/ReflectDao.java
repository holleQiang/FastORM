package com.zhangqiang.fastdatabase.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.zhangqiang.fastdatabase.entity.DBEntity;
import com.zhangqiang.fastdatabase.entity.Index;
import com.zhangqiang.fastdatabase.entity.PrimaryKey;
import com.zhangqiang.fastdatabase.entity.Unique;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectDao<E extends DBEntity> extends Dao<E> {
    private static final Map<Class, ReflectHolder> mReflectCache = new HashMap<>();
    private Class<E> tClass;

    public ReflectDao(Class<E> tClass, SQLiteOpenHelper sqLiteOpenHelper) {
        super(tClass.getSimpleName(), sqLiteOpenHelper);
        this.tClass = tClass;
    }

    @Override
    protected List<ColumnEntry> buildPrivateColumnEntries() {
        return buildColumnEntries(tClass);
    }

    @NonNull
    @Override
    protected E toDBEntity(Cursor cursor) {
        E e = null;
        try {
            e =  getConstructor(tClass).newInstance();
            setupObjectWithCursor(e, cursor);
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }
        if (e == null) {
            throw new NullPointerException();
        }
        return e;
    }

    @Override
    protected void fillContentValues(ContentValues contentValues, E entity) {
        fillContentValuesWithObject(contentValues, entity);
    }


    public static List<ColumnEntry> buildColumnEntries(Class tClass) {

        ArrayList<ColumnEntry> columnEntries = new ArrayList<>();
        Class tmpClass = tClass;
        while (tmpClass != null && tmpClass != DBEntity.class) {

            columnEntries.addAll(buildColumnEntriesInternal(tmpClass));
            tmpClass = tmpClass.getSuperclass();
        }
        return columnEntries;
    }

    public static void setupObjectWithCursor(Object object, Cursor cursor) {

        Class tmpClass = object.getClass();
        while (tmpClass != null && tmpClass != DBEntity.class) {

            try {
                setupObjectWithCursor(object, tmpClass, cursor);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            tmpClass = tmpClass.getSuperclass();
        }
    }

    public static void fillContentValuesWithObject(ContentValues contentValues, Object object) {

        Class tmpClass = object.getClass();
        while (tmpClass != null && tmpClass != DBEntity.class) {

            try {
                fillContentValues(object, tmpClass, contentValues);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            tmpClass = tmpClass.getSuperclass();
        }
    }

    public static void fillContentValues(Object object, Class tClass, ContentValues contentValues) throws IllegalAccessException {

        Field[] declaredFields = getDeclaredFields(tClass);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            fillContentValuesWithField(object, field, contentValues);
            field.setAccessible(false);
        }
        Field[] fields = getFields(tClass);
        for (Field field : fields) {
            fillContentValuesWithField(object, field, contentValues);
        }
    }

    private static void fillContentValuesWithField(Object object, Field field, ContentValues contentValues) throws IllegalAccessException {

        Class<?> declaringClass = field.getType();
        if (int.class == declaringClass || Integer.class == declaringClass) {

            Integer value = (Integer) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (long.class == declaringClass || Long.class == declaringClass) {
            Long value = (Long) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (String.class == declaringClass) {
            String value = (String) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (float.class == declaringClass || Float.class == declaringClass) {
            Float value = (Float) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (double.class == declaringClass || Double.class == declaringClass) {
            Double value = (Double) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (char.class == declaringClass || Character.class == declaringClass) {
            String value = (String) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (boolean.class == declaringClass || Boolean.class == declaringClass) {
            Boolean value = (Boolean) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (byte.class == declaringClass || Byte.class == declaringClass) {
            Byte value = (Byte) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else if (short.class == declaringClass || Short.class == declaringClass) {
            Short value = (Short) field.get(object);
            if (value != null) {
                contentValues.put(field.getName(), value);
            }
        } else {
            throw new IllegalArgumentException("not support type:" + declaringClass);
        }

    }

    public static void setupObjectWithCursor(Object object, Class tClass, Cursor cursor) throws IllegalAccessException {

        Field[] declaredFields = getDeclaredFields(tClass);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            setupObjectWithCursor(object, field, cursor);
            field.setAccessible(false);
        }
        Field[] fields = getFields(tClass);
        for (Field field : fields) {
            setupObjectWithCursor(object, field, cursor);
        }
    }

    private static void setupObjectWithCursor(Object object, Field field, Cursor cursor) throws IllegalAccessException {

        int columnIndex = cursor.getColumnIndex(field.getName());
        Object value;
        Class<?> declaringClass = field.getType();
        if (int.class == declaringClass || Integer.class == declaringClass) {
            value = cursor.getInt(columnIndex);
        } else if (long.class == declaringClass || Long.class == declaringClass) {
            value = cursor.getInt(columnIndex);
        } else if (String.class == declaringClass) {
            value = cursor.getString(columnIndex);
        } else if (float.class == declaringClass || Float.class == declaringClass) {
            value = cursor.getFloat(columnIndex);
        } else if (double.class == declaringClass || Double.class == declaringClass) {
            value = cursor.getDouble(columnIndex);
        } else if (char.class == declaringClass || Character.class == declaringClass) {
            value = cursor.getString(columnIndex);
        } else if (boolean.class == declaringClass || Boolean.class == declaringClass) {
            value = cursor.getInt(columnIndex);
        } else if (byte.class == declaringClass || Byte.class == declaringClass) {
            value = cursor.getBlob(columnIndex);
        } else if (short.class == declaringClass || Short.class == declaringClass) {
            value = cursor.getInt(columnIndex);
        } else {
            throw new IllegalArgumentException("not support type:" + declaringClass);
        }
        field.set(object, value);
    }

    public static List<ColumnEntry> buildColumnEntriesInternal(Class tClass) {

        ArrayList<ColumnEntry> columnEntries = new ArrayList<>();
        Field[] declaredFields = getDeclaredFields(tClass);
        for (Field field : declaredFields) {
            field.setAccessible(true);
            columnEntries.add(buildColumnEntry(field));
            field.setAccessible(false);
        }
        Field[] fields = getFields(tClass);
        for (Field field : fields) {
            columnEntries.add(buildColumnEntry(field));
        }
        return columnEntries;
    }

    private static ColumnEntry buildColumnEntry(Field field) {

        Class<?> declaringClass = field.getType();
        ColumnType type;
        if (int.class == declaringClass || Integer.class == declaringClass) {
            type = ColumnType.INTEGER;
        } else if (long.class == declaringClass || Long.class == declaringClass) {
            type = ColumnType.INTEGER;
        } else if (String.class == declaringClass) {
            type = ColumnType.TEXT;
        } else if (float.class == declaringClass || Float.class == declaringClass) {
            type = ColumnType.REAL;
        } else if (double.class == declaringClass || Double.class == declaringClass) {
            type = ColumnType.REAL;
        } else if (char.class == declaringClass || Character.class == declaringClass) {
            type = ColumnType.TEXT;
        } else if (boolean.class == declaringClass || Boolean.class == declaringClass) {
            type = ColumnType.INTEGER;
        } else if (byte.class == declaringClass || Byte.class == declaringClass) {
            type = ColumnType.BLOB;
        } else if (short.class == declaringClass || Short.class == declaringClass) {
            type = ColumnType.INTEGER;
        } else {
            throw new IllegalArgumentException("not support type:" + declaringClass);
        }
        String fieldName = field.getName();
        Index index = field.getAnnotation(Index.class);
        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        Unique unique = field.getAnnotation(Unique.class);
        return new ColumnEntry().setName(fieldName)
                .setType(type)
                .setAutoIncrement(primaryKey != null && primaryKey.autoIncrement())
                .setPrimaryKey(primaryKey != null)
                .setUnique(unique != null)
                .setIndex(index != null);
    }

    private static Field[] getDeclaredFields(Class tClass) {

        ReflectHolder reflectHolder = getReflectHolder(tClass);
        if (reflectHolder.declaredFields == null) {
            reflectHolder.declaredFields = tClass.getDeclaredFields();
        }
        return reflectHolder.declaredFields;
    }

    private static Field[] getFields(Class tClass) {

        ReflectHolder reflectHolder = getReflectHolder(tClass);
        if (reflectHolder.fields == null) {
            reflectHolder.fields = tClass.getFields();
        }
        return reflectHolder.fields;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getConstructor(Class<T> tClass) throws NoSuchMethodException {
        ReflectHolder reflectHolder = getReflectHolder(tClass);
        if (reflectHolder.constructor == null) {
            reflectHolder.constructor = tClass.getConstructor();
        }
        return reflectHolder.constructor;
    }

    @NonNull
    private static ReflectHolder getReflectHolder(Class tClass) {
        ReflectHolder reflectHolder = mReflectCache.get(tClass);
        if (reflectHolder == null) {
            reflectHolder = new ReflectHolder();
            mReflectCache.put(tClass, reflectHolder);
        }
        return reflectHolder;
    }

    private static class ReflectHolder {

        Field[] declaredFields;
        Field[] fields;
        Constructor constructor;
    }
}
