package com.cibiod.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class btDeviceAdapter extends RecyclerView.Adapter<btDeviceAdapter.ViewHolder>
{
    private List<btDevice> devices;
    private recyclerClickInterface mRecyclerClickInterface;

    public btDeviceAdapter(List<btDevice> tempDevices, recyclerClickInterface recyclerClickInterfaceVar) {
        devices = tempDevices;
        mRecyclerClickInterface = recyclerClickInterfaceVar;
    }

    @NonNull
    @Override
    public btDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.bluetooth_device_layout, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull btDeviceAdapter.ViewHolder holder, int position) {
        btDevice p = devices.get(position);

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
                    try {
                        mRecyclerClickInterface.OnItemClick(getAdapterPosition());
                    } catch (IOException e) {
                        Log.d("customm","socket connection failed");
                    }
                }
            });
        }
    }
}
