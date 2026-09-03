package com.example.habittracker.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_history")
public class HabitHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int habitId;
    public String date;
    public int completedCount;
}