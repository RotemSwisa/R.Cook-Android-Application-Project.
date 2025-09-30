package com.kinneret.rcook.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.kinneret.rcook.fragment.LessonsFragment;

/**
 * Pager adapter for displaying lesson fragments by level:
 * Beginners, Medium, and Advanced.
 */
public class LevelsPagerAdapter extends FragmentPagerAdapter {

    private final String[] levelTitles = {"Beginners", "Medium", "Advanced"};

    // Initializes the adapter using the provided fragment manager.
    public LevelsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    // Returns the correct LessonsFragment instance based on the selected tab position.
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return LessonsFragment.newInstance("Beginners");
            case 1:
                return LessonsFragment.newInstance("Medium");
            case 2:
                return LessonsFragment.newInstance("Advanced");
            default:
                return LessonsFragment.newInstance("Beginners");
        }
    }

    // Returns the number of lesson levels (3 tabs total).
    @Override
    public int getCount() {
        return levelTitles.length;
    }

    // Returns the title for each tab (Beginners, Medium, Advanced).
    @Override
    public CharSequence getPageTitle(int position) {
        return levelTitles[position];
    }
}