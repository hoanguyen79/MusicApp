package com.music.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.music.R;
import com.music.databinding.ActivityLoginBinding;
import com.music.ui.main.MainActivity;
import com.music.ui.register.RegisterActivity;
import com.music.ui.register.RegisterControlActivity;
import com.music.utils.ToolbarHelper;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private ActivityLoginBinding binding;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 123;

    private CallbackManager callbackManager;
    private static final String TAG = "FBAUTH";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarHelper.hideToolbar(this);

        firebaseAuth = FirebaseAuth.getInstance();
        DangNhap();
        createRequest();

        binding.tvCreateANewAccount.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        findViewById(R.id.google_sign).setOnClickListener(v -> signIn());
        //------------------------------------------------------------------
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(TAG, "onError: " + exception.getMessage());
            }
        });

        binding.facebookSign.setOnClickListener(v -> {
            LoginManager.getInstance()
                    .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        });

    }

    private void DangNhap() {
        binding.tvCreateANewAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterControlActivity.class));
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtFullname.getText().toString().trim();
            String password = binding.edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                binding.edtFullname.setError("không được bỏ trống");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                binding.edtPassword.setError("Bỏ trống là không được");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this,
                        "Địa chỉ Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            binding.layoutLoading.frmLoading.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Đăng nhập thành công",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(this, MainActivity.class).setFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK));

                        } else {
                            binding.edtFullname.setError("Tài khoản hoặc mật khẩu không đúng");
                        }
                        binding.layoutLoading.frmLoading.setVisibility(View.GONE);
                    });
        });
    }

    // Google
    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Đăng nhập bằng Google
    private void firebaseAuthWithGoogle(String idToken) {
        binding.layoutLoading.frmLoading.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                          Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                          Intent.FLAG_ACTIVITY_NEW_TASK);
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập thành công",
                                Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                    binding.layoutLoading.frmLoading.setVisibility(View.GONE);
                });
    }

    // đăng nhập Facebook
    private void handleFacebookAccessToken(AccessToken token) {
        binding.layoutLoading.frmLoading.setVisibility(View.VISIBLE);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                          Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                          Intent.FLAG_ACTIVITY_NEW_TASK);
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập thành công",
                                Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                    binding.layoutLoading.frmLoading.setVisibility(View.GONE);
                });
    }
}