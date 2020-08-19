package com.cibiod.app;

import java.util.ArrayList;

public class patientDetails {
    private String mName;
    private String mId;
    private String mAddress;
    private String mGender;
    private String mAge;

    public patientDetails(String name, String id, String address, String gender, String age)
    {
        mName = name;
        mId = id;
        mAddress = address;
        mGender = gender;
        mAge = age;
    }

    public patientDetails getPatientDetails()
    {
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

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
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

    public static ArrayList<patientDetails> createTemp(int amount) {
        ArrayList<patientDetails> patients = new ArrayList<patientDetails>();

        for (int i = 1; i <= amount; i++) {
            patients.add(new patientDetails("Person " + amount, Integer.toString(amount), "address", "gender", "20"));
        }

        return patients;
    }
}
