package com.music.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

public final class ToolbarHelper {
    /**
     * Hiển thị Toolbar của Activity
     *
     * @param activity Activity cần hiển thị Toolbar
     */
    public static void showToolbar(@NonNull AppCompatActivity activity) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().show();
        }
    }

    /**
     * @see ToolbarHelper#showToolbar(AppCompatActivity)
     */
    public static void showToolbar(@NonNull FragmentActivity fragmentActivity) {
        showToolbar((AppCompatActivity) fragmentActivity);
    }

    /**
     * Ẩn Toolbar của Activity
     *
     * @param activity Activity cần ẩn Toolbar
     */
    public static void hideToolbar(@NonNull AppCompatActivity activity) {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().hide();
        }
    }

    /**
     * @see ToolbarHelper#hideToolbar(AppCompatActivity)
     */
    public static void hideToolbar(@NonNull FragmentActivity fragmentActivity) {
        hideToolbar((AppCompatActivity) fragmentActivity);
    }
}
