package com.cibiod.app;

import java.io.Serializable;
import java.util.ArrayList;

public class PatientObject implements Serializable {
    private String mName;
    private String mId;
    private String mGender;
    private String mAge;

    public PatientObject(String name, String id, String gender, String age)
    {
        mName = name;
        mId = id;
        mGender = gender;
        mAge = age;
    }

    public PatientObject getPatientDetails()
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

    public static ArrayList<PatientObject> createTemp(int amount) {
        ArrayList<PatientObject> patients = new ArrayList<PatientObject>();

        for (int i = 1; i <= amount; i++) {
            patients.add(new PatientObject("Person " + amount, Integer.toString(amount), "gender", "20"));
        }

        return patients;
    }
}
