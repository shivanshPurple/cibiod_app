package com.cibiod2.estetho.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.cibiod2.estetho.R;
import com.cibiod2.estetho.callbacks.RecyclerCallback;
import com.cibiod2.estetho.objects.PatientObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.patientViewHolder> {
    final List<PatientObject> patients;
    final RecyclerCallback callback;
    final Context context;

    public PatientAdapter(Context c, List<PatientObject> tempPatients, RecyclerCallback patientRecyclerCallback) {
        patients = tempPatients;
        callback = patientRecyclerCallback;
        context = c;
    }

    @NonNull
    @Override
    public patientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.recycler_patient, parent, false);

        return new patientViewHolder(contactView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull patientViewHolder holder, int position) {
        PatientObject p = patients.get(position);
        TextView textView = holder.nameTextView;
        String[] split = p.getName().split("\\s+");
        if (split.length > 1)
            textView.setText(split[0] + "\n" + split[1]);
        else
            textView.setText(p.getName());
        TextView textView2 = holder.idTextView;
        textView2.setText(p.getId());

        View card = holder.patientCard;

        card.startAnimation(AnimationUtils.loadAnimation(context, R.anim.recycler_anim));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class patientViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTextView;
        public final TextView idTextView;
        public final View patientCard;

        public patientViewHolder(View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nameAdapter);
            idTextView = itemView.findViewById(R.id.idAdapter);
            patientCard = itemView.findViewById(R.id.patientCard);

            itemView.setOnClickListener(v -> callback.onItemClick(getAdapterPosition(), patientCard));
        }
    }
}