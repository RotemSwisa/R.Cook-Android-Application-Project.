package com.kinneret.rcook.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

@Entity(tableName = "user_table")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id = "current_user"; // Single user - fixed ID

    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    private String lastName;

    @ColumnInfo(name = "role")
    private String role; // "Guide" or "Student"

    @ColumnInfo(name = "level")
    private String level; // "Beginners", "Medium", "Advanced"

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "age")
    private int age;

    @ColumnInfo(name = "gender")
    private String gender; // "male" or "female"

    @ColumnInfo(name = "background_color")
    private String backgroundColor; // "gray", "blue", "yellowish", "default"

    // Constructor for Room
    public User(@NonNull String id, String firstName, String lastName, String role,
                String level, String phone, int age, String gender, String backgroundColor) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.level = level;
        this.phone = phone;
        this.age = age;
        this.gender = gender;
        this.backgroundColor = backgroundColor;
    }

    // Empty constructor
    public User() {
        this.id = "current_user";
        this.role = "Student";
        this.level = "Beginners";
        this.backgroundColor = "default";
    }

    // Getters and setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

    // Utility method to get full name
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return "";
    }
}