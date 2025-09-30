package com.kinneret.rcook;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.kinneret.rcook.model.User;
import com.kinneret.rcook.model.RCookRoomDatabase;
import com.kinneret.rcook.model.UserDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UserDaoTest {

    private RCookRoomDatabase database;
    private UserDao userDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, RCookRoomDatabase.class)
                .allowMainThreadQueries()
                .build();
        userDao = database.userDao();
    }

    @After
    public void closeDb() throws IOException {
        database.close();
    }

    @Test
    public void insertAndGetUser() {
        User user = new User("current_user", "Rotem", "Swisa",
                "Student", "Beginners", "0501234567",
                25, "female", "default");

        userDao.insertOrUpdate(user);

        User retrievedUser = userDao.getUserById("current_user");

        assertThat(retrievedUser.getFirstName(), equalTo("Rotem"));
        assertThat(retrievedUser.getRole(), equalTo("Student"));
    }

    @Test
    public void updateUserRole() {
        User user = new User("current_user", "Rotem", "Swisa",
                "Student", "Beginners", "0501234567",
                25, "female", "default");

        userDao.insertOrUpdate(user);
        userDao.updateUserRole("current_user", "Guide");

        User updatedUser = userDao.getUserById("current_user");

        assertThat(updatedUser.getRole(), equalTo("Guide"));
    }
}
