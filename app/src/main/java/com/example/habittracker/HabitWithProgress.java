package com.example.habittracker;

import com.example.habittracker.data.Habit;

public class HabitWithProgress {
    public Habit habit;
    public int completedToday;

    public HabitWithProgress(Habit habit, int completedToday) {
        this.habit = habit;
        this.completedToday = completedToday;
    }
}