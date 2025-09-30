package com.kinneret.rcook.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    // Insert or update user (only one user in the app)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(User user);

    // Update user
    @Update
    void update(User user);

    // Get current user (with LiveData for observation)
    @Query("SELECT * FROM user_table WHERE id = 'current_user' LIMIT 1")
    LiveData<User> getCurrentUser();

    // Get current user without LiveData (for immediate access)
    @Query("SELECT * FROM user_table WHERE id = 'current_user' LIMIT 1")
    User getCurrentUserSync();

    // Check if user exists
    @Query("SELECT COUNT(*) FROM user_table WHERE id = 'current_user'")
    int getUserCount();

    // Delete current user (for testing purposes)
    @Query("DELETE FROM user_table WHERE id = :userId")
    void deleteUser(String userId);


    // For test: get user by ID (non-LiveData)
    @Query("SELECT * FROM user_table WHERE id = :userId LIMIT 1")
    User getUserById(String userId);

    // For test: update only user role by ID
    @Query("UPDATE user_table SET role = :newRole WHERE id = :userId")
    void updateUserRole(String userId, String newRole);

}