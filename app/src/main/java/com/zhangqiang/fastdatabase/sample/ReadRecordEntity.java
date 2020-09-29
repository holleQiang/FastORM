package com.zhangqiang.fastdatabase.sample;

import com.zhangqiang.fastdatabase.entity.DBEntity;

public class ReadRecordEntity extends DBEntity {

    private String bookName;
    private int chapterIndex;
    private String chapterName;

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }
}
