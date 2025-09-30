package com.kinneret.rcook.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.kinneret.rcook.R;
import com.kinneret.rcook.model.Lesson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class EditLessonActivity extends BaseActivity {

    private static final String TAG = "EditLessonActivity";
    public static final String EXTRA_LESSON_ID = "lesson_id";

    private EditText nameEditText, guideEditText, shortDescEditText, fullDescEditText, videoUrlEditText, imageUrlEditText;
    private Spinner levelSpinner;
    private Button saveButton, cancelButton, selectVideoButton, selectImageButton;
    private ImageView backButton;
    private Toolbar toolbar;

    // Preview elements
    private VideoView videoPreview;
    private ImageView imagePreview;
    private LinearLayout videoPlaceholder;
    private CardView videoPreviewCard, imagePreviewCard;

    private String editingLessonId = null;
    private Lesson editingLesson = null;

    // File selection launchers
    private ActivityResultLauncher<Intent> videoPickerLauncher;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // Initializes the lesson editor screen. Sets up toolbar, spinners, views, file pickers,
    // click listeners, previews, and determines if this is a new lesson or edit mode.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_edit_lesson);

            // Initialize toolbar first
            initializeToolbar();

            // Initialize file pickers
            initializeFilePickers();

            // Initialize views
            initializeViews();

            // Setup spinner
            setupLevelSpinner();

            // Setup click listeners
            setupClickListeners();

            // Setup preview functionality
            setupPreviewFunctionality();

            // Check if editing existing lesson
            checkEditMode();

            // Set default guide name for new lessons
            setupDefaultGuideName();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading lesson editor: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Automatically fills in the current user's full name as the guide name for new lessons.
    private void setupDefaultGuideName() {
        if (editingLesson == null && guideEditText != null) {
            if (dataManager != null) {
                dataManager.getCurrentUser().observe(this, user -> {
                    if (user != null && TextUtils.isEmpty(guideEditText.getText())) {
                        String fullName = user.getFullName();
                        if (!TextUtils.isEmpty(fullName)) {
                            guideEditText.setText(fullName);
                            Log.d(TAG, "Set default guide name: " + fullName);
                        }
                    }
                });
            }
        }
    }

    // Sets up activity result launchers for selecting video and image files from device storage.
    private void initializeFilePickers() {
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        handleVideoSelection(videoUri);
                    }
                }
        );

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        handleImageSelection(imageUri);
                    }
                }
        );
    }

    // Connects all views from the XML layout to their Java variables and performs basic checks.
    private void initializeViews() {
        nameEditText = findViewById(R.id.lesson_name);
        guideEditText = findViewById(R.id.lesson_guide);
        shortDescEditText = findViewById(R.id.lesson_short_description);
        fullDescEditText = findViewById(R.id.lesson_full_description);
        videoUrlEditText = findViewById(R.id.lesson_video_url);
        imageUrlEditText = findViewById(R.id.lesson_image_url);
        levelSpinner = findViewById(R.id.lesson_level_spinner);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        selectVideoButton = findViewById(R.id.select_video_button);
        selectImageButton = findViewById(R.id.select_image_button);

        // Initialize preview elements
        videoPreview = findViewById(R.id.video_preview);
        imagePreview = findViewById(R.id.image_preview);
        videoPlaceholder = findViewById(R.id.video_placeholder);
        videoPreviewCard = findViewById(R.id.video_preview_card);
        imagePreviewCard = findViewById(R.id.image_preview_card);

        if (nameEditText == null || saveButton == null || cancelButton == null || levelSpinner == null) {
            throw new RuntimeException("Required views not found in layout");
        }
        Log.d(TAG, "All views initialized successfully");
    }

    // Populates the lesson level spinner using predefined options (e.g., Beginner, Medium).
    private void setupLevelSpinner() {
        try {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.levels_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            levelSpinner.setAdapter(adapter);
            Log.d(TAG, "Level spinner setup successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up spinner: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up level spinner", Toast.LENGTH_SHORT).show();
        }
    }

    // Sets up listeners for the Save, Cancel, Back, and Select Video/Image buttons.
    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> {
            if (validateFields()) {
                saveLesson();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        if (selectVideoButton != null) {
            selectVideoButton.setOnClickListener(v -> selectVideo());
        }
        if (selectImageButton != null) {
            selectImageButton.setOnClickListener(v -> selectImage());
        }

        Log.d(TAG, "Click listeners setup successfully");
    }

    // Sets up text change listeners to update the video and image previews when their URLs change.
    private void setupPreviewFunctionality() {
        // Monitor video URL changes for preview updates
        if (videoUrlEditText != null) {
            videoUrlEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    updateVideoPreview(s.toString().trim());
                }
            });
        }

        // Monitor image URL changes for preview updates
        if (imageUrlEditText != null) {
            imageUrlEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    updateImagePreview(s.toString().trim());
                }
            });
        }

        // Initialize with empty preview for new lessons
        if (editingLesson == null) {
            showEmptyVideoPreview();
            showDefaultImagePreview();
        }
    }

    // Tries to load the video preview from internal storage or app resources.
    // Shows an empty placeholder if not found.
    private void updateVideoPreview(String videoFileName) {
        if (TextUtils.isEmpty(videoFileName)) {
            showEmptyVideoPreview();
            return;
        }

        try {
            Log.d(TAG, "Updating video preview for: " + videoFileName);

            // First, try to find the video file in internal storage
            File videoFile = findVideoFile(videoFileName);

            if (videoFile != null && videoFile.exists()) {
                // Load from internal storage
                Uri videoUri = Uri.fromFile(videoFile);
                Log.d(TAG, "Loading video from internal storage: " + videoFile.getAbsolutePath());
                loadVideoFromUri(videoUri);
            } else {
                // Try to load from resources (drawable or raw)
                Uri videoUri = getVideoResourceUri(videoFileName);

                if (videoUri != null) {
                    Log.d(TAG, "Loading video from resources: " + videoFileName);
                    loadVideoFromUri(videoUri);
                } else {
                    Log.d(TAG, "Video file not found: " + videoFileName);
                    showEmptyVideoPreview();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating video preview: " + e.getMessage(), e);
            showEmptyVideoPreview();
        }
    }

    // Loads the image preview from internal storage or app resources. Falls back to a default image.
    private void updateImagePreview(String imageFileName) {
        if (TextUtils.isEmpty(imageFileName)) {
            showDefaultImagePreview();
            return;
        }

        try {
            Log.d(TAG, "Updating image preview for: " + imageFileName);

            // Check if this is a drawable resource path
            if (imageFileName.startsWith("drawable/")) {
                // This is an existing lesson with drawable resource
                String resourceName = getResourceName(imageFileName);
                int imageResourceId = getImageResourceId(imageFileName);

                if (imageResourceId != 0) {
                    Log.d(TAG, "Loading image from drawable resources: " + imageFileName + " (ID: " + imageResourceId + ")");
                    loadImageFromResource(imageResourceId);
                    return;
                }
            }

            // First, try to find the image file in internal storage
            File imageFile = findImageFile(imageFileName);

            if (imageFile != null && imageFile.exists()) {
                // Load from internal storage
                Log.d(TAG, "Loading image from internal storage: " + imageFile.getAbsolutePath());
                loadImageFromFile(imageFile);
            } else {
                // Try to load from drawable resources (for files without drawable/ prefix)
                int imageResourceId = getImageResourceId(imageFileName);

                if (imageResourceId != 0) {
                    // Load from drawable resources
                    Log.d(TAG, "Loading image from drawable resources: " + imageFileName + " (ID: " + imageResourceId + ")");
                    loadImageFromResource(imageResourceId);
                } else {
                    Log.d(TAG, "Image file not found: " + imageFileName);
                    showDefaultImagePreview();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating image preview: " + e.getMessage(), e);
            showDefaultImagePreview();
        }
    }

    // Loads an image from a file path using optimized Bitmap decoding.
    private void loadImageFromFile(File imageFile) {
        if (imagePreview != null) {
            try {
                // Use BitmapFactory options to avoid memory issues
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                // Calculate sample size to reduce memory usage
                options.inSampleSize = calculateInSampleSize(options, 150, 150);
                options.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
                if (bitmap != null) {
                    imagePreview.setImageBitmap(bitmap);
                    imagePreviewCard.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Successfully loaded image from file");
                } else {
                    Log.e(TAG, "Failed to decode bitmap from file");
                    showDefaultImagePreview();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image bitmap from file: " + e.getMessage());
                showDefaultImagePreview();
            }
        }
    }

    // Displays an image from a drawable resource ID in the preview.
    private void loadImageFromResource(int imageResourceId) {
        if (imagePreview != null) {
            try {
                // Clear any previous image first
                imagePreview.setImageResource(0);

                // Load the new image with proper error handling
                Drawable drawable = ContextCompat.getDrawable(this, imageResourceId);
                if (drawable != null) {
                    imagePreview.setImageDrawable(drawable);
                    imagePreviewCard.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Successfully loaded image from drawable resource: " + imageResourceId);
                } else {
                    Log.e(TAG, "Drawable resource is null for ID: " + imageResourceId);
                    showDefaultImagePreview();
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Resource not found for ID: " + imageResourceId, e);
                showDefaultImagePreview();
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from drawable: " + e.getMessage(), e);
                showDefaultImagePreview();
            }
        }
    }

    // Calculates how much to scale down an image to efficiently fit in preview without memory issues.
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // Attempts to locate a video resource by name in the `raw` or `drawable` directories.
    private Uri getVideoResourceUri(String videoFileName) {
        if (videoFileName == null || videoFileName.trim().isEmpty()) {
            return null;
        }

        String resourceName = getResourceName(videoFileName);

        // Try raw resources first (videos are usually in raw folder)
        int videoResourceId = getResources().getIdentifier(resourceName, "raw", getPackageName());

        if (videoResourceId != 0) {
            return Uri.parse("android.resource://" + getPackageName() + "/" + videoResourceId);
        }

        // Try drawable resources as fallback
        videoResourceId = getResources().getIdentifier(resourceName, "drawable", getPackageName());

        if (videoResourceId != 0) {
            return Uri.parse("android.resource://" + getPackageName() + "/" + videoResourceId);
        }

        return null;
    }

    // Finds the drawable or mipmap resource ID matching the given image filename.
    private int getImageResourceId(String imageFileName) {
        if (imageFileName == null || imageFileName.trim().isEmpty()) {
            return 0;
        }

        String resourceName = getResourceName(imageFileName);
        Log.d(TAG, "Looking for image resource: " + resourceName);

        // Try drawable resources
        int imageResourceId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        Log.d(TAG, "Drawable resource ID for '" + resourceName + "': " + imageResourceId);

        if (imageResourceId != 0) {
            return imageResourceId;
        }

        // Try mipmap resources as fallback
        imageResourceId = getResources().getIdentifier(resourceName, "mipmap", getPackageName());
        Log.d(TAG, "Mipmap resource ID for '" + resourceName + "': " + imageResourceId);

        return imageResourceId;
    }

    // Cleans and converts a filename to match resource naming conventions (e.g., lowercased with underscores).
    private String getResourceName(String fileName) {
        if (fileName == null) {
            return "";
        }

        String resourceName = fileName;

        // Handle drawable/ prefix for existing lessons
        if (resourceName.startsWith("drawable/")) {
            resourceName = resourceName.substring("drawable/".length());
            Log.d(TAG, "Removed drawable/ prefix: " + resourceName);
        }

        // Remove file extension if present
        int lastDotIndex = resourceName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            resourceName = resourceName.substring(0, lastDotIndex);
        }

        // Convert to valid resource name (lowercase, replace invalid chars with underscores)
        resourceName = resourceName.toLowerCase()
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("_{2,}", "_") // Replace multiple underscores with single
                .replaceAll("^_+|_+$", ""); // Remove leading/trailing underscores

        Log.d(TAG, "Converted filename '" + fileName + "' to resource name '" + resourceName + "'");

        return resourceName;
    }

    // Plays a video in the preview area with looping and touch-to-pause functionality.
    // Handles errors gracefully and reverts to an empty preview.
    private void loadVideoFromUri(Uri videoUri) {
        if (videoPreview != null) {
            try {
                videoPreview.setVideoURI(videoUri);
                videoPreview.setVisibility(View.VISIBLE);
                if (videoPlaceholder != null) {
                    videoPlaceholder.setVisibility(View.GONE);
                }

                // Prepare video and set it to loop for preview
                videoPreview.setOnPreparedListener(mp -> {
                    try {
                        mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        mp.setLooping(true); // Enable looping for preview
                        mp.seekTo(0); // Start from beginning
                        mp.start(); // Auto-start the video for preview
                        Log.d(TAG, "Video prepared and started with looping");
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting video properties: " + e.getMessage());
                    }
                });

                videoPreview.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "Video preview error: what=" + what + ", extra=" + extra);
                    showEmptyVideoPreview();
                    return true;
                });

                // Add click listener to pause/resume video preview
                videoPreview.setOnClickListener(v -> {
                    try {
                        if (videoPreview.isPlaying()) {
                            videoPreview.pause();
                            Log.d(TAG, "Video paused");
                        } else {
                            videoPreview.start();
                            Log.d(TAG, "Video resumed");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error controlling video playback: " + e.getMessage());
                    }
                });

                Log.d(TAG, "Video preview setup completed");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up video preview: " + e.getMessage());
                showEmptyVideoPreview();
            }
        }
    }

    // Searches for a video file in the app's internal storage paths.
    private File findVideoFile(String videoFileName) {
        if (videoFileName == null || videoFileName.trim().isEmpty()) {
            return null;
        }
        // Try multiple possible locations for the video file

        // Option 1: In videos subdirectory
        File videosDir = new File(getFilesDir(), "videos");
        File videoInSubDir = new File(videosDir, videoFileName);
        if (videoInSubDir.exists()) {
            return videoInSubDir;
        }

        // Option 2: Directly in files directory
        File videoInFilesDir = new File(getFilesDir(), videoFileName);
        if (videoInFilesDir.exists()) {
            return videoInFilesDir;
        }

        return null;
    }

    // Searches for an image file in the app's internal storage paths.
    private File findImageFile(String imageFileName) {
        if (imageFileName == null || imageFileName.trim().isEmpty()) {
            return null;
        }
        // Try multiple possible locations for the image file

        // Option 1: In images subdirectory
        File imagesDir = new File(getFilesDir(), "images");
        File imageInSubDir = new File(imagesDir, imageFileName);
        if (imageInSubDir.exists()) {
            return imageInSubDir;
        }

        // Option 2: Directly in files directory
        File imageInFilesDir = new File(getFilesDir(), imageFileName);
        if (imageInFilesDir.exists()) {
            return imageInFilesDir;
        }

        return null;
    }

    // Hides the video player and shows a placeholder when no video is available.
    private void showEmptyVideoPreview() {
        if (videoPreview != null) {
            videoPreview.setVisibility(View.GONE);
            videoPreview.stopPlayback(); // Stop any current playback
        }
        if (videoPlaceholder != null) {
            videoPlaceholder.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Showing empty video preview");
    }

    // Displays a default image when the preview image can't be loaded.
    private void showDefaultImagePreview() {
        if (imagePreview != null) {
            // Use a more appropriate default image
            // Check if we have a default placeholder image
            int defaultImageResource = getResources().getIdentifier("default_lesson_image", "drawable", getPackageName());
            if (defaultImageResource == 0) {
                // Fallback to launcher background
                defaultImageResource = R.drawable.ic_image_placeholder;
            }

            try {
                imagePreview.setImageResource(defaultImageResource);
                imagePreviewCard.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing default image preview");
            } catch (Exception e) {
                Log.e(TAG, "Error showing default image preview: " + e.getMessage());
                // Hide the preview if we can't show anything
                imagePreviewCard.setVisibility(View.GONE);
            }
        }
    }

    // Opens the system file picker to choose a video file from the device.
    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        videoPickerLauncher.launch(Intent.createChooser(intent, "Select Video"));
    }

    // Opens the system file picker to choose a image file from the device.
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    // Saves the selected video to internal storage, updates the input field, and updates the preview.
    private void handleVideoSelection(Uri videoUri) {
        try {
            String fileName = "video_" + System.currentTimeMillis() + ".mp4";
            File videoFile = new File(getFilesDir(), "videos");
            if (!videoFile.exists()) {
                videoFile.mkdirs();
            }

            File destFile = new File(videoFile, fileName);
            copyFileFromUri(videoUri, destFile);

            videoUrlEditText.setText(fileName);
            Toast.makeText(this, "Video selected: " + fileName, Toast.LENGTH_SHORT).show();

            // Update preview immediately after selection
            updateVideoPreview(fileName);

        } catch (Exception e) {
            Log.e(TAG, "Error handling video selection: " + e.getMessage(), e);
            Toast.makeText(this, "Error selecting video", Toast.LENGTH_SHORT).show();
        }
    }

    // Saves the selected image to internal storage, updates the input field, and updates the preview.
    private void handleImageSelection(Uri imageUri) {
        try {
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(getFilesDir(), "images");
            if (!imageFile.exists()) {
                imageFile.mkdirs();
            }

            File destFile = new File(imageFile, fileName);
            copyFileFromUri(imageUri, destFile);

            imageUrlEditText.setText(fileName);
            Toast.makeText(this, "Image selected: " + fileName, Toast.LENGTH_SHORT).show();

            // Update preview immediately after selection
            updateImagePreview(fileName);

        } catch (Exception e) {
            Log.e(TAG, "Error handling image selection: " + e.getMessage(), e);
            Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show();
        }
    }

    // Copies a file from a selected content URI into the app's internal storage.
    private void copyFileFromUri(Uri sourceUri, File destFile) throws Exception {
        try (InputStream inputStream = getContentResolver().openInputStream(sourceUri);
             FileOutputStream outputStream = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    // Checks if the activity was opened in edit mode using an existing lesson ID.
    // Loads existing lesson data if applicable.
    private void checkEditMode() {
        if (getIntent().hasExtra(EXTRA_LESSON_ID)) {
            editingLessonId = getIntent().getStringExtra(EXTRA_LESSON_ID);

            if (editingLessonId != null) {
                lessonViewModel.getAllLessons().observe(this, lessons -> {
                    if (lessons != null) {
                        for (Lesson lesson : lessons) {
                            if (lesson.getId().equals(editingLessonId)) {
                                editingLesson = lesson;
                                loadLessonData();
                                Toast.makeText(this, "Editing lesson: " + lesson.getName(), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                });
            }
        } else {
            // New lesson - show empty previews
            showEmptyVideoPreview();
            showDefaultImagePreview();
        }
    }

    // Loads existing lesson data into the form fields and updates video/image previews.
    private void loadLessonData() {
        if (editingLesson != null) {
            Log.d(TAG, "Loading lesson data for: " + editingLesson.getName());

            nameEditText.setText(editingLesson.getName());
            guideEditText.setText(editingLesson.getGuideName());
            shortDescEditText.setText(editingLesson.getShortDescription());
            fullDescEditText.setText(editingLesson.getFullDescription());
            videoUrlEditText.setText(editingLesson.getVideoUrl());
            imageUrlEditText.setText(editingLesson.getImageUrl());

            String level = editingLesson.getLevel();
            ArrayAdapter adapter = (ArrayAdapter) levelSpinner.getAdapter();
            int position = adapter.getPosition(level);
            if (position >= 0) {
                levelSpinner.setSelection(position);
            }

            // Load existing previews for editing mode with proper delay
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                String videoUrl = editingLesson.getVideoUrl();
                String imageUrl = editingLesson.getImageUrl();

                Log.d(TAG, "Loading existing video: " + videoUrl);
                Log.d(TAG, "Loading existing image: " + imageUrl);

                if (!TextUtils.isEmpty(videoUrl)) {
                    updateVideoPreview(videoUrl);
                } else {
                    showEmptyVideoPreview();
                }

                if (!TextUtils.isEmpty(imageUrl)) {
                    updateImagePreview(imageUrl);
                } else {
                    showDefaultImagePreview();
                }
            }, 200); // Increased delay to ensure UI is ready
        }
    }

    // Saves or updates the lesson based on current form data.
    // Uses DataManager to persist changes and handles success/error feedback.
    private void saveLesson() {
        try {
            Lesson lessonToSave;

            if (editingLesson != null) {
                lessonToSave = editingLesson;
            } else {
                lessonToSave = new Lesson();
                lessonToSave.setId(UUID.randomUUID().toString());
            }

            lessonToSave.setName(nameEditText.getText().toString().trim());
            lessonToSave.setGuideName(guideEditText.getText().toString().trim());
            lessonToSave.setShortDescription(shortDescEditText.getText().toString().trim());
            lessonToSave.setFullDescription(fullDescEditText.getText().toString().trim());
            lessonToSave.setVideoUrl(videoUrlEditText.getText().toString().trim());
            lessonToSave.setImageUrl(imageUrlEditText.getText().toString().trim());
            lessonToSave.setLevel(levelSpinner.getSelectedItem().toString());

            if (editingLesson != null) {
                dataManager.updateLesson(lessonToSave);
                Toast.makeText(this, "Lesson updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                dataManager.addLesson(lessonToSave);
                Toast.makeText(this, "Lesson created successfully!", Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error saving lesson: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving lesson: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Validates that required fields (lesson name, guide, short description) are filled before saving.
    private boolean validateFields() {
        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Lesson name is required");
            nameEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(guideEditText.getText())) {
            guideEditText.setError("Guide name is required");
            guideEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(shortDescEditText.getText())) {
            shortDescEditText.setError("Short description is required");
            shortDescEditText.requestFocus();
            return false;
        }

        return true;
    }

    // Manages the state of the video preview during activity lifecycle events.
    @Override
    protected void onPause() {
        super.onPause();
        // Pause video preview when activity is paused
        if (videoPreview != null && videoPreview.isPlaying()) {
            videoPreview.pause();
        }
    }

    // Manages the state of the video preview during activity lifecycle events.
    @Override
    protected void onResume() {
        super.onResume();
        // Resume video preview if it was playing
        if (videoPreview != null && videoPreview.getVisibility() == View.VISIBLE) {
            videoPreview.start();
        }
    }

    // Manages the state of the video preview during activity lifecycle events.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up video resources
        if (videoPreview != null) {
            videoPreview.stopPlayback();
        }
    }
}
