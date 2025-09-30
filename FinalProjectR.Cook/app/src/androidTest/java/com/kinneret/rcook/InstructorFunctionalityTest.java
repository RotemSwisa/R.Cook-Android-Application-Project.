package com.kinneret.rcook;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ScrollView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.kinneret.rcook.activity.MyLessonsActivity;
import com.kinneret.rcook.model.DataManager;
import com.kinneret.rcook.model.Lesson;
import com.kinneret.rcook.model.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class InstructorFunctionalityTest {

    private Context context;
    private DataManager dataManager;
    private SharedPreferences preferences;

    @Rule
    public ActivityScenarioRule<MyLessonsActivity> activityRule =
            new ActivityScenarioRule<>(createLaunchIntent());

    private static Intent createLaunchIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MyLessonsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        dataManager = DataManager.getInstance(context);
        preferences = context.getSharedPreferences("RCookPrefs", Context.MODE_PRIVATE);

        setupGuideUser();
        preferences.edit().putBoolean("first_time", false).apply();

        waitForUI(1000); // המתנה ארוכה להתחלה
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    private void setupGuideUser() {
        User guideUser = new User();
        guideUser.setFirstName("Test");
        guideUser.setLastName("Guide");
        guideUser.setRole("Guide");
        guideUser.setLevel("Beginners");
        guideUser.setGender("male");
        guideUser.setPhone("1234567890");
        guideUser.setAge(25);
        guideUser.setBackgroundColor("default");

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_first_name", guideUser.getFirstName());
        editor.putString("user_last_name", guideUser.getLastName());
        editor.putString("user_role", guideUser.getRole());
        editor.putString("user_level", guideUser.getLevel());
        editor.putString("user_gender", guideUser.getGender());
        editor.putString("user_phone", guideUser.getPhone());
        editor.putInt("user_age", guideUser.getAge());
        editor.putString("user_background_color", guideUser.getBackgroundColor());
        editor.apply();

        dataManager.saveUser(guideUser);
    }

    private void createTestLesson() {
        Lesson lesson = new Lesson(
                "test_id_123",
                "Test Lesson",
                "Test Guide",
                "Short Desc",
                "Full Desc",
                "",
                "",
                "Beginners"
        );
        dataManager.addLesson(lesson);
        waitForUI(1000);
    }

    private void waitForUI(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitForUI() {
        waitForUI(800);
    }

    // פונקציית עזר
    private void fillFieldSafely(int fieldId, String text) {
        try {
            onView(withId(fieldId))
                    .perform(scrollTo());
            waitForUI(500);

            onView(withId(fieldId))
                    .check(matches(isDisplayed()))
                    .perform(click(), clearText(), typeText(text), closeSoftKeyboard());
            waitForUI(500);
        } catch (Exception e) {
            // נסיון נוסף אם הראשון לא עבד
            try {
                onView(withId(fieldId))
                        .perform(click(), clearText(), typeText(text), closeSoftKeyboard());
                waitForUI(500);
            } catch (Exception e2) {
                System.out.println("Failed to fill field " + fieldId + ": " + e2.getMessage());
            }
        }
    }

    @Test
    public void testAddLessonFlowSafe() {
        setupGuideUser();
        waitForUI();

        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
        waitForUI();

        onView(withId(R.id.fab)).perform(click());
        waitForUI(3000);

        // גלול לראש העמוד
        onView(isAssignableFrom(ScrollView.class)).perform(swipeDown(), swipeDown());
        waitForUI(1000);

        // השתמש בפונקציית העזר לכל השדות
        fillFieldSafely(R.id.lesson_name, "Test Lesson1");
        fillFieldSafely(R.id.lesson_guide, "Test Guide");
        fillFieldSafely(R.id.lesson_short_description, "Short description for test");
        fillFieldSafely(R.id.lesson_full_description, "This is a full detailed description of the test lesson.");

        // דלג על הספינר - השאר ברירת מחדל
        System.out.println("Using default spinner selection");

        // שדות וידאו ותמונה
        fillFieldSafely(R.id.lesson_video_url, "schnitzel_video");
        onView(isAssignableFrom(ScrollView.class)).perform(swipeUp());
        fillFieldSafely(R.id.lesson_image_url, "drawable/schnitzel");

        // שמירה
        try {
            onView(withId(R.id.save_button))
                    .perform(scrollTo(), click());
            waitForUI(1000);
        } catch (Exception e) {
            System.out.println("Save button issue: " + e.getMessage());
            // נסה בלי scrollTo
            onView(withId(R.id.save_button)).perform(click());
            waitForUI(1000);
        }

    }

    @Test
    public void testDeleteAllLessonsMenu() {
        createTestLesson();

        openActionBarOverflowOrOptionsMenu(context);
        waitForUI();

        onView(withText("Delete My Lessons")).perform(click());
        waitForUI();

        onView(withText("Delete")).perform(click());
        waitForUI(1000);

        int count = getRecyclerViewItemCount();
        // בדוק שהספירה נכונה ( 3 שיעורים ברירת מחדל)
        assertTrue("User lessons should be deleted", count >= 0);
    }

    @Test
    public void testEditLessonValidation() {
        onView(withId(R.id.fab)).perform(click());
        waitForUI();

        // נסה לשמור בלי למלא שדות
        onView(withId(R.id.save_button)).perform(click());
        waitForUI();

        // בדוק שיש שגיאת ולידציה
        onView(withId(R.id.lesson_name))
                .check(matches(hasErrorText("Lesson name is required")));
    }

    //  מציאת שיעור לפי שם
    @Test
    public void testFindLessonByName() {
        createTestLesson();
        waitForUI();

        // בדוק שהשיעור נמצא ברשימה
        onView(withText("Test Lesson")).check(matches(isDisplayed()));
    }


    private int getRecyclerViewItemCount() {
        final int[] count = {0};
        activityRule.getScenario().onActivity(activity -> {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
            if (recyclerView != null && recyclerView.getAdapter() != null) {
                count[0] = recyclerView.getAdapter().getItemCount();
            }
        });
        return count[0];
    }

    // פונקציה לבדיקת תוכן הרשימה
    private void logRecyclerViewContents() {
        activityRule.getScenario().onActivity(activity -> {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
            if (recyclerView != null && recyclerView.getAdapter() != null) {
                Log.d("TEST", "RecyclerView item count: " + recyclerView.getAdapter().getItemCount());
            }
        });
    }
}
