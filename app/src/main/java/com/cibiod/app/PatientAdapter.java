package com.cibiod.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.patientViewHolder>
{
    List<PatientObject> patients;
    PatientRecyclerCallback callback;

    public PatientAdapter(List<PatientObject> tempPatients, PatientRecyclerCallback patientRecyclerCallback) {
        patients = tempPatients;
        callback = patientRecyclerCallback;
    }

    @NonNull
    @Override
    public patientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.patient_recycler, parent, false);

        return new patientViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull patientViewHolder holder, int position) {
        PatientObject p = patients.get(position);

        TextView textView = holder.nameTextView;
        textView.setText(p.getName());
        TextView textView2 = holder.idTextView;
        textView2.setText("#"+p.getId());
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class patientViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView idTextView;
        public ImageView imgAdapter;
        public ConstraintLayout patientListItem;

        public patientViewHolder(View itemView)
        {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nameAdapter);
            idTextView = itemView.findViewById(R.id.idAdapter);
            imgAdapter = itemView.findViewById(R.id.imgAdapter);
            patientListItem = itemView.findViewById(R.id.patientListItem);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.OnPatientClickListener(getAdapterPosition());
                }
            });
        }
    }
}