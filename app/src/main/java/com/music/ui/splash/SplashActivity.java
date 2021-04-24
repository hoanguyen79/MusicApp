package com.music.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.music.Constant;
import com.music.ui.login.LoginActivity;
import com.music.ui.main.MainActivity;
import com.music.utils.NetworkHelper;
import com.music.utils.UiModeUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {
    @Inject
    FirebaseAuth firebaseAuth;

    @Inject
    NetworkHelper networkHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleChangeUiMode();

        // Kiểm tra kết nối Internet
        if (networkHelper.isNetworkNotAvailable()) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Để sử dụng ứng dụng bạn cần kết nối internet");
            alertBuilder.setOnDismissListener(dialog -> finishAndRemoveTask());
            alertBuilder.show();
            return;
        }

        /*
            Kiểm tra đã đăng nhập hay chưa, nếu chưa đăng nhập sẽ dẫn đến trang đăng nhập
            Nếu đăng nhập rồi thì sẽ dẫn đến trang chủ
         */
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }

    private void handleChangeUiMode() {
        final SharedPreferences settingsSharedPreference = getSharedPreferences(
                Constant.SETTING_SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE
        );

        final int theme = settingsSharedPreference.getInt(
                Constant.SETTING_SHARED_PREFERENCE_THEME,
                Configuration.UI_MODE_NIGHT_UNDEFINED
        );

        switch (theme) {
            case Configuration.UI_MODE_NIGHT_YES:
                UiModeUtils.enableNightMode();
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                UiModeUtils.disableNightMode();
                break;
            default:
                UiModeUtils.defaultNightMode();
                break;
        }
    }
}