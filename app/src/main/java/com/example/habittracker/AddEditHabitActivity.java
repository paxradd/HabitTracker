package com.example.habittracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.habittracker.data.AppDatabase;
import com.example.habittracker.data.Habit;
import com.example.habittracker.data.HabitHistory;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditHabitActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etDescription;
    private TextInputEditText etTargetCount;
    private RadioGroup rgTimeOfDay;
    private Button btnSave, btnCancel;

    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private int habitId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etTargetCount = findViewById(R.id.etTargetCount);
        rgTimeOfDay = findViewById(R.id.rgTimeOfDay);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        db = AppDatabase.getInstance(this);

        habitId = getIntent().getIntExtra("habitId", -1);
        if (habitId != -1) {
            loadHabitData(habitId);
        }

        btnSave.setOnClickListener(v -> saveHabit());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadHabitData(int habitId) {
        executor.execute(() -> {
            Habit habit = db.habitDao().getHabitById(habitId);
            runOnUiThread(() -> {
                if (habit != null) {
                    etName.setText(habit.name);
                    etDescription.setText(habit.description != null ? habit.description : "");
                    etTargetCount.setText(String.valueOf(habit.targetCount));
                    switch (habit.timeOfDay) {
                        case "Утро": rgTimeOfDay.check(R.id.rbMorning); break;
                        case "День": rgTimeOfDay.check(R.id.rbDay); break;
                        case "Вечер": rgTimeOfDay.check(R.id.rbEvening); break;
                        default: rgTimeOfDay.check(R.id.rbAny); break;
                    }
                }
            });
        });
    }

    private void saveHabit() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String targetStr = etTargetCount.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Введите название");
            etName.requestFocus();
            return;
        }
        if (targetStr.isEmpty()) {
            etTargetCount.setError("Укажите количество");
            etTargetCount.requestFocus();
            return;
        }
        int targetCount;
        try {
            targetCount = Integer.parseInt(targetStr);
        } catch (NumberFormatException e) {
            etTargetCount.setError("Введите число");
            etTargetCount.requestFocus();
            return;
        }
        if (targetCount <= 0) {
            etTargetCount.setError("Количество должно быть > 0");
            etTargetCount.requestFocus();
            return;
        }

        int checkedId = rgTimeOfDay.getCheckedRadioButtonId();
        String timeOfDay;
        if (checkedId == R.id.rbMorning) timeOfDay = "Утро";
        else if (checkedId == R.id.rbDay) timeOfDay = "День";
        else if (checkedId == R.id.rbEvening) timeOfDay = "Вечер";
        else timeOfDay = "Любое";

        String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());

        executor.execute(() -> {
            if (habitId == -1) {
                Habit habit = new Habit();
                habit.name = name;
                habit.description = description;
                habit.targetCount = targetCount;
                habit.timeOfDay = timeOfDay;
                habit.createdAt = today;
                habit.isDeleted = 0;
                db.habitDao().insertHabit(habit);
            } else {
                Habit habit = db.habitDao().getHabitById(habitId);
                if (habit != null) {
                    int oldTarget = habit.targetCount;
                    habit.name = name;
                    habit.description = description;
                    habit.targetCount = targetCount;
                    habit.timeOfDay = timeOfDay;
                    db.habitDao().updateHabit(habit);

                    if (targetCount < oldTarget) {
                        HabitHistory history = db.habitDao().getHistoryForDay(habitId, today);
                        if (history != null && history.completedCount > targetCount) {
                            history.completedCount = targetCount;
                            db.habitDao().updateHistory(history);
                        }
                    }
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Привычка сохранена!", Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }
}