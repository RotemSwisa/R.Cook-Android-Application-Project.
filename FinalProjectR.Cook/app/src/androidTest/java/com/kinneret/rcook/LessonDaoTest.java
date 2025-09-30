package com.kinneret.rcook;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.kinneret.rcook.model.Lesson;
import com.kinneret.rcook.model.LessonDao;
import com.kinneret.rcook.model.RCookRoomDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LessonDaoTest {

    private RCookRoomDatabase database;
    private LessonDao lessonDao;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, RCookRoomDatabase.class)
                .allowMainThreadQueries()
                .build();
        lessonDao = database.lessonDao();
    }

    @After
    public void closeDb() throws IOException {
        database.close();
    }

    @Test
    public void insertAndGetLesson() throws Exception {
        Lesson lesson = new Lesson("test_id", "Tortilla", "Rotem",
                "Short desc", "Full desc",
                "image_url", "video_url", "Beginners", false, false);

        lessonDao.insert(lesson);

        List<Lesson> lessons = LiveDataTestUtil.getValue(lessonDao.getAllLessons());
        assertThat(lessons.size(), equalTo(1));
        assertThat(lessons.get(0).getName(), equalTo("Tortilla"));
    }

    @Test
    public void updateLessonCompletionStatus() throws Exception {
        Lesson lesson = new Lesson("test_id", "Schnitzel", "Rotem",
                "Short", "Full", "img", "vid", "Medium", false, false);

        lessonDao.insert(lesson);
        lessonDao.updateCompletionStatus("test_id", true);

        List<Lesson> lessons = LiveDataTestUtil.getValue(lessonDao.getAllLessons());
        assertThat(lessons.get(0).isCompleted(), equalTo(true));
    }

    @Test
    public void deleteLessonById() throws Exception {
        Lesson lesson = new Lesson("test_id", "Pasta Rosa", "Rotem",
                "Short", "Full", "img", "vid", "Advanced", false, false);

        lessonDao.insert(lesson);
        lessonDao.delete(lesson);

        List<Lesson> lessons = LiveDataTestUtil.getValue(lessonDao.getAllLessons());
        assertThat(lessons.size(), equalTo(0));
    }

    @Test
    public void getLessonsByLevel() throws Exception {
        Lesson lesson1 = new Lesson("id1", "Beginner lesson", "Rotem",
                "Desc", "Full", "img", "vid", "Beginners", false, false);
        Lesson lesson2 = new Lesson("id2", "Advanced lesson", "Rotem",
                "Desc", "Full", "img", "vid", "Advanced", false, false);

        lessonDao.insert(lesson1);
        lessonDao.insert(lesson2);

        List<Lesson> beginners = LiveDataTestUtil.getValue(lessonDao.getLessonsByLevel("Beginners"));
        assertThat(beginners.size(), equalTo(1));
        assertThat(beginners.get(0).getLevel(), equalTo("Beginners"));
    }
}
