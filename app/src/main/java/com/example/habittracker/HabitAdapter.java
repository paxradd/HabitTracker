package com.example.habittracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittracker.data.Habit;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public interface OnCompleteListener {
        void onComplete(HabitWithProgress item);
    }

    public interface OnItemClickListener {
        void onItemClick(HabitWithProgress item);
    }

    private List<HabitWithProgress> items;
    private OnCompleteListener completeListener;
    private OnItemClickListener itemClickListener;

    public HabitAdapter(List<HabitWithProgress> items,
                        OnCompleteListener completeListener,
                        OnItemClickListener itemClickListener) {
        this.items = items;
        this.completeListener = completeListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        HabitWithProgress item = items.get(position);
        holder.bind(item, completeListener, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void updateList(List<HabitWithProgress> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvProgress, tvTimeOfDay;
        Button btnComplete;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvTimeOfDay = itemView.findViewById(R.id.tvTimeOfDay);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }

        public void bind(HabitWithProgress item,
                         OnCompleteListener completeListener,
                         OnItemClickListener itemClickListener) {
            Habit habit = item.habit;
            tvName.setText(habit.name);
            if (habit.description != null && !habit.description.isEmpty()) {
                tvDescription.setText(habit.description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            tvProgress.setText(item.completedToday + " / " + habit.targetCount);
            tvTimeOfDay.setText(habit.timeOfDay);

            if (item.completedToday >= habit.targetCount) {
                btnComplete.setEnabled(false);
                btnComplete.setText("Выполнено");
                btnComplete.setBackgroundColor(Color.parseColor("#9E9E9E"));
            } else {
                btnComplete.setEnabled(true);
                btnComplete.setText("+");
                btnComplete.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            btnComplete.setOnClickListener(v -> {
                if (completeListener != null) {
                    completeListener.onComplete(item);
                }
            });

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item);
                }
            });
        }
    }
}