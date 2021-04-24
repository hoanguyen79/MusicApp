package com.music.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.music.R;
import com.music.databinding.ActivityMainBinding;
import com.music.models.Song;
import com.music.ui.home.HomeFragmentDirections;
import com.music.ui.login.LoginActivity;
import com.music.ui.playsong.playback.MediaPlayBackService;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Nullable
    private ActivityMainBinding binding;

    @NonNull
    public ActivityMainBinding getBinding() {
        return Objects.requireNonNull(binding);
    }

    @Nullable
    private NavController navController;

    @Nullable
    private AppBarConfiguration appBarConfiguration;

    @Inject
    FirebaseAuth firebaseAuth;

    @Nullable
    private MediaBrowserCompat mediaBrowser;

    @Nullable
    private MediaControllerCompat mediaController;

    @Nullable
    private MediaPlayer mediaPlayer;

    @NonNull
    private final Handler handler = new Handler(Looper.myLooper());

    @NonNull
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                getBinding().prbSongTimePlayed.setProgress(mediaPlayer.getCurrentPosition());
            }

            handler.postDelayed(this, 100);
        }
    };

    @NonNull
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionReady() {
            if (mediaController.getMetadata() != null) {
                handleHideShowBottomMediaPlayer();
                setupUI(mediaController.getMetadata());
                handler.postDelayed(runnable, 0);
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackStateCompat) {
            if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PAUSED) {
                handler.removeCallbacks(runnable);
            }

            if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                handler.postDelayed(runnable, 100);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metaData) {
            // Ẩn phần BottomMediaPlayer nếu đang ở trang nghe nhạc
            handleHideShowBottomMediaPlayer();

            // Cập nhật giao diện của BottomMediaPlayer
            setupUI(metaData);
        }
    };

    @NonNull
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlayer = ((MediaPlayBackService.LocalBinder) service).getMediaPlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaPlayer = null;
        }
    };

    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        if (firebaseAuth.getCurrentUser() == null) {
            // Ngừng phát nhạc và giải phóng bộ nhớ
            handler.removeCallbacks(runnable);

            if (mediaController != null) {
                mediaController.getTransportControls().stop();
                mediaController.unregisterCallback(controllerCallback);
            }

            if (mediaBrowser != null) {
                mediaBrowser.disconnect();
                mediaPlayer.release();
            }

            // Chuyển về trang đăng nhập
            Intent intent = new Intent(this, LoginActivity.class).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        initMediaPlayer();
        initStatusBar();
        initToolBar();
        initNavigationUI();

        getBinding().mediaPlayer.setOnClickListener(view -> {
            if (mediaController == null || navController == null) {
                return;
            }

            final MediaDescriptionCompat mediaDescription = mediaController.getMetadata().getDescription();

            final Song song = Song.Builder()
                    .setId(Objects.requireNonNull(mediaDescription.getMediaId()))
                    .setThumbnail(mediaDescription.getIconUri())
                    .build();

            final HomeFragmentDirections.ActionNavigationToPlaySongFragment action =
                    HomeFragmentDirections.actionNavigationToPlaySongFragment(song, new Song[]{});
            navController.navigate(action);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authStateListener);

        if (mediaBrowser != null && !mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mediaController != null) {
            if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                handler.removeCallbacks(runnable);
            }

            if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                handler.postDelayed(runnable, 100);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null && appBarConfiguration != null) {
            return NavigationUI.navigateUp(navController, appBarConfiguration);
        }

        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStop() {
        super.onStop();

        firebaseAuth.removeAuthStateListener(authStateListener);

        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
        }

        handler.removeCallbacks(runnable);

        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;

        unbindService(serviceConnection);
    }

    private void initStatusBar() {
        // Chỉnh màu StatusBar cho phiên bản Android 21, 22
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black_800));
        }
    }

    private void initToolBar() {
        // Tùy chỉnh phông chữ tiêu đề của ToolBar
        getBinding().toolbar.setTitleTextAppearance(this, R.style.Theme_CustomTextAppearance_ExtraLarge);
    }

    private void initNavigationUI() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(getBinding().bottomNavigationView, navController);
    }

    private void initMediaPlayer() {
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MediaPlayBackService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        final MediaSessionCompat.Token token = Objects.requireNonNull(mediaBrowser).getSessionToken();
                        mediaController = new MediaControllerCompat(MainActivity.this, token);
                        MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                        mediaController.registerCallback(controllerCallback);
                    }
                },
                null
        );

        bindService(
                new Intent(this, MediaPlayBackService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    private void setupUI(@NonNull MediaMetadataCompat metaData) {
        final MediaDescriptionCompat mediaDescription = metaData.getDescription();

        getBinding().songTitle.setText(mediaDescription.getTitle());
        getBinding().songArtists.setText(mediaDescription.getSubtitle());
        getBinding().prbSongTimePlayed.setMax((int) metaData.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));

        Glide.with(MainActivity.this)
                .load(mediaDescription.getIconUri())
                .circleCrop()
                .into(getBinding().songThumbnail);
    }

    private void handleHideShowBottomMediaPlayer() {
        if (navController == null) {
            return;
        }

        final NavDestination currentDestination = navController.getCurrentDestination();

        if (currentDestination != null && currentDestination.getId() == R.id.navigation_play_song_fragment) {
            getBinding().mediaPlayer.setVisibility(View.GONE);
        } else {
            getBinding().mediaPlayer.setVisibility(View.VISIBLE);
        }
    }
}