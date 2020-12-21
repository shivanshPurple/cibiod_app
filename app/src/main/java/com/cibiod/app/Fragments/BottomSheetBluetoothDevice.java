package com.cibiod.app.Fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cibiod.app.Activities.BluetoothActivity;
import com.cibiod.app.Adapters.DeviceAdapter;
import com.cibiod.app.Callbacks.RecyclerCallback;
import com.cibiod.app.Objects.BluetoothDeviceObject;
import com.cibiod.app.R;
import com.cibiod.app.Utils.u;
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

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDeviceObject> devices = new ArrayList<>();
    private RecyclerView rv;
    private ProgressView progressView;
    private Context thisContext;

    @Override
    public void onAttach(@NonNull Context context) {
        thisContext = context;
        super.onAttach(context);
    }

    public BottomSheetBluetoothDevice(BluetoothAdapter ba) {
        bluetoothAdapter = ba;
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
        bluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND);
        LocalBroadcastManager.getInstance(thisContext).registerReceiver(receiver,filter);
        thisContext.registerReceiver(receiver, filter);
        progressView.start();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            u.alert();
            String action = intent.getAction();
            if (android.bluetooth.BluetoothDevice.ACTION_FOUND.equals(action)) {
                android.bluetooth.BluetoothDevice device = intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE);
                devices.add(new BluetoothDeviceObject(device.getName(), device.getAddress()));
                updateList();
            }
        }
    };

    private void updateList() {
        DeviceAdapter adapter = new DeviceAdapter(devices, this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onItemClick(int position, View card) {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        SharedPreferences prefs = getActivity().getSharedPreferences("applicationVariables", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("pairedStethoAddress", devices.get(position).getAddress());
        editor.apply();
        ((BluetoothActivity) getActivity()).startBluetoothService();
        this.dismiss();
    }

    @Override
    public void onDestroy() {
        thisContext.unregisterReceiver(receiver);
        super.onDestroy();
    }
}
