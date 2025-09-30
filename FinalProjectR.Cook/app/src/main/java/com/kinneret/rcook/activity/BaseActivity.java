package com.kinneret.rcook.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.kinneret.rcook.R;
import com.kinneret.rcook.model.DataManager;
import com.kinneret.rcook.model.LessonViewModel;
import com.kinneret.rcook.model.User;
import com.kinneret.rcook.util.BackgroundColorUtils;

/**
 * Base activity that handles shared features across all screens:
 * - Applies saved background color based on user preference
 * - Displays user info in the top bar
 * - Notifies child activities when user role or level updates
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected LessonViewModel lessonViewModel;
    protected DataManager dataManager;
    protected User currentUser;
    protected boolean isUserGuide = false;

    // Initializes the ViewModel and DataManager used across all screens.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModel and DataManager
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        dataManager = DataManager.getInstance(this);
        dataManager.setLessonViewModel(lessonViewModel);
    }

    // Applies the saved background color and starts observing the current user.
    @Override
    protected void onResume() {
        super.onResume();

        // Reload saved background color (if any)
        String color = getSharedPreferences("app_settings", MODE_PRIVATE)
                .getString("background_color", "default");
        BackgroundColorUtils.applyBackgroundColor(this, color);

        // Observe user data every time activity resumes
        observeUserData();
    }

    // Observes LiveData for the current user.
    // When data is received, applies settings, updates UI, and notifies child activities.
    private void observeUserData() {
        lessonViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                boolean previousIsGuide = isUserGuide;

                currentUser = user;
                isUserGuide = "Guide".equals(user.getRole());

                // Apply background color
                applyUserBackgroundColor(user.getBackgroundColor());

                // Update user info display
                updateUserInfoDisplay(user);

                if (previousIsGuide != isUserGuide) {
                    onUserRoleUpdated(isUserGuide);
                    invalidateOptionsMenu();
                }

                onUserLevelUpdated(user.getLevel());

                // Notify child activities that user data is loaded
                onUserDataLoaded(user);
            }
        });
    }

    // Sets up the app's custom toolbar and title/subtitle layout.
    protected void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            customizeToolbarLayout();
            setupToolbarTitle();
        }
    }

    // Applies layout rules to title/subtitle for positioning and visibility in the toolbar.
    private void customizeToolbarLayout() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayShowCustomEnabled(false);
            customizeToolbarAppearance();
        }
    }

    // Adjusts font styles, size, and alignment of the toolbar's title and subtitle dynamically.
    private void customizeToolbarAppearance() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            try {
                for (int i = 0; i < toolbar.getChildCount(); i++) {
                    View child = toolbar.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView textView = (TextView) child;
                        CharSequence text = textView.getText();

                        if (text != null && text.toString().contains("R.Cook")) {
                            textView.setTextSize(22f);
                            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                            textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

                            Toolbar.LayoutParams params = (Toolbar.LayoutParams) textView.getLayoutParams();
                            params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                            textView.setLayoutParams(params);

                        } else if (text != null && text.toString().contains("Welcome")) {
                            textView.setTextSize(16f);
                            textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
                            textView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

                            Toolbar.LayoutParams params = (Toolbar.LayoutParams) textView.getLayoutParams();
                            params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                            params.setMarginEnd(16);
                            textView.setLayoutParams(params);
                        }
                    }
                }
            } catch (Exception e) {}
        }
    }

    // Sets the app's title and adds a welcome message if the user is available.
    private void setupToolbarTitle() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("R.Cook");

            if (currentUser != null) {
                updateToolbarSubtitle(currentUser.getFullName());
            }
        }
    }

    // Displays the user's name in the subtitle with custom formatting.
    protected void updateToolbarSubtitle(String userName) {
        if (getSupportActionBar() != null) {
            if (userName != null && !userName.isEmpty()) {
                getSupportActionBar().setSubtitle("Welcome " + userName);

                new Handler().post(() -> customizeToolbarAppearance());
            } else {
                getSupportActionBar().setSubtitle(null);
            }
        }
    }

    // Updates the toolbar with the current user's full name and re-applies custom formatting.
    private void updateUserInfoDisplay(User user) {
        updateToolbarSubtitle(user.getFullName());

        if (getSupportActionBar() != null) {
            String firstName = user.getFirstName();
            if (firstName != null && !firstName.isEmpty()) {
                getSupportActionBar().setTitle("R.Cook");

                new Handler().post(() -> customizeToolbarAppearance());
            }
        }
    }

    // Uses utility class to apply a background color throughout the screen.
    private void applyUserBackgroundColor(String backgroundColor) {
        BackgroundColorUtils.applyBackgroundColor(this, backgroundColor);
    }

    // Meant to be overridden by child activities to respond to role changes.
    protected void onUserRoleUpdated(boolean isGuide) {
        // Override in child classes
    }

    // Meant to be overridden by child activities to respond to level changes.
    protected void onUserLevelUpdated(String level) {
        // Override in child classes
    }

    // Returns the current logged-in user.
    protected User getCurrentUser() {
        return currentUser;
    }

    // Returns true if the user has the 'Guide' role.
    protected boolean isUserGuide() {
        return isUserGuide;
    }

    // Re-applies background color in case it changed since last visit.
    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null && currentUser.getBackgroundColor() != null) {
            applyUserBackgroundColor(currentUser.getBackgroundColor());
        }
    }

    // Meant to be overridden by child activities once user data is ready.
    protected void onUserDataLoaded(User user) {
        // Override in child classes
    }
}