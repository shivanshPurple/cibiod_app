package com.cibiod.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cibiod.app.Objects.BluetoothDeviceObject;
import com.cibiod.app.R;
import com.cibiod.app.Callbacks.RecyclerCallback;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>
{
    private final List<BluetoothDeviceObject> devices;
    private final RecyclerCallback mRecyclerCallback;

    public DeviceAdapter(List<BluetoothDeviceObject> tempDevices, RecyclerCallback recyclerCallbackVar) {
        devices = tempDevices;
        mRecyclerCallback = recyclerCallbackVar;
    }

    @NonNull
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.recycler_device, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.ViewHolder holder, int position) {
        BluetoothDeviceObject p = devices.get(position);

        TextView textView = holder.nameTextView;
        textView.setText(p.getName());
        TextView textView2 = holder.idTextView;
        textView2.setText(p.getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView idTextView;

        public ViewHolder(View itemView)
        {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.btDeviceName);
            idTextView = itemView.findViewById(R.id.btDeviceAddress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRecyclerCallback.onItemClick(getAdapterPosition(),null);
                }
            });
        }
    }
}
