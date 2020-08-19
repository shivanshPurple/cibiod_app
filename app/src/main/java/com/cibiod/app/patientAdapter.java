package com.cibiod.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class patientAdapter extends RecyclerView.Adapter<patientAdapter.ViewHolder>
{
    private List<patientDetails> patients;

    public patientAdapter(List<patientDetails> tempPatients) {
        patients = tempPatients;
    }

    @NonNull
    @Override
    public patientAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.patient_recycler, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        patientDetails p = patients.get(position);

        TextView textView = holder.nameTextView;
        textView.setText(p.getName());
        TextView textView2 = holder.idTextView;
        textView2.setText("#"+p.getId());
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView idTextView;

        public ViewHolder(View itemView)
        {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nameAdapter);
            idTextView = itemView.findViewById(R.id.idAdapter);
        }
    }
}