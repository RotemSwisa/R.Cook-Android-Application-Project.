package com.kinneret.rcook.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.kinneret.rcook.R;
import com.kinneret.rcook.model.DataManager;
import com.kinneret.rcook.model.LessonViewModel;
import com.kinneret.rcook.model.User;

public class WelcomeActivity extends AppCompatActivity {

    private EditText editFirstName, editLastName;
    private Spinner spinnerRole;
    private Button buttonContinue;
    private CheckBox buttonHealth;
    private TextView textHealthWarning;
    private boolean healthDeclarationAgreed = false;
    private DataManager dataManager;
    private LessonViewModel lessonViewModel;

    // Called when the WelcomeActivity is first created.
    // Initializes views, sets up the ViewModel and DataManager, checks if user is already logged in,
    // and handles user interface interactions.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize ViewModel first
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Initialize DataManager and set ViewModel reference
        dataManager = DataManager.getInstance(this);
        dataManager.setLessonViewModel(lessonViewModel);

        // Check if user already exists - use LiveData observer
        if (lessonViewModel.getCurrentUser() != null) {
            lessonViewModel.getCurrentUser().observe(this, user -> {
                if (!dataManager.isFirstTimeUser() && user != null) {
                    // If user already setup, go directly to lessons
                    startActivity(new Intent(WelcomeActivity.this, MyLessonsActivity.class));
                    finish();
                }
            });
        }
        // Initialize views
        initializeViews();

        // Setup spinner for roles
        setupRoleSpinner();

        // Setup click listeners
        setupClickListeners();
    }

    // Finds and links all views from the XML layout file to their corresponding Java variables.
    private void initializeViews() {
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        spinnerRole = findViewById(R.id.spinnerRole);
        buttonHealth = findViewById(R.id.buttonHealth);
        buttonContinue = findViewById(R.id.buttonContinue);
        textHealthWarning = findViewById(R.id.textHealthWarning);
    }

    // Populates the role selection spinner with options like "Student" and "Guide".
    private void setupRoleSpinner() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Student", "Guide"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    // Sets up listeners for the health declaration checkbox and the continue button.
    // Clicking the checkbox opens a health declaration popup.
    // Clicking "Continue" validates inputs and proceeds only if health declaration is accepted.
    private void setupClickListeners() {
        // Setup click listeners
        buttonHealth.setOnClickListener(v -> {
            buttonHealth.setChecked(false);
            showHealthDeclarationPopup();
        });

        buttonContinue.setOnClickListener(v -> {
            if (validateInputs()) {
                if (healthDeclarationAgreed) {
                    saveUserData();
                } else {
                    textHealthWarning.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    // Displays a popup dialog with the health declaration.
    // If the user agrees, the checkbox is marked and the warning is hidden.
    // If the user cancels, the checkbox is unchecked and a warning message is shown.
    private void showHealthDeclarationPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_health_declaration);
        dialog.setCancelable(false);

        Button buttonAgree = dialog.findViewById(R.id.buttonAgree);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);

        buttonAgree.setOnClickListener(v -> {
            healthDeclarationAgreed = true;
            buttonHealth.setChecked(true);  // Check the box
            textHealthWarning.setVisibility(View.GONE); // Hidden
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> {
            healthDeclarationAgreed = false;
            buttonHealth.setChecked(false); // Uncheck
            textHealthWarning.setVisibility(View.VISIBLE); // A message is displayed.
            dialog.dismiss();
        });

        dialog.show();
    }

    // Validates that the user entered both first name and last name.
    // Shows an error if either field is empty.
    private boolean validateInputs() {
        boolean isValid = true;

        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();

        if (firstName.isEmpty()) {
            editFirstName.setError("Enter your First name.");
            isValid = false;
        }

        if (lastName.isEmpty()) {
            editLastName.setError("Enter your Last name.");
            isValid = false;
        }

        return isValid;
    }

    // Saves the user’s first name, last name, and role to the DataManager,
    // marks the user as having completed the initial setup,
    // and navigates to the MyLessonsActivity screen.
    private void saveUserData() {
        User user = new User();
        user.setFirstName(editFirstName.getText().toString().trim());
        user.setLastName(editLastName.getText().toString().trim());
        user.setRole(spinnerRole.getSelectedItem().toString());

        // Save to DataManager
        dataManager.saveUser(user);

        // Mark first time as done
        dataManager.setFirstTimeDone();

        // Navigate to main activity
        startActivity(new Intent(WelcomeActivity.this, MyLessonsActivity.class));
        finish();
    }
}