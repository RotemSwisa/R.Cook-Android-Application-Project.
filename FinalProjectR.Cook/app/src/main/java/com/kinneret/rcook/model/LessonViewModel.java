package com.kinneret.rcook.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LessonViewModel extends AndroidViewModel {

    private LessonDao mLessonDao;
    private UserDao mUserDao;
    private LiveData<List<Lesson>> mAllLessons;
    private LiveData<User> mCurrentUser;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public LessonViewModel(Application application) {
        super(application);
        RCookRoomDatabase db = RCookRoomDatabase.getDatabase(application);
        mLessonDao = db.lessonDao();
        mUserDao = db.userDao();
        mAllLessons = mLessonDao.getAllLessons();
        mCurrentUser = mUserDao.getCurrentUser();
    }
    // Lesson methods
    public LiveData<List<Lesson>> getAllLessons() {
        return mAllLessons;
    }

    public LiveData<List<Lesson>> getLessonsByLevel(String level) {
        return mLessonDao.getLessonsByLevel(level);
    }

    public LiveData<List<Lesson>> getFavoriteLessons() {
        return mLessonDao.getFavoriteLessons();
    }

    public LiveData<List<Lesson>> getCompletedLessons() {
        return mLessonDao.getCompletedLessons();
    }

    public void insert(Lesson lesson) {
        executor.execute(() -> mLessonDao.insert(lesson));
    }

    public void update(Lesson lesson) {
        executor.execute(() -> mLessonDao.update(lesson));
    }

    public void delete(Lesson lesson) {
        executor.execute(() -> mLessonDao.delete(lesson));
    }

    public void updateFavoriteStatus(String lessonId, boolean isFavorite) {
        executor.execute(() -> mLessonDao.updateFavoriteStatus(lessonId, isFavorite));
    }

    public void updateCompletionStatus(String lessonId, boolean isCompleted) {
        executor.execute(() -> mLessonDao.updateCompletionStatus(lessonId, isCompleted));
    }

    // User methods
    public LiveData<User> getCurrentUser() {
        return mCurrentUser;
    }

    public void saveUser(User user) {
        executor.execute(() -> mUserDao.insertOrUpdate(user));
    }

    public void updateUser(User user) {
        executor.execute(() -> mUserDao.update(user));
    }

}