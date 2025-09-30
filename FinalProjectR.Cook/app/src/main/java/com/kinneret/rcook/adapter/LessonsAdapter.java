package com.kinneret.rcook.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kinneret.rcook.R;
import com.kinneret.rcook.model.DataManager;
import com.kinneret.rcook.model.Lesson;

import java.io.File;
import java.util.List;

/**
 * Adapter that manages how lessons are displayed in a RecyclerView.
 * Handles image loading, checkbox and favorite interactions, and role-based UI logic.
 */
public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.ViewHolder> {

    private final List<Lesson> lessonList;
    private final Context context;
    private final OnLessonClickListener listener;
    private final boolean isUserGuide;

    // Callback interface to handle lesson click and long-click events.
    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
        void onLessonLongClick(Lesson lesson, int position);
    }

    // Constructs the adapter with a context, lesson list, click listeners, and user role flag.
    public LessonsAdapter(Context context, List<Lesson> lessonList,
                          OnLessonClickListener listener, boolean isUserGuide) {
        this.context = context;
        this.lessonList = lessonList;
        this.listener = listener;
        this.isUserGuide = isUserGuide;
    }

    // Inflates the layout for a single lesson item and returns its ViewHolder.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new ViewHolder(view);
    }

    // Binds lesson data to the UI components in the ViewHolder.
    // Handles checkbox logic, image loading, and click events (short and long).
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= lessonList.size() || position < 0) return;

        Lesson lesson = lessonList.get(position);
        if (lesson == null) return;

        holder.textLessonTitle.setText(lesson.getName());
        holder.textLessonDescription.setText(lesson.getShortDescription());

        // Set checkbox state without triggering listeners
        holder.checkCompleted.setOnCheckedChangeListener(null);
        holder.checkCompleted.setChecked(lesson.isCompleted());

        // Set ImageButton state
        holder.btnFavorite.setSelected(lesson.isFavorite());

        // Load image
        loadLessonImage(lesson.getImageUrl(), holder.imageLessonThumb);

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLessonClick(lesson);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null && isUserGuide) {
                listener.onLessonLongClick(lesson, position);
                return true;
            }
            return false;
        });

        holder.checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                lesson.setCompleted(isChecked);

                // If unchecking completed, also uncheck favorite
                if (!isChecked && lesson.isFavorite()) {
                    lesson.setFavorite(false);
                    holder.btnFavorite.setSelected(false);
                    DataManager.getInstance(context).updateFavoriteStatus(lesson.getId(), false);
                }

                DataManager.getInstance(context).updateCompletionStatus(lesson.getId(), isChecked);
            }
        });

        // Replace CheckBox listener with ImageButton click listener
        holder.btnFavorite.setOnClickListener(v -> {
            if (!lesson.isCompleted()) {
                Toast.makeText(context, "You can only like a lesson after completing it.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean newFavoriteState = !lesson.isFavorite();
            lesson.setFavorite(newFavoriteState);
            holder.btnFavorite.setSelected(newFavoriteState);
            DataManager.getInstance(context).updateFavoriteStatus(lesson.getId(), newFavoriteState);
        });
    }

    // Loads the lesson image either from internal storage or app resources using Glide.
    // Falls back to a default image if not found.
    private void loadLessonImage(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // No image URL - show default
            imageView.setImageResource(R.drawable.image_rounded_corners);
            return;
        }

        // First, try to load as a file (for user-uploaded images)
        File imageFile = findImageFile(imageUrl);

        if (imageFile != null && imageFile.exists()) {
            // Load from file using Glide
            try {
                Glide.with(context)
                        .load(imageFile)
                        .centerCrop()
                        .placeholder(R.drawable.image_rounded_corners)
                        .error(R.drawable.image_rounded_corners)
                        .into(imageView);
                return;
            } catch (Exception e) {
                Log.e("LessonsAdapter", "Error loading image from file: " + e.getMessage());
            }
        }

        // If file doesn't exist, try to load as drawable resource (for pre-loaded lessons)
        try {
            int resourceId = context.getResources().getIdentifier(
                    imageUrl.replace("drawable/", "").replace(".jpg", "").replace(".png", ""),
                    "drawable",
                    context.getPackageName());

            if (resourceId != 0) {
                Glide.with(context)
                        .load(resourceId)
                        .centerCrop()
                        .placeholder(R.drawable.image_rounded_corners)
                        .error(R.drawable.image_rounded_corners)
                        .into(imageView);
            } else {
                // Neither file nor resource found - show default
                imageView.setImageResource(R.drawable.image_rounded_corners);
            }
        } catch (Exception e) {
            Log.e("LessonsAdapter", "Error loading image from resources: " + e.getMessage());
            imageView.setImageResource(R.drawable.image_rounded_corners);
        }
    }

    // Searches for the image file in the app's internal storage, including fallback checks for extensions.
    private File findImageFile(String imageFileName) {
        if (imageFileName == null || imageFileName.trim().isEmpty()) {
            return null;
        }
        // Try multiple possible locations for the image file

        // Option 1: In images subdirectory
        File imagesDir = new File(context.getFilesDir(), "images");
        File imageInSubDir = new File(imagesDir, imageFileName);
        if (imageInSubDir.exists()) {
            return imageInSubDir;
        }

        // Option 2: Directly in files directory
        File imageInFilesDir = new File(context.getFilesDir(), imageFileName);
        if (imageInFilesDir.exists()) {
            return imageInFilesDir;
        }

        // Option 3: Try with different file extensions if none specified
        if (!imageFileName.contains(".")) {
            String[] extensions = {".jpg", ".jpeg", ".png", ".bmp"};
            for (String ext : extensions) {
                File fileWithExt = new File(imagesDir, imageFileName + ext);
                if (fileWithExt.exists()) {
                    return fileWithExt;
                }
                fileWithExt = new File(context.getFilesDir(), imageFileName + ext);
                if (fileWithExt.exists()) {
                    return fileWithExt;
                }
            }
        }

        return null;
    }

    // Returns the total number of lessons to be displayed.
    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageLessonThumb;
        TextView textLessonTitle, textLessonDescription;
        CheckBox checkCompleted;
        ImageButton btnFavorite;

        // Holds references to the views used in each lesson item layout.
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageLessonThumb = itemView.findViewById(R.id.imageLessonThumb);
            textLessonTitle = itemView.findViewById(R.id.textLessonTitle);
            textLessonDescription = itemView.findViewById(R.id.textLessonDescription);
            checkCompleted = itemView.findViewById(R.id.checkCompleted);
            btnFavorite = itemView.findViewById(R.id.btnFavorite); // Changed ID
        }
    }

    // Returns the lesson object at the given position in the list.
    public Lesson getLessonAt(int position) {
        return lessonList.get(position);
    }

}