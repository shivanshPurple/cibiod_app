package com.cibiod.app;

import java.util.ArrayList;

public class btDevice {
    private String mName;
    private String mAddress;

    public btDevice(String name, String address)
    {
        mName = name;
        mAddress = address;
    }

    public btDevice getBtDevice()
    {
        return this;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public static ArrayList<btDevice> createTemp(int amount) {
        ArrayList<btDevice> patients = new ArrayList<btDevice>();

        for (int i = 1; i <= amount; i++) {
            patients.add(new btDevice("Person " + i, Integer.toString(i)));
        }

        return patients;
    }
}
