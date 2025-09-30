package com.kinneret.rcook.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import java.util.List;

/**
 * Singleton class that manages user and lesson data.
 * Acts as a bridge between ViewModel (Room) and legacy SharedPreferences.
 * Handles saving, updating, and retrieving lessons and user details.
 */

public class DataManager {
    private static final String PREF_NAME = "RCookPrefs";
    private static final String KEY_FIRST_TIME = "first_time";
    private static final String TAG = "DataManager";
    private static DataManager instance;
    private final Context context;
    private final SharedPreferences preferences;
    private LessonViewModel lessonViewModel;

    // Private constructor for singleton
    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        preferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Returns the singleton instance of DataManager.
    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // Stores reference to the ViewModel for Room database operations.
    public void setLessonViewModel(LessonViewModel viewModel) {
        this.lessonViewModel = viewModel;
    }

    // Checks if the app is launched for the first time using SharedPreferences.
    public boolean isFirstTimeUser() {
        return preferences.getBoolean(KEY_FIRST_TIME, true);
    }

    // Marks the first-time launch flag as false.
    public void setFirstTimeDone() {
        try {
            preferences.edit().putBoolean(KEY_FIRST_TIME, false).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving first time flag: " + e.getMessage());
        }
    }

    // Returns the current logged-in user from ViewModel.
    public LiveData<User> getCurrentUser() {
        if (lessonViewModel != null) {
            return lessonViewModel.getCurrentUser();
        }
        return null;
    }

    // Saves user data to Room. Falls back to SharedPreferences if needed.
    public void saveUser(User user) {
        if (lessonViewModel != null && user != null) {
            try {
                // Validate user data before saving
                if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                    Log.w(TAG, "User first name is empty, setting default");
                    user.setFirstName("User");
                }
                if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                    Log.w(TAG, "User last name is empty, setting default");
                    user.setLastName("");
                }
                if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                    Log.w(TAG, "User role is empty, setting default to Student");
                    user.setRole("Student");
                }
                if (user.getLevel() == null || user.getLevel().trim().isEmpty()) {
                    Log.w(TAG, "User level is empty, setting default to Beginners");
                    user.setLevel("Beginners");
                }
                if (user.getGender() == null || user.getGender().trim().isEmpty()) {
                    Log.w(TAG, "User gender is empty, setting default");
                    user.setGender("male");
                }

                lessonViewModel.saveUser(user);
                Log.d(TAG, "User saved successfully: " + user.getFullName());
            } catch (Exception e) {
                Log.e(TAG, "Error saving user: " + e.getMessage(), e);
                // Fallback to SharedPreferences if Room fails
                saveUserToPreferences(user);
            }
        } else {
            Log.e(TAG, "Cannot save user: viewModel or user is null");
        }
    }

    // Backup method to save user details in SharedPreferences.
    private void saveUserToPreferences(User user) {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_first_name", user.getFirstName());
            editor.putString("user_last_name", user.getLastName());
            editor.putString("user_role", user.getRole());
            editor.putString("user_level", user.getLevel());
            editor.putString("user_gender", user.getGender());
            editor.putString("user_phone", user.getPhone());
            editor.putInt("user_age", user.getAge());
            editor.putString("user_background_color", user.getBackgroundColor());
            editor.apply();
            Log.d(TAG, "User saved to preferences as fallback");
        } catch (Exception e) {
            Log.e(TAG, "Error saving user to preferences: " + e.getMessage());
        }
    }

    // Returns all lessons from Room database.
    public LiveData<List<Lesson>> getLessons() {
        if (lessonViewModel != null) {
            return lessonViewModel.getAllLessons();
        }
        return null;
    }

    // Returns lessons filtered by level using Room.
    public LiveData<List<Lesson>> getLessonsByLevel(String level) {
        if (lessonViewModel != null) {
            return lessonViewModel.getLessonsByLevel(level);
        }
        return null;
    }

    // Adds a new lesson to the Room database after validating required fields.
    public void addLesson(Lesson lesson) {
        if (lessonViewModel != null && lesson != null) {
            try {
                // Validate lesson data
                if (lesson.getName() == null || lesson.getName().trim().isEmpty()) {
                    Log.e(TAG, "Cannot add lesson: name is empty");
                    return;
                }
                if (lesson.getId() == null || lesson.getId().trim().isEmpty()) {
                    lesson.setId(generateLessonId(lesson.getName()));
                }
                if (lesson.getLevel() == null || lesson.getLevel().trim().isEmpty()) {
                    lesson.setLevel("Beginners");
                }

                lessonViewModel.insert(lesson);
                Log.d(TAG, "Lesson added successfully: " + lesson.getName());
            } catch (Exception e) {
                Log.e(TAG, "Error adding lesson: " + e.getMessage(), e);
            }
        }
    }

    // Updates an existing lesson in Room.
    public void updateLesson(Lesson updatedLesson) {
        if (lessonViewModel != null && updatedLesson != null) {
            try {
                lessonViewModel.update(updatedLesson);
                Log.d(TAG, "Lesson updated successfully: " + updatedLesson.getName());
            } catch (Exception e) {
                Log.e(TAG, "Error updating lesson: " + e.getMessage(), e);
            }
        }
    }

    // Deletes a lesson from Room.
    public void deleteLesson(Lesson lesson) {
        if (lessonViewModel != null && lesson != null) {
            try {
                lessonViewModel.delete(lesson);
                Log.d(TAG, "Lesson deleted successfully: " + lesson.getName());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting lesson: " + e.getMessage(), e);
            }
        }
    }

    // Returns all favorite lessons.
    public LiveData<List<Lesson>> getFavoriteLessons() {
        if (lessonViewModel != null) {
            return lessonViewModel.getFavoriteLessons();
        }
        return null;
    }

    // Returns all completed lessons.
    public LiveData<List<Lesson>> getCompletedLessons() {
        if (lessonViewModel != null) {
            return lessonViewModel.getCompletedLessons();
        }
        return null;
    }

    // Updates the 'favorite' status of a lesson by its ID.
    public void updateFavoriteStatus(String lessonId, boolean isFavorite) {
        if (lessonViewModel != null && lessonId != null && !lessonId.trim().isEmpty()) {
            try {
                lessonViewModel.updateFavoriteStatus(lessonId, isFavorite);
                Log.d(TAG, "Favorite status updated for lesson: " + lessonId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating favorite status: " + e.getMessage(), e);
            }
        }
    }

    // Updates the 'completed' status of a lesson by its ID.
    public void updateCompletionStatus(String lessonId, boolean isCompleted) {
        if (lessonViewModel != null && lessonId != null && !lessonId.trim().isEmpty()) {
            try {
                lessonViewModel.updateCompletionStatus(lessonId, isCompleted);
                Log.d(TAG, "Completion status updated for lesson: " + lessonId);
            } catch (Exception e) {
                Log.e(TAG, "Error updating completion status: " + e.getMessage(), e);
            }
        }
    }

    // Generates a unique ID for a lesson using its name and a timestamp.
    private String generateLessonId(String lessonName) {
        String baseId = lessonName.toLowerCase().replaceAll("[^a-z0-9]", "_");
        long timestamp = System.currentTimeMillis();
        return baseId + "_" + timestamp;
    }

    // No-op method (kept for compatibility). Room handles auto-saving.
    public void saveLessons() {
        // This method is no longer needed with Room as changes are automatically saved
        Log.d(TAG, "saveLessons() called - using Room auto-save");
    }

}