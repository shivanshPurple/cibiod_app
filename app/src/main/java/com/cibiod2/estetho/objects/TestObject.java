package com.cibiod2.estetho.objects;

import java.io.Serializable;

public class TestObject implements Serializable {
    private final String mId;
    private final String mDate;
    private final String mTime;

    private String mDataCloud;

    public TestObject(String id, String date, String time, String dataUrl, boolean isLocal) {
        mId = id;
        mDate = date;
        mTime = time;
        if (!isLocal) mDataCloud = dataUrl;
    }

    public String getId() {
        return mId;
    }

    public String getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }

    public String getDataCloud() {
        return mDataCloud;
    }

}
