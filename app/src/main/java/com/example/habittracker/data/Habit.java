package com.example.habittracker.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;
    public int targetCount;
    public String timeOfDay;
    public String createdAt;
    public int isDeleted;
}