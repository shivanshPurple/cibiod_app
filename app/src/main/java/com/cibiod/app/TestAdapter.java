package com.cibiod.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder>
{
    List<TestObject> tests;
    TestRecyclerCallback callback;

    public TestAdapter(List<TestObject> tempTests, TestRecyclerCallback testRecyclerCallback) {
        tests = tempTests;
        callback = testRecyclerCallback;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.test_recycler, parent, false);
        return new TestViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        TestObject p = tests.get(position);

        TextView textView = holder.dateText;
        textView.setText(p.getDate());
        TextView textView2 = holder.timeText;
        textView2.setText(p.getTime());
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    class TestViewHolder extends RecyclerView.ViewHolder {
        public TextView dateText;
        public TextView timeText;

        public TestViewHolder(View itemView)
        {
            super(itemView);

            dateText = itemView.findViewById(R.id.dateAdapter);
            timeText = itemView.findViewById(R.id.timeAdapter);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.OnTestClickListener(getAdapterPosition());
                }
            });
        }
    }
}