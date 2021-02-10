package com.cibiod2.estetho.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cibiod2.estetho.activities.BluetoothActivity;
import com.cibiod2.estetho.activities.SettingsActivity;
import com.cibiod2.estetho.adapters.DeviceAdapter;
import com.cibiod2.estetho.callbacks.RecyclerCallback;
import com.cibiod2.estetho.objects.BluetoothDeviceObject;
import com.cibiod2.estetho.R;
import com.cibiod2.estetho.utils.u;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BottomSheetBluetoothDevice extends BottomSheetDialogFragment implements RecyclerCallback {

    private final BluetoothAdapter bluetoothAdapter;
    private final ArrayList<BluetoothDeviceObject> devices = new ArrayList<>();
    private RecyclerView rv;
    private ProgressView progressView;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                progressView.start();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                bluetoothAdapter.startDiscovery();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                android.bluetooth.BluetoothDevice device = intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);
                boolean already = false;
                for (BluetoothDeviceObject d : devices)
                    if (device != null && d.getAddress().equals(device.getAddress())) {
                        already = true;
                        break;
                    }
                if (!already)
                    if (device != null) {
                        devices.add(new BluetoothDeviceObject(device.getName(), device.getAddress()));
                    }
                updateList();
            }
        }
    };
    private Context thisContext;
    private boolean fromSettings = false;

    public BottomSheetBluetoothDevice(BluetoothAdapter ba, boolean b) {
        bluetoothAdapter = ba;
        fromSettings = b;
    }

    public BottomSheetBluetoothDevice(BluetoothAdapter ba) {
        bluetoothAdapter = ba;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        thisContext = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_bluetooth_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv = view.findViewById(R.id.deviceRecyclerView);
        progressView = view.findViewById(R.id.deviceSearchProgress);
        showBondedDevices();
        searchDevices();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        super.onCreate(savedInstanceState);
    }

    private void showBondedDevices() {
        Set<android.bluetooth.BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        if (paired.size() > 0) {
            for (android.bluetooth.BluetoothDevice device : paired) {
                devices.add(new BluetoothDeviceObject(device.getName(), device.getAddress()));
            }
            updateList();
        }
    }

    private void searchDevices() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        LocalBroadcastManager.getInstance(thisContext).registerReceiver(receiver, filter);
        thisContext.registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    private void updateList() {
        DeviceAdapter adapter = new DeviceAdapter(devices, this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onItemClick(int position, View card) {
        u.setPref(thisContext, "pairedStethoAddress", devices.get(position).getAddress());
        u.setPref(thisContext, "pairedStethoName", devices.get(position).getName());
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        if (!fromSettings) {
            ((BluetoothActivity) requireActivity()).startBluetoothService();
        } else {
            TextView tv = ((SettingsActivity) requireActivity()).findViewById(R.id.deviceName);
            tv.setText(devices.get(position).getName());
        }

        this.dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
        if (!fromSettings)
            ((BluetoothActivity) requireActivity()).startBluetoothService();
        super.onCancel(dialog);
    }

    @Override
    public void onDestroy() {
        thisContext.unregisterReceiver(receiver);
        super.onDestroy();
    }
}
