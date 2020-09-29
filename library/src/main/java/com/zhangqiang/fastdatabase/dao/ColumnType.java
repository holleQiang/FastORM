package com.zhangqiang.fastdatabase.dao;

public enum ColumnType {

    INTEGER("INTEGER"),
    REAL("REAL"),
    BLOB("BLOB"),
    NULL("NULL"),
    TEXT("TEXT");

    private String value;

    ColumnType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
