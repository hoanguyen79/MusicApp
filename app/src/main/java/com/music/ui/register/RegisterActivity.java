package com.music.ui.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.music.databinding.ActivityRegisterBinding;
import com.music.network.Status;
import com.music.ui.main.MainActivity;
import com.music.utils.ToolbarHelper;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ActivityRegisterBinding binding;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        ToolbarHelper.hideToolbar(this);

        binding.btnRegister.setOnClickListener(this::onClickBtnRegister);
    }

    /**
     * Thực hiễn thao tác ẩn bàn phím khi nhấp ra ngoài EditText
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();

        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP ||
                             ev.getAction() == MotionEvent.ACTION_MOVE)) {
            if (view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
                view.clearFocus();

                int[] scroords = new int[2];
                view.getLocationOnScreen(scroords);

                float x = ev.getRawX() + view.getLeft() - scroords[0];
                float y = ev.getRawY() + view.getTop() - scroords[1];

                if (x < view.getLeft() || x > view.getRight() ||
                    y < view.getTop() || y > view.getBottom()) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean validateFullName() {
        final String fullName = binding.edtFullName.getText().toString().trim();

        if (fullName.isEmpty()) {
            binding.edtFullName.setError("Không được để trống");
            return false;
        }

        return true;
    }

    private boolean validateEmail() {
        final EditText edtEmail = binding.edtEmail;
        final String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Địa chỉ email không được để trống");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Địa chỉ email không hợp lệ");
            return false;
        }

        return true;
    }

    private boolean validatePassword() {
        final EditText edtPassword = binding.edtPassword;
        final EditText edtConfirmPassword = binding.edtConfirmPassword;
        final String password = edtPassword.getText().toString();
        final String confirmPassword = edtConfirmPassword.getText().toString();

        if (password.isEmpty()) {
            edtPassword.setError("Mật khẩu không được để trống");
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setError("Mật khẩu không được để trống");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu nhập lại không khớp");
            return false;
        }

        return true;
    }

    private void onClickBtnRegister(View view) {
        if (!validateFullName() || !validateEmail() || !validatePassword()) {
            return;
        }

        final String email = binding.edtEmail.getText().toString();
        final String password = binding.edtPassword.getText().toString();
        final String displayName = binding.edtFullName.getText().toString();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        viewModel.login(email, password).observe(this, responseCreateUser -> {
            switch (responseCreateUser.status) {
                case SUCCESS:
                    viewModel.updateProfile(responseCreateUser.data, displayName)
                            .observe(this, responseUpdateProfile -> {
                                if (responseUpdateProfile.status != Status.LOADING) {
                                    binding.layoutLoading.frmLoading.setVisibility(View.GONE);
                                    alert.setMessage(responseCreateUser.message);
                                    alert.show();

                                    if (responseUpdateProfile.status == Status.SUCCESS) {
                                        startActivity(new Intent(this, MainActivity.class).addFlags(
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK));
                                    }
                                }
                            });
                    break;
                case LOADING:
                    binding.layoutLoading.frmLoading.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    binding.layoutLoading.frmLoading.setVisibility(View.GONE);
                    alert.setTitle("Đã xảy ra lỗi!");
                    alert.setMessage(responseCreateUser.message);
                    alert.show();
                    break;
            }
        });
    }
}