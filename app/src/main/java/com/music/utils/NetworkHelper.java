package com.music.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class NetworkHelper {
    private final Context context;

    @Inject
    public NetworkHelper(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Có kết nối internet
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Không có kết nối internet
     */
    public boolean isNetworkNotAvailable() {
        return !isNetworkAvailable();
    }
}
