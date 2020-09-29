package com.zhangqiang.fastdatabase.dao;

import android.content.ContentValues;

public class ContentValuesRecord {

    public final ContentValues value = new ContentValues();
    private ContentValuesRecord next;
    private static ContentValuesRecord sPool;
    private static final Object lock = new Object();

    private ContentValuesRecord() {
    }

    public static ContentValuesRecord obtain(){
        synchronized (lock) {
            if (sPool == null) {
                return new ContentValuesRecord();
            }
            ContentValuesRecord temp = sPool;
            sPool = sPool.next;
            return temp;
        }
    }

    public void recycle(){
        synchronized (lock) {
            next = sPool;
            sPool = this;
            value.clear();
        }
    }
}
