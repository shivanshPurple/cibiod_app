package com.cibiod.app.Objects;

import java.io.Serializable;

public class PatientObject implements Serializable {
    private String mName;
    private String mId;
    private String mGender;
    private String mAge;
    private String mPhotoUrl;

    public PatientObject(String name, String id, String gender, String age, String photoUrl) {
        mName = name;
        mId = id;
        mGender = gender;
        mAge = age;
        mPhotoUrl = photoUrl;
    }

    public PatientObject getPatientDetails() {
        return this;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String mGender) {
        this.mGender = mGender;
    }

    public String getAge() {
        return mAge;
    }

    public void setAge(String mAge) {
        this.mAge = mAge;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setmPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }

}
