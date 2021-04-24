package com.music.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public final class UiModeUtils {
    private static final String TAG = "UiModeUtils";

    private static int getCurrentUiMode(@NonNull Context context) {
        return context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    /**
     * @param context Context
     * @return Trả về true nếu là giao diện tối và ngược lại
     */
    public static boolean isDarkMode(@NonNull Context context) {
        return getCurrentUiMode(context) == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * @param context Context
     * @return Trả về true nếu là giao diện sáng và ngược lại
     */
    public static boolean isLightMode(@NonNull Context context) {
        return getCurrentUiMode(context) == Configuration.UI_MODE_NIGHT_NO;
    }

    /**
     * Bật giao diện tối
     */
    public static void enableNightMode() {
        Log.i(TAG, "enableNightMode: Đã chuyển sang giao diện tối");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    /**
     * Bật giao diện sáng
     */
    public static void disableNightMode() {
        Log.i(TAG, "enableNightMode: Đã chuyển sang giao diện sáng");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    /**
     * Bật giao diện sáng / tối theo hệ thống
     */
    public static void defaultNightMode() {
        Log.i(TAG, "enableNightMode: Đã chuyển sang giao diện mặc định của hệ thống");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
