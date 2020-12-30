package com.cibiod.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.cibiod.app.Objects.PatientObject;
import com.cibiod.app.R;
import com.cibiod.app.Callbacks.RecyclerCallback;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.patientViewHolder>
{
    List<PatientObject> patients;
    RecyclerCallback callback;
    Context context;

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

    @Override
    public void onBindViewHolder(@NonNull patientViewHolder holder, int position) {
        PatientObject p = patients.get(position);
        TextView textView = holder.nameTextView;
        String[] split = p.getName().split("\\s+");
        if(split.length>1)
            textView.setText(split[0]+"\n"+split[1]);
        else
            textView.setText(p.getName());
        TextView textView2 = holder.idTextView;
        textView2.setText(p.getId());

        View card = holder.patientCard;

        card.startAnimation(AnimationUtils.loadAnimation(context,R.anim.recycler_anim));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class patientViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView idTextView;
        public ImageView imgAdapter;
        public View patientCard;

        public patientViewHolder(View itemView)
        {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.nameAdapter);
            idTextView = itemView.findViewById(R.id.idAdapter);
            imgAdapter = itemView.findViewById(R.id.imgAdapter);
            patientCard = itemView.findViewById(R.id.patientCard);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onItemClick(getAdapterPosition(), patientCard);
                }
            });
        }
    }
}