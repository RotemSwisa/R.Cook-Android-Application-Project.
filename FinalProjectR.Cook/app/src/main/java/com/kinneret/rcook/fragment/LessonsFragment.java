package com.kinneret.rcook.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kinneret.rcook.R;
import com.kinneret.rcook.activity.EditLessonActivity;
import com.kinneret.rcook.activity.LessonDetailActivity;
import com.kinneret.rcook.adapter.LessonsAdapter;
import com.kinneret.rcook.model.DataManager;
import com.kinneret.rcook.model.Lesson;
import com.kinneret.rcook.model.LessonViewModel;
import com.kinneret.rcook.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of lessons filtered by level (Beginners, Medium, Advanced).
 * Shows edit/delete options only for guide users.
 * Supports swipe-to-delete with custom icon and role-based behavior.
 */
public class LessonsFragment extends Fragment implements LessonsAdapter.OnLessonClickListener {

    public static final String EXTRA_LESSON_ID = "lesson_id";
    private static final String ARG_LEVEL = "level";
    private String level;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private LessonsAdapter adapter;
    private List<Lesson> lessonList = new ArrayList<>();
    private boolean isUserGuide = false;
    private LessonViewModel lessonViewModel;
    private User currentUser;
    private ItemTouchHelper itemTouchHelper;

    // Creates a new LessonsFragment instance for the specified level (used by ViewPager).
    public static LessonsFragment newInstance(String level) {
        LessonsFragment fragment = new LessonsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LEVEL, level);
        fragment.setArguments(args);
        return fragment;
    }

    // Initializes ViewModel and retrieves the selected lesson level from arguments.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            level = getArguments().getString(ARG_LEVEL);
        }

        // Initialize ViewModel
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Set ViewModel reference in DataManager
        DataManager.getInstance(requireContext()).setLessonViewModel(lessonViewModel);
    }

    // Inflates the layout for the lessons list fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lessons_list, container, false);
    }

    // Sets up RecyclerView, registers for role changes, observes user data and loads lessons.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LessonsAdapter(requireContext(), lessonList, this, isUserGuide);
        recyclerView.setAdapter(adapter);

        // Register for role change broadcasts
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(roleChangeReceiver,
                new IntentFilter("com.kinneret.rcook.USER_ROLE_CHANGED")
        );

        lessonViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            currentUser = user;
            isUserGuide = "Guide".equalsIgnoreCase(user.getRole());

            adapter = new LessonsAdapter(requireContext(), lessonList, this, isUserGuide);
            recyclerView.setAdapter(adapter);

            if (itemTouchHelper != null) {
                itemTouchHelper.attachToRecyclerView(null);
                itemTouchHelper = null;
            }

            if (isUserGuide) {
                attachSwipeToDelete();
            }

            loadLessonData();
        });
    }

    // Listens for changes in user role (Guide/Student) and updates adapter + swipe behavior accordingly.
    private final BroadcastReceiver roleChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Force refresh of user data and permissions
            if (lessonViewModel != null) {
                lessonViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                    if (user != null) {
                        currentUser = user;
                        boolean wasGuide = isUserGuide;
                        isUserGuide = "Guide".equalsIgnoreCase(user.getRole());

                        // Only recreate adapter if role actually changed
                        if (wasGuide != isUserGuide) {
                            adapter = new LessonsAdapter(requireContext(), lessonList,
                                    LessonsFragment.this, isUserGuide);
                            recyclerView.setAdapter(adapter);

                            // Reset swipe functionality
                            if (itemTouchHelper != null) {
                                itemTouchHelper.attachToRecyclerView(null);
                            }

                            if (isUserGuide) {
                                attachSwipeToDelete();
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    };

    // Unregisters broadcast receiver to prevent memory leaks.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(roleChangeReceiver);
        } catch (Exception e) {
            Log.e("LessonsFragment", "Error unregistering receiver: " + e.getMessage());
        }
    }

    // Enables swipe-to-delete only for lessons created by the current guide.
    // Shows a circular red delete icon with border and haptic feedback.
    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (!isUserGuide) return 0; // No swipe if not guide

                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return 0;

                Lesson lesson = adapter.getLessonAt(position);
                if (!isUserCreatedLesson(lesson)) {
                    return 0; // Disable swipe for preloaded lessons
                }
                return ItemTouchHelper.LEFT;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Lesson lessonToDelete = adapter.getLessonAt(position);
                    showDeleteLessonDialog(lessonToDelete, position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    View itemView = viewHolder.itemView;
                    float alpha = Math.min(1.0f, Math.abs(dX) / (itemView.getWidth() * 0.3f));

                    try {
                        Drawable icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete);
                        if (icon != null) {
                            int itemHeight = itemView.getHeight();

                            // Increased icon size (previously itemHeight / 3)
                            int iconSize = Math.min(itemHeight / 2, 120);

                            int iconMargin = (itemHeight - iconSize) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconLeft = itemView.getRight() - iconMargin - iconSize - (int) Math.abs(dX / 4);
                            int iconRight = iconLeft + iconSize;
                            int iconBottom = iconTop + iconSize;

                            if (iconLeft > itemView.getLeft()) {
                                // 🟥 Draw filled red circle
                                Paint circlePaint = new Paint();
                                circlePaint.setAntiAlias(true);
                                circlePaint.setColor(ContextCompat.getColor(requireContext(), R.color.errorColor));
                                circlePaint.setAlpha((int) (255 * alpha));

                                // ⬛️ Draw circle border (stroke)
                                Paint borderPaint = new Paint();
                                borderPaint.setAntiAlias(true);
                                borderPaint.setStyle(Paint.Style.STROKE);
                                borderPaint.setStrokeWidth(4f); // Thin border
                                borderPaint.setColor(ContextCompat.getColor(requireContext(), R.color.colorRed)); // Darker red

                                float centerX = (iconLeft + iconRight) / 2f;
                                float centerY = (iconTop + iconBottom) / 2f;
                                float radius = iconSize * 0.85f; // Increased radius

                                // Draw inner circle
                                c.drawCircle(centerX, centerY, radius, circlePaint);

                                // Draw border on top
                                c.drawCircle(centerX, centerY, radius, borderPaint);

                                // 🗑 Draw the delete icon
                                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                                icon.setTint(ContextCompat.getColor(requireContext(), R.color.purple_200)); // צבע כהה יותר לאייקון
                                icon.setAlpha((int) (255 * alpha));
                                icon.draw(c);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("LessonsFragment", "Error drawing delete icon: " + e.getMessage());
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && viewHolder != null) {
                    // Add haptic feedback for better touch response
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                }
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // Returns true if the lesson was created by the current user (guide).
    private boolean isUserCreatedLesson(Lesson lesson) {
        if (lesson == null || currentUser == null) return false;
        return lesson.getGuideName().equalsIgnoreCase(currentUser.getFullName());
    }

    // Shows a confirmation dialog before deleting a lesson. Cancels swipe if dismissed.
    private void showDeleteLessonDialog(Lesson lesson, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Lesson")
                .setMessage("Are you sure you want to delete this lesson?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    DataManager.getInstance(requireContext()).deleteLesson(lesson);
                    Toast.makeText(getContext(), "Lesson deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyItemChanged(position); // Cancel swipe
                })
                .setCancelable(false)
                .show();
    }

    // Observes LiveData for lessons by level and updates the RecyclerView accordingly.
    private void loadLessonData() {
        // Observe lessons for this level using LiveData
        lessonViewModel.getLessonsByLevel(level).observe(getViewLifecycleOwner(), lessons -> {
            if (lessons != null) {
                lessonList.clear();
                lessonList.addAll(lessons);

                // Show empty view if no lessons
                if (lessonList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }

                // Notify adapter of data change
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Opens LessonDetailActivity when a lesson is clicked.
    @Override
    public void onLessonClick(Lesson lesson) {
        // Open lesson detail
        Intent intent = new Intent(getActivity(), LessonDetailActivity.class);
        intent.putExtra(LessonDetailActivity.EXTRA_LESSON_ID, lesson.getId());
        intent.putExtra("title", lesson.getName());
        intent.putExtra("guide", lesson.getGuideName());
        intent.putExtra("description", lesson.getFullDescription());
        intent.putExtra("video", lesson.getVideoUrl());
        startActivity(intent);
    }

    // Opens EditLessonActivity when a guide long-clicks a lesson item.
    @Override
    public void onLessonLongClick(Lesson lesson, int position) {
        if (isUserGuide) {
            // Open edit lesson screen
            Intent intent = new Intent(getActivity(), EditLessonActivity.class);
            intent.putExtra(EditLessonActivity.EXTRA_LESSON_ID, lesson.getId());
            startActivity(intent);
        }
    }
}