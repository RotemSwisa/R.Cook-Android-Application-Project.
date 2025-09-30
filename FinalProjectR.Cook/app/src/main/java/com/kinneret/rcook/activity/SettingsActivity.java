package com.kinneret.rcook.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kinneret.rcook.R;
import com.kinneret.rcook.model.User;
import com.kinneret.rcook.util.BackgroundColorUtils;

public class SettingsActivity extends BaseActivity {

    private EditText phoneEditText, ageEditText;
    private Spinner roleSpinner, levelSpinner, genderSpinner;
    private Button saveButton, cancelButton;
    private Button colorGrayButton, colorBlueButton, colorYellowButton, colorDefaultButton;
    private String selectedBackgroundColor = null;

    // Initializes the settings screen, including toolbar, input fields, spinners,
    // color options, and button listeners. Waits for user data to populate fields.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup toolbar first
        initializeToolbar();

        // Initialize views
        initViews();

        // Setup spinners with arrays
        setupSpinners();

        // Setup click listeners
        setupClickListeners();
    }

    // Finds and links all input fields, spinners, color buttons, and action buttons from the layout.
    private void initViews() {
        phoneEditText = findViewById(R.id.phone_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        roleSpinner = findViewById(R.id.role_spinner);
        levelSpinner = findViewById(R.id.level_spinner);
        genderSpinner = findViewById(R.id.gender_spinner);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        // Color buttons
        colorGrayButton = findViewById(R.id.color_gray_button);
        colorBlueButton = findViewById(R.id.color_blue_button);
        colorYellowButton = findViewById(R.id.color_yellow_button);
        colorDefaultButton = findViewById(R.id.color_default_button);
    }

    // Logs the role update; user data will be loaded separately in onUserDataLoaded().
    @Override
    protected void onUserRoleUpdated(boolean isGuide) {
        Log.d("SettingsActivity", "User role updated to: " + (isGuide ? "Guide" : "Student"));
    }

    // Called when user data is available. Loads the settings into the form fields.
    @Override
    protected void onUserDataLoaded(User user) {
        loadUserSettings(user);
        Log.d("SettingsActivity", "User data loaded and settings populated");
    }

    // Populates the form fields and spinners with values from the user object.
    private void loadUserSettings(User user) {
        if (user.getPhone() != null) {
            phoneEditText.setText(user.getPhone());
        }

        if (user.getAge() > 0) {
            ageEditText.setText(String.valueOf(user.getAge()));
        }

        setSpinnerSelection(roleSpinner, user.getRole() != null ? user.getRole() : "Student");
        setSpinnerSelection(levelSpinner, user.getLevel() != null ? user.getLevel() : "Beginners");
        setSpinnerSelection(genderSpinner, user.getGender() != null ? user.getGender() : "male");
    }

    // Sets up the role, level, and gender dropdown menus using predefined string arrays.
    private void setupSpinners() {
        // Role spinner
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.role_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Level spinner
        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(this,
                R.array.levels_array, android.R.layout.simple_spinner_item);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelAdapter);

        // Gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
    }

    // Sets the click actions for save, cancel, and background color buttons.
    // Color buttons will not apply immediately but will be saved for later.
    private void setupClickListeners() {
        // Save button
        saveButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveSettings();
                Toast.makeText(SettingsActivity.this, "Settings saved successfully!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());

        // Color buttons - these will now apply to entire app
        colorGrayButton.setOnClickListener(v -> changeBackgroundColor("gray"));
        colorBlueButton.setOnClickListener(v -> changeBackgroundColor("blue"));
        colorYellowButton.setOnClickListener(v -> changeBackgroundColor("yellowish"));
        colorDefaultButton.setOnClickListener(v -> changeBackgroundColor("default"));
    }

    // Validates the phone number and age fields before saving.
    // Ensures proper format and range (age 1–120, phone 10 digits).
    private boolean validateInput() {
        // Validate phone
        String phone = phoneEditText.getText().toString().trim();
        if (!phone.isEmpty() && (phone.length() < 10 || !phone.matches("\\d+"))) {
            phoneEditText.setError("Please enter a valid phone number");
            phoneEditText.requestFocus();
            return false;
        }

        // Validate age
        String age = ageEditText.getText().toString().trim();
        if (!age.isEmpty()) {
            try {
                int ageInt = Integer.parseInt(age);
                if (ageInt < 1 || ageInt > 120) {
                    ageEditText.setError("Please enter a valid age (1-120)");
                    ageEditText.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                ageEditText.setError("Please enter a valid age");
                ageEditText.requestFocus();
                return false;
            }
        }

        return true;
    }

    // Updates the current user with all form values including phone, age, role, level, gender,
    // and selected background color. Saves changes to the database and SharedPreferences,
    // and refreshes the activity if role has changed.
    private void saveSettings() {
        if (currentUser == null) {
            Log.e("SettingsActivity", "Cannot save - currentUser is null");
            Toast.makeText(this, "Error: User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug: Log current user state before updating
        Log.d("SettingsActivity", "Before update - User: " + currentUser.getFullName() +
                ", role=" + currentUser.getRole() +
                ", phone=" + currentUser.getPhone() +
                ", age=" + currentUser.getAge() +
                ", gender=" + currentUser.getGender());

        // Get values from form
        String phoneValue = phoneEditText.getText().toString().trim();
        String ageValue = ageEditText.getText().toString().trim();
        String roleValue = roleSpinner.getSelectedItem().toString();
        String levelValue = levelSpinner.getSelectedItem().toString();
        String genderValue = genderSpinner.getSelectedItem().toString();

        // Log what we're about to save
        Log.d("SettingsActivity", "Form values - phone=" + phoneValue +
                ", age=" + ageValue +
                ", role=" + roleValue +
                ", level=" + levelValue +
                ", gender=" + genderValue);

        // Update user object with new values
        currentUser.setPhone(phoneValue);

        if (!ageValue.isEmpty()) {
            try {
                currentUser.setAge(Integer.parseInt(ageValue));
            } catch (NumberFormatException e) {
                currentUser.setAge(0);
            }
        } else {
            currentUser.setAge(0);
        }

        // Store the old role to compare later
        String oldRole = currentUser.getRole();

        currentUser.setRole(roleValue);
        currentUser.setLevel(levelValue);
        currentUser.setGender(genderValue);

        // Update background color in User model if one was selected
        if (selectedBackgroundColor != null) {
            currentUser.setBackgroundColor(selectedBackgroundColor);
        }

        // Debug: Log after update
        Log.d("SettingsActivity", "After update - User: " +
                "phone=" + currentUser.getPhone() +
                ", age=" + currentUser.getAge() +
                ", role=" + currentUser.getRole() +
                ", level=" + currentUser.getLevel() +
                ", gender=" + currentUser.getGender());

        // Save updated user to database
        dataManager.saveUser(currentUser);

        // Check if role actually changed
        String newRole = currentUser.getRole();
        boolean roleChanged = !oldRole.equals(newRole);

        if (roleChanged) {
            // Send broadcast for role change
            Intent intent = new Intent("com.kinneret.rcook.USER_ROLE_CHANGED");
            intent.putExtra("new_role", newRole);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // Recreate the current activity to refresh everything
            recreate();
        }

        // Also save to SharedPreferences as backup
        if (selectedBackgroundColor != null) {
            getSharedPreferences("app_settings", MODE_PRIVATE)
                    .edit()
                    .putString("background_color", selectedBackgroundColor)
                    .apply();

            // Apply the color immediately after saving
            BackgroundColorUtils.applyBackgroundColor(this, selectedBackgroundColor);
        }
    }

    // Temporarily stores the selected background color until the user presses Save.
    // Shows feedback to the user and updates the UI to reflect the selected button.
    private void changeBackgroundColor(String color) {
        selectedBackgroundColor = color;

        Toast.makeText(this, "Background color selected: " + color + " (will apply after saving)",
                Toast.LENGTH_SHORT).show();
        resetColorButtonsAppearance();
        highlightSelectedColorButton(color);
    }

    // Resets all background color buttons to their default appearance.
    private void resetColorButtonsAppearance() {
        colorGrayButton.setAlpha(1.0f);
        colorBlueButton.setAlpha(1.0f);
        colorYellowButton.setAlpha(1.0f);
        colorDefaultButton.setAlpha(1.0f);
    }

    // Highlight the selected button (make others slightly transparent)
    private void highlightSelectedColorButton(String color) {
        colorGrayButton.setAlpha(color.equals("gray") ? 1.0f : 0.5f);
        colorBlueButton.setAlpha(color.equals("blue") ? 1.0f : 0.5f);
        colorYellowButton.setAlpha(color.equals("yellowish") ? 1.0f : 0.5f);
        colorDefaultButton.setAlpha(color.equals("default") ? 1.0f : 0.5f);
    }

    // Selects the matching spinner option based on the provided string value.
    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}