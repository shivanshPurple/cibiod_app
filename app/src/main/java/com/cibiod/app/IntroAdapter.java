package com.cibiod.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.IntroViewHolder>{

    private List<IntroItem> introItems;

    public IntroAdapter(List<IntroItem> introItems){
        this.introItems = introItems;
    }

    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IntroViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.intro_screen_container, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
        holder.setIntroData(introItems.get(position));
    }

    @Override
    public int getItemCount() {
        return introItems.size();
    }

    class IntroViewHolder extends RecyclerView.ViewHolder{
        private TextView textTitle;
        private TextView textDesc;
        private ImageView vector;

        public IntroViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.title);
            textDesc = itemView.findViewById(R.id.desc);
            vector = itemView.findViewById(R.id.vector);
        }

        void setIntroData(IntroItem introItem)
        {
            textTitle.setText(introItem.getTitle());
            textDesc.setText(introItem.getDesc());
            vector.setImageResource(introItem.getImage());
        }
    }
}
