package com.example.habittracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Habit;
import com.example.habittracker.data.HabitHistory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements HabitAdapter.OnCompleteListener, HabitAdapter.OnItemClickListener {

    private RecyclerView rvHabits;
    private HabitAdapter adapter;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView todaysDate = findViewById(R.id.todaysDate);
        String currentDate = new SimpleDateFormat("d MMMM", Locale.getDefault()).format(new Date());
        todaysDate.setText(currentDate);

        rvHabits = findViewById(R.id.rvHabits);
        rvHabits.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HabitAdapter(null, this, this);
        rvHabits.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        loadHabits();

        FloatingActionButton fabAddHabit = findViewById(R.id.fabAddHabit);
        fabAddHabit.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddEditHabitActivity.class);
            startActivity(intent);
        });

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_today);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            }
            return true;
        });
    }

    private void loadHabits() {
        executor.execute(() -> {
            List<Habit> habits = db.habitDao().getAllHabitsSorted();
            String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
            List<HabitHistory> histories = db.habitDao().getHistoryByDate(today);

            List<HabitWithProgress> items = new ArrayList<>();
            for (Habit habit : habits) {
                int completed = 0;
                for (HabitHistory h : histories) {
                    if (h.habitId == habit.id) {
                        completed = h.completedCount;
                        break;
                    }
                }
                items.add(new HabitWithProgress(habit, completed));
            }

            runOnUiThread(() -> adapter.updateList(items));
        });
    }

    @Override
    public void onComplete(HabitWithProgress item) {
        executor.execute(() -> {
            String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
            HabitHistory history = db.habitDao().getHistoryForDay(item.habit.id, today);
            int current = (history == null) ? 0 : history.completedCount;

            if (current >= item.habit.targetCount) {
                runOnUiThread(() -> Toast.makeText(this, "Цель уже выполнена!", Toast.LENGTH_SHORT).show());
                return;
            }

            if (history == null) {
                history = new HabitHistory();
                history.habitId = item.habit.id;
                history.date = today;
                history.completedCount = 1;
            } else {
                history.completedCount++;
            }
            db.habitDao().insertHistory(history);
            runOnUiThread(this::loadHabits);
        });
    }

    @Override
    public void onItemClick(HabitWithProgress item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.habit.name)
                .setItems(new String[]{"Редактировать", "Удалить"}, (dialog, which) -> {
                    if (which == 0) {
                        editHabit(item.habit);
                    } else if (which == 1) {
                        confirmDelete(item.habit);
                    }
                })
                .show();
    }

    private void editHabit(Habit habit) {
        Intent intent = new Intent(this, AddEditHabitActivity.class);
        intent.putExtra("habitId", habit.id);
        startActivity(intent);
    }

    private void confirmDelete(Habit habit) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить привычку?")
                .setMessage("Вы уверены, что хотите удалить \"" + habit.name + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteHabit(habit))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteHabit(Habit habit) {
        executor.execute(() -> {
            habit.isDeleted = 1;
            db.habitDao().updateHabit(habit);
            runOnUiThread(() -> {
                Toast.makeText(this, "Привычка удалена", Toast.LENGTH_SHORT).show();
                loadHabits();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_today);
        }
    }
}