package com.cibiod2.estetho.objects;

public class BluetoothDeviceObject {
    private final String mName;
    private final String mAddress;

    public BluetoothDeviceObject(String name, String address) {
        mName = name;
        mAddress = address;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }
}
