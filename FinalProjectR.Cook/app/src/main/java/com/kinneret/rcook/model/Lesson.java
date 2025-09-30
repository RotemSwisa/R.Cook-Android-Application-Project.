package com.kinneret.rcook.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

/**
 * Represents a cooking or baking lesson stored in the local Room database.
 * Each lesson contains metadata like title, guide name, descriptions, level,
 * media links (image/video), and status flags (completed/favorite).
 *
 * Room entity for the "lesson_table".
 */
@Entity(tableName = "lesson_table")
public class Lesson {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "guide_name")
    private String guideName;

    @ColumnInfo(name = "short_description")
    private String shortDescription;

    @ColumnInfo(name = "full_description")
    private String fullDescription;

    @ColumnInfo(name = "image_url")
    private String imageUrl;

    @ColumnInfo(name = "video_url")
    private String videoUrl;

    @ColumnInfo(name = "level")
    private String level; // "Beginners", "Medium", "Advanced"

    @ColumnInfo(name = "completed")
    private boolean completed;

    @ColumnInfo(name = "favorite")
    private boolean favorite;

    // Constructor - Room requires a constructor with all fields
    public Lesson(@NonNull String id, String name, String guideName, String shortDescription,
                  String fullDescription, String imageUrl, String videoUrl, String level,
                  boolean completed, boolean favorite) {
        this.id = id;
        this.name = name;
        this.guideName = guideName;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.level = level;
        this.completed = completed;
        this.favorite = favorite;
    }

    // Empty constructor for Room
    public Lesson() {
        this.completed = false;
        this.favorite = false;
        this.level = "Beginners";
    }

    // Convenience constructor (for creating new lessons)
    public Lesson(String id, String name, String guideName, String shortDescription,
                  String fullDescription, String imageUrl, String videoUrl, String level) {
        this.id = id;
        this.name = name;
        this.guideName = guideName;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.level = level;
        this.completed = false;
        this.favorite = false;
    }

    // Getters and setters (Room requires public getters for all fields)
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGuideName() { return guideName; }
    public void setGuideName(String guideName) { this.guideName = guideName; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}