package com.kinneret.rcook.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LessonDao {

    // Insert new lesson
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Lesson lesson);

    // Update existing lesson
    @Update
    void update(Lesson lesson);

    // Delete lesson
    @Delete
    void delete(Lesson lesson);

    // Delete all lessons
    @Query("DELETE FROM lesson_table")
    void deleteAll();

    // Get all lessons (with LiveData for observation)
    @Query("SELECT * FROM lesson_table ORDER BY name ASC")
    LiveData<List<Lesson>> getAllLessons();

    // Get lessons by level (with LiveData)
    @Query("SELECT * FROM lesson_table WHERE level = :level ORDER BY name ASC")
    LiveData<List<Lesson>> getLessonsByLevel(String level);

    // Get lesson by ID
    @Query("SELECT * FROM lesson_table WHERE id = :lessonId LIMIT 1")
    Lesson getLessonById(String lessonId);

    // Get favorite lessons
    @Query("SELECT * FROM lesson_table WHERE favorite = 1 ORDER BY name ASC")
    LiveData<List<Lesson>> getFavoriteLessons();

    // Get completed lessons
    @Query("SELECT * FROM lesson_table WHERE completed = 1 ORDER BY name ASC")
    LiveData<List<Lesson>> getCompletedLessons();

    // Check if database has any lessons (for initialization)
    @Query("SELECT * FROM lesson_table LIMIT 1")
    Lesson[] getAnyLesson();

    // Update lesson favorite status
    @Query("UPDATE lesson_table SET favorite = :isFavorite WHERE id = :lessonId")
    void updateFavoriteStatus(String lessonId, boolean isFavorite);

    // Update lesson completion status
    @Query("UPDATE lesson_table SET completed = :isCompleted WHERE id = :lessonId")
    void updateCompletionStatus(String lessonId, boolean isCompleted);
}