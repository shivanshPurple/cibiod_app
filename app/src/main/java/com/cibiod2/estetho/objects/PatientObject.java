package com.cibiod2.estetho.objects;

import java.io.Serializable;

public class PatientObject implements Serializable {
    private final String mName;
    private final String mId;
    private final String mGender;
    private final String mAge;
    private final String mPhotoUrl;

    public PatientObject(String name, String id, String gender, String age, String photoUrl) {
        mName = name;
        mId = id;
        mGender = gender;
        mAge = age;
        mPhotoUrl = photoUrl;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public String getGender() {
        return mGender;
    }

    public String getAge() {
        return mAge;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

}
