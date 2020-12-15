package com.cibiod.app.Objects;

import java.util.ArrayList;

public class BluetoothDeviceObject {
    private String mName;
    private String mAddress;

    public BluetoothDeviceObject(String name, String address)
    {
        mName = name;
        mAddress = address;
    }

    public BluetoothDeviceObject getBtDevice()
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

    public static ArrayList<BluetoothDeviceObject> createTemp(int amount) {
        ArrayList<BluetoothDeviceObject> patients = new ArrayList<BluetoothDeviceObject>();

        for (int i = 1; i <= amount; i++) {
            patients.add(new BluetoothDeviceObject("Person " + i, Integer.toString(i)));
        }

        return patients;
    }
}
