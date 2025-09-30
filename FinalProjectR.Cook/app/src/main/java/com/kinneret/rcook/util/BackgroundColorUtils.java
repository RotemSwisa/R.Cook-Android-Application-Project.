package com.kinneret.rcook.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import com.kinneret.rcook.R;

/**
 * Utility class for managing background colors across all activities
 */
public class BackgroundColorUtils {

    private static final String DEFAULT_COLOR = "default";

    /**
     * Apply background color to activity based on user preference
     * @param activity The activity to apply color to
     * @param color The color preference ("gray", "blue", "yellowish", "default")
     */
    public static void applyBackgroundColor(Activity activity, String color) {
        if (activity == null) return;

        Log.d("BackgroundColorUtils", "Applying color: " + color + " to " + activity.getClass().getSimpleName());

        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            applyColorToView(rootView, color, activity);
        }
    }

    /**
     * Apply background color to specific view
     * @param view The view to apply color to
     * @param color The color preference
     * @param activity The activity context for getting resources
     */
    public static void applyColorToView(View view, String color, Activity activity) {
        if (view == null || activity == null || color == null) {
            return;
        }

        switch (color) {
            case "gray":
                view.setBackgroundColor(activity.getResources().getColor(R.color.colorGray));
                break;
            case "blue":
                view.setBackgroundColor(activity.getResources().getColor(R.color.backgroundBlue));
                break;
            case "yellowish":
                view.setBackgroundColor(activity.getResources().getColor(R.color.backgroundYellowish));
                break;
            case "default":
            default:
                view.setBackgroundColor(activity.getResources().getColor(R.color.backgroundDefault));
                break;
        }
    }

    /**
     * Get color resource ID based on color name
     * @param color The color name
     * @return The color resource ID
     */
    public static int getColorResourceId(String color) {
        switch (color) {
            case "gray":
                return R.color.colorGray;
            case "blue":
                return R.color.backgroundBlue;
            case "yellowish":
                return R.color.backgroundYellowish;
            case "default":
            default:
                return R.color.backgroundDefault;
        }
    }

}