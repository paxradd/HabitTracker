package com.example.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Habit;
import com.example.habittracker.data.HabitHistory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String selectedDate;
    private BottomNavigationView bottomNav;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = AppDatabase.getInstance(this);

        CalendarView calendarView = findViewById(R.id.calendarView);
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        Calendar cal = Calendar.getInstance();
        selectedDate = sdf.format(cal.getTime());
        loadHistory(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            selectedDate = sdf.format(cal.getTime());
            loadHistory(selectedDate);
        });

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_today) {
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadHistory(String date) {
        executor.execute(() -> {
            List<Habit> allHabits = db.habitDao().getAllHabitsIncludingDeleted();
            List<HabitHistory> histories = db.habitDao().getHistoryByDate(date);

            List<HistoryAdapter.HistoryItem> items = new ArrayList<>();

            Date selectedDateObj;
            try {
                selectedDateObj = sdf.parse(date);
            } catch (ParseException e) {
                selectedDateObj = new Date();
            }

            String todayStr = sdf.format(new Date());
            Date todayObj;
            try {
                todayObj = sdf.parse(todayStr);
            } catch (ParseException e) {
                todayObj = new Date();
            }

            for (Habit habit : allHabits) {
                if (habit.createdAt != null) {
                    try {
                        Date createdDate = sdf.parse(habit.createdAt);
                        if (selectedDateObj.before(createdDate) || selectedDateObj.after(todayObj)) {
                            continue;
                        }
                    } catch (ParseException e) {
                    }
                }

                HabitHistory history = null;
                for (HabitHistory h : histories) {
                    if (h.habitId == habit.id) {
                        history = h;
                        break;
                    }
                }
                int completed = (history == null) ? 0 : history.completedCount;
                String status;
                if (completed == 0) {
                    status = "Не выполнено";
                } else if (completed >= habit.targetCount) {
                    status = "Выполнено ✓";
                } else {
                    status = "Частично (" + completed + "/" + habit.targetCount + ")";
                }
                items.add(new HistoryAdapter.HistoryItem(habit, status, completed));
            }

            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new HistoryAdapter(items);
                    rvHistory.setAdapter(adapter);
                } else {
                    adapter.updateList(items);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_history);
        }
    }
}