package com.kinneret.rcook.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.kinneret.rcook.R;
import com.kinneret.rcook.adapter.LevelsPagerAdapter;
import com.kinneret.rcook.model.DataManager;
import com.kinneret.rcook.model.Lesson;

import java.util.List;

public class MyLessonsActivity extends BaseActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fab;

    private final BroadcastReceiver userRoleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Re-check the user role and refresh UI accordingly
            DataManager.getInstance(context).getCurrentUser().observe(MyLessonsActivity.this, user -> {
                if (user != null) {
                    isUserGuide = "Guide".equals(user.getRole());
                    setupFAB(); // Update FAB visibility
                    invalidateOptionsMenu(); // Refresh options menu (settings/delete)
                }
            });
        }
    };

    // Called when the activity is first created. Initializes the layout, toolbar, ViewPager,
    // and sets up the user's tabs and FAB visibility according to their role.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting status bar
        setupStatusBar();
        setContentView(R.layout.activity_my_lessons);

        // Setup toolbar (will be updated when user data loads)
        initializeToolbar();

        // Initialize views
        initViews();

        // Setup ViewPager and Tabs
        setupViewPagerAndTabs();
    }

    // Triggered when the user's role is loaded or updated. Refreshes the FAB based on the role.
    @Override
    protected void onUserRoleUpdated(boolean isGuide) {
        // Setup FAB based on user role
        setupFAB();
    }

    // Triggered when the user's selected level is updated. Sets the default tab accordingly.
    @Override
    protected void onUserLevelUpdated(String level) {
        // Set user level as default tab
        setDefaultTabByUserLevel(level);
    }

    // Selects the default tab (Beginner / Medium / Advanced) based on the user's saved level.
    private void setDefaultTabByUserLevel(String userLevel) {
        if (userLevel != null && viewPager != null) {
            switch (userLevel) {
                case "Beginners":
                    viewPager.setCurrentItem(0);
                    break;
                case "Medium":
                    viewPager.setCurrentItem(1);
                    break;
                case "Advanced":
                    viewPager.setCurrentItem(2);
                    break;
            }
        }
    }

    // Customizes the status bar color to match the app's theme if supported by the OS.
    private void setupStatusBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorStatusBar));

        }
    }

    // Links all view components from the XML layout to their corresponding Java variables.
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        fab = findViewById(R.id.fab);
    }

    // Sets up the ViewPager with the tab adapter and customizes tab appearance and behavior.
    private void setupViewPagerAndTabs() {
        LevelsPagerAdapter pagerAdapter = new LevelsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                // Set background color for each tab
                tab.view.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
                tab.view.setPadding(16, 12, 16, 12);
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tab selected - indicator will automatically show
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Tab unselected
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tab reselected
            }
        });
    }

    // Displays the floating action button only for users with the 'Guide' role.
    // Clicking it navigates to the screen for adding/editing a lesson.
    private void setupFAB() {
        // Show FAB only for guide users
        if (isUserGuide()) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(MyLessonsActivity.this, EditLessonActivity.class);
                startActivity(intent);
            });
        } else {
            fab.setVisibility(View.GONE);
        }
    }

    // Inflates the options menu (settings and delete).
    // Hides the "delete" option for non-guide users.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lessons, menu);
        MenuItem deleteItem = menu.findItem(R.id.action_delete_my_lessons);
        if (!isUserGuide()) {
            deleteItem.setVisible(false);
        }
        return true;
    }

    // Handles menu item clicks: opens Settings or shows a confirmation dialog for deleting all lessons.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_delete_my_lessons && isUserGuide()) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Displays a confirmation dialog before deleting all lessons created by the user.
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All My Lessons")
                .setMessage("Are you sure you want to delete all lessons you've created? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserCreatedLessons())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Deletes all lessons where the current user is listed as the guide.
    // Provides user feedback based on deletion success or failure.
    private void deleteUserCreatedLessons() {
        if (currentUser == null) {
            Toast.makeText(this, "User information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String myName = currentUser.getFullName();
        Log.d("DeleteLessons", "Deleting lessons for user: " + myName);

        lessonViewModel.getAllLessons().observe(this, new Observer<List<Lesson>>() {
            @Override
            public void onChanged(List<Lesson> lessons) {
                // Remove observer to prevent repeated calls
                lessonViewModel.getAllLessons().removeObserver(this);

                if (lessons == null || lessons.isEmpty()) {
                    Toast.makeText(MyLessonsActivity.this, "No lessons found to delete", Toast.LENGTH_SHORT).show();
                    return;
                }

                int deletedCount = 0;
                for (Lesson lesson : lessons) {
                    if (lesson.getGuideName() != null &&
                            lesson.getGuideName().trim().equalsIgnoreCase(myName.trim())) {
                        DataManager.getInstance(MyLessonsActivity.this).deleteLesson(lesson);
                        deletedCount++;
                        Log.d("DeleteLessons", "Deleting lesson: " + lesson.getName());
                    }
                }

                if (deletedCount > 0) {
                    Toast.makeText(MyLessonsActivity.this,
                            deletedCount + " lessons have been deleted.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MyLessonsActivity.this,
                            "No lessons created by you were found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Utility function to observe LiveData only once and then automatically remove the observer.
    public static <T> void observeOnce(final LiveData<T> liveData, final LifecycleOwner owner, final Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
    }

    // Unregisters the broadcast receiver when the activity is paused to prevent memory leaks.
    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening to avoid leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userRoleReceiver);
    }

}