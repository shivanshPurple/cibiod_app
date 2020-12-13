package com.cibiod.app;

import java.io.Serializable;

public class TestObject implements Serializable {
    private String mId;
    private String mDate;
    private String mTime;

    private String mDataLocal;

    private String mDataCloud;
    public TestObject(String id, String date, String time, String dataUrl, boolean isLocal)
    {
        mId = id;
        mDate = date;
        mTime = time;
        if(isLocal)
        {
            mDataLocal = dataUrl;
        }
        else
        {
            mDataCloud = dataUrl;
        }
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String getDataLocal() {
        return mDataLocal;
    }

    public void setDataLocal(String mDataLocal) {
        this.mDataLocal = mDataLocal;
    }

    public String getDataCloud() {
        return mDataCloud;
    }

    public void setDataCloud(String mDataCloud) {
        this.mDataCloud = mDataCloud;
    }
}
