package com.cibiod.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.cibiod.app.Objects.TestObject;
import com.cibiod.app.R;
import com.cibiod.app.Callbacks.RecyclerCallback;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder>
{
    List<TestObject> tests;
    RecyclerCallback callback;
    Context context;

    public TestAdapter(Context c, List<TestObject> tempTests, RecyclerCallback testRecyclerCallback) {
        tests = tempTests;
        callback = testRecyclerCallback;
        context = c;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.recycler_test, parent, false);
        return new TestViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        TestObject p = tests.get(position);

        TextView textView = holder.dateText;
        textView.setText(p.getDate());
        TextView textView2 = holder.timeText;
        textView2.setText(p.getTime());

        View card = holder.containerCard;

        card.setAnimation(AnimationUtils.loadAnimation(context,R.anim.recycler_anim));
    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    class TestViewHolder extends RecyclerView.ViewHolder {
        public TextView dateText;
        public TextView timeText;
        public View containerCard;

        public TestViewHolder(View itemView)
        {
            super(itemView);

            dateText = itemView.findViewById(R.id.dateAdapter);
            timeText = itemView.findViewById(R.id.timeAdapter);
            containerCard = itemView.findViewById(R.id.testCard);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onItemClick(getAdapterPosition(),containerCard);
                }
            });
        }
    }
}