package com.zhangqiang.fastdatabase.entity;

public abstract class DBEntity {

    @Index
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
