package com.kinneret.rcook;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.kinneret.rcook.activity.WelcomeActivity;
import com.kinneret.rcook.model.RCookRoomDatabase;
import com.kinneret.rcook.model.UserDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class WelcomeActivityTest {

    @Rule
    public ActivityScenarioRule<WelcomeActivity> activityRule =
            new ActivityScenarioRule<>(WelcomeActivity.class);

    @Before
    public void setUp() {
        // ניקוי SharedPreferences לפני כל בדיקה
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply(); // שימוש ב-apply() במקום commit()

        // ניקוי מסד הנתונים - יצירת מופע חדש ומחיקת כל הטבלאות
        RCookRoomDatabase database = Room.databaseBuilder(context, RCookRoomDatabase.class, "rcook_database")
                .allowMainThreadQueries()
                .build();

        try {
            database.clearAllTables();

            // מחיקה נוספת של המשתמש מהמסד - וידוא שלא נשאר כלום
            UserDao userDao = database.userDao();
            userDao.deleteUser("current_user");

            database.close();
        } catch (Exception e) {
            // במקרה של שגיאה, ננסה למחוק את כל הבסיס
            context.deleteDatabase("rcook_database");
        }

        // המתנה קצרה כדי לוודא שכל הפעולות הושלמו
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testWelcomeFlowWithValidData() {
        // מילוי שם פרטי ושם משפחה
        onView(withId(R.id.editFirstName))
                .perform(typeText("Rotem"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.editLastName))
                .perform(typeText("Swisa"), ViewActions.closeSoftKeyboard());

        // בחירת תפקיד מהספינר
        onView(withId(R.id.spinnerRole)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Guide"))).perform(click());

        // לחיצה על כפתור הצהרת הבריאות
        onView(withId(R.id.buttonHealth)).perform(click());

        // אישור הצהרת הבריאות
        onView(withText("I Agree")).perform(click());

        // בדיקה שהצ'קבוקס מסומן
        onView(withId(R.id.buttonHealth)).check(matches(isChecked()));

        // לחיצה על כפתור המשך
        onView(withId(R.id.buttonContinue)).perform(click());

        // בדיקה שעברנו למסך הבא
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
    }

    @Test
    public void testValidationErrorsDisplayed() {
        // לחיצה על המשך בלי למלא שדות
        onView(withId(R.id.buttonContinue)).perform(click());

        // בדיקת הודעות שגיאה
        onView(withId(R.id.editFirstName))
                .check(matches(hasErrorText("Enter your First name.")));
        onView(withId(R.id.editLastName))
                .check(matches(hasErrorText("Enter your Last name.")));
    }

    @Test
    public void testHealthDeclarationRequired() {
        // מילוי כל השדות מלבד הצהרת הבריאות
        onView(withId(R.id.editFirstName))
                .perform(typeText("Rotem"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.editLastName))
                .perform(typeText("Swisa"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.spinnerRole)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Student"))).perform(click());

        // לחיצה על המשך בלי אישור הצהרת הבריאות
        onView(withId(R.id.buttonContinue)).perform(click());

        // בדיקה שמופיעה הודעת שגיאה
        onView(withText("Please note that you cannot continue to use the app without consent from the health declaration."))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testHealthDeclarationCancelFlow() {
        // מילוי שדות
        onView(withId(R.id.editFirstName))
                .perform(typeText("Rotem"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.editLastName))
                .perform(typeText("Swisa"), ViewActions.closeSoftKeyboard());

        // פתיחת הצהרת הבריאות
        onView(withId(R.id.buttonHealth)).perform(click());

        // לחיצה על ביטול
        onView(withText("Cancel")).perform(click());

        // בדיקה שהצ'קבוקס לא מסומן
        onView(withId(R.id.buttonHealth)).check(matches(not(isChecked())));

        // ניסיון להמשיך
        onView(withId(R.id.buttonContinue)).perform(click());

        // בדיקה שמופיעה הודעת שגיאה
        onView(withText("Please note that you cannot continue to use the app without consent from the health declaration."))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSpinnerRoleSelection() {
        // בדיקת בחירת תפקיד שונה
        onView(withId(R.id.spinnerRole)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Student"))).perform(click());

        // בדיקה שהתפקיד נבחר
        onView(withId(R.id.spinnerRole)).check(matches(withSpinnerText("Student")));
    }

    @After
    public void tearDown() {
        // ניקוי יסודי לאחר כל בדיקה
        Context context = ApplicationProvider.getApplicationContext();

        // מחיקת SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // מחיקת מסד הנתונים לחלוטין
        try {
            RCookRoomDatabase database = Room.databaseBuilder(context, RCookRoomDatabase.class, "rcook_database")
                    .allowMainThreadQueries()
                    .build();
            database.clearAllTables();
            database.close();
        } catch (Exception e) {
            // אם יש בעיה, נמחק את כל הבסיס
            context.deleteDatabase("rcook_database");
        }

        // המתנה קצרה לוודא שכל הפעולות הושלמו
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
