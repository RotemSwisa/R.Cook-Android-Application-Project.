package com.kinneret.rcook.activity;

import android.annotation.SuppressLint;

import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;

import androidx.appcompat.widget.Toolbar;
import com.kinneret.rcook.R;
import com.kinneret.rcook.model.DataManager;
import com.kinneret.rcook.model.Lesson;

import java.io.File;


public class LessonDetailActivity extends BaseActivity {
    public static final String EXTRA_LESSON_ID = "lesson_id";

    private TextView titleTextView, guideTextView, descriptionTextView;
    private VideoView videoView;
    private ImageView lessonImageView;
    private CheckBox checkCompleted;
    private Lesson currentLesson;
    private ImageButton btnFavorite;

    // Called when the lesson detail screen is created.
    // Initializes the UI, toolbar, and attempts to load lesson data from intent or database.
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();

        String lessonId = getIntent().getStringExtra(EXTRA_LESSON_ID);

        if (lessonId != null) {
            lessonViewModel.getAllLessons().observe(this, lessons -> {
                if (lessons != null) {
                    for (Lesson lesson : lessons) {
                        if (lesson.getId().equals(lessonId)) {
                            currentLesson = lesson;
                            displayLessonData(lesson);
                            setupCheckboxListeners();
                            break;
                        }
                    }
                }
            });
        } else {
            String lessonTitle = getIntent().getStringExtra("title");
            String guideName = getIntent().getStringExtra("guide");
            String description = getIntent().getStringExtra("description");
            String videoUri = getIntent().getStringExtra("video");

            if (lessonTitle != null) {
                displayDirectData(lessonTitle, guideName, description, videoUri);
            } else {
                Toast.makeText(this, "No lesson data found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Finds and connects all views in the layout to their corresponding Java variables.
    private void initViews() {
        titleTextView = findViewById(R.id.lesson_title);
        guideTextView = findViewById(R.id.lesson_guide);
        descriptionTextView = findViewById(R.id.lesson_description);
        videoView = findViewById(R.id.lesson_video);
        lessonImageView = findViewById(R.id.imageLessonDetail);
        checkCompleted = findViewById(R.id.checkCompleted);
        btnFavorite = findViewById(R.id.btnFavorite);
    }

    // Populates the lesson details (title, guide, description, video, image, and states)
    // from the given Lesson object.
    private void displayLessonData(Lesson lesson) {
        titleTextView.setText(lesson.getName());
        guideTextView.setText("Taught by " + lesson.getGuideName());
        descriptionTextView.setText(lesson.getFullDescription());

        displayLessonImage(lesson);
        checkCompleted.setChecked(lesson.isCompleted());
        btnFavorite.setSelected(lesson.isFavorite());

        String videoUri = lesson.getVideoUrl();
        setupVideoView(videoUri);
    }

    // Tries to display the lesson's image from internal storage or drawable resources.
    // Uses a default placeholder if no valid image is found.
    private void displayLessonImage(Lesson lesson) {
        if (lesson.getImageUrl() != null && !lesson.getImageUrl().isEmpty()) {
            if (lesson.getImageUrl().endsWith(".jpg") || lesson.getImageUrl().endsWith(".png")) {
                File imageFile = new File(getFilesDir(), "images/" + lesson.getImageUrl());
                if (imageFile.exists()) {
                    Uri imageUri = Uri.fromFile(imageFile);
                    lessonImageView.setImageURI(imageUri);
                    return;
                }
            }

            int resId = getResources().getIdentifier(lesson.getImageUrl(), "drawable", getPackageName());
            if (resId != 0) {
                lessonImageView.setImageResource(resId);
                return;
            }
        }
        lessonImageView.setImageResource(R.drawable.ic_launcher_background);
    }

    // Displays lesson data directly from intent extras (used as fallback if no ID is passed).
    private void displayDirectData(String title, String guide, String description, String videoUri) {
        titleTextView.setText(title);
        guideTextView.setText("Taught by " + guide);
        descriptionTextView.setText(description);
        lessonImageView.setImageResource(R.drawable.ic_launcher_background);
        setupVideoView(videoUri);
    }

    // Sets listeners for the 'completed' and 'favorite' options.
    // Only allows marking as favorite if the lesson is completed.
    // Updates database and local preferences accordingly.
    private void setupCheckboxListeners() {
        if (currentLesson == null) return;

        checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentLesson.setCompleted(isChecked);

            if (!isChecked && currentLesson.isFavorite()) {
                currentLesson.setFavorite(false);
                btnFavorite.setSelected(false);

                DataManager.getInstance(this).updateFavoriteStatus(currentLesson.getId(), false);
            }

            DataManager.getInstance(this).updateCompletionStatus(currentLesson.getId(), isChecked);

            getSharedPreferences("lesson_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("completed_" + currentLesson.getId(), isChecked)
                    .apply();
        });

        // ImageButton click listener for the heart
        btnFavorite.setOnClickListener(v -> {
            if (!currentLesson.isCompleted()) {
                Toast.makeText(this, "You can only like a lesson after completing it.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean newFavoriteState = !currentLesson.isFavorite();
            currentLesson.setFavorite(newFavoriteState);
            btnFavorite.setSelected(newFavoriteState);

            DataManager.getInstance(this).updateFavoriteStatus(currentLesson.getId(), newFavoriteState);

            getSharedPreferences("lesson_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("favorite_" + currentLesson.getId(), newFavoriteState)
                    .apply();
        });
    }

    // Prepares the VideoView to display a lesson video from local storage or resources.
    // Adds a media controller and starts playback automatically if available.
    private void setupVideoView(String videoUri) {
        if (videoUri != null && !videoUri.isEmpty()) {
            Uri uri = null;

            if (videoUri.endsWith(".mp4")) {
                File file = new File(getFilesDir(), "videos/" + videoUri);
                if (file.exists()) {
                    uri = Uri.fromFile(file);
                } else {
                    Toast.makeText(this, "Video file not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                int resId = getResources().getIdentifier(videoUri, "raw", getPackageName());
                if (resId != 0) {
                    uri = Uri.parse("android.resource://" + getPackageName() + "/" + resId);
                } else {
                    Toast.makeText(this, "Video resource not found", Toast.LENGTH_SHORT).show();
                }
            }

            if (uri != null) {
                videoView.setVideoURI(uri);
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                videoView.start();
            }
        }
    }
}