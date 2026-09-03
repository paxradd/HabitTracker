package com.example.habittracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    void insertHabit(Habit habit);

    @Update
    void updateHabit(Habit habit);

    @Query("SELECT * FROM habits WHERE isDeleted = 0 ORDER BY " +
            "CASE timeOfDay " +
            "WHEN 'Утро' THEN 1 " +
            "WHEN 'День' THEN 2 " +
            "WHEN 'Вечер' THEN 3 " +
            "WHEN 'Любое' THEN 4 " +
            "ELSE 5 END")
    List<Habit> getAllHabitsSorted();

    @Query("SELECT * FROM habits ORDER BY id")
    List<Habit> getAllHabitsIncludingDeleted();

    @Query("SELECT * FROM habits WHERE id = :id")
    Habit getHabitById(int id);

    @Query("SELECT * FROM habit_history WHERE date = :date")
    List<HabitHistory> getHistoryByDate(String date);

    @Query("SELECT * FROM habit_history WHERE habitId = :habitId AND date = :date")
    HabitHistory getHistoryForDay(int habitId, String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(HabitHistory history);

    @Update
    void updateHistory(HabitHistory history);

    @Query("DELETE FROM habit_history WHERE habitId = :habitId")
    void deleteHistoryByHabitId(int habitId);
}