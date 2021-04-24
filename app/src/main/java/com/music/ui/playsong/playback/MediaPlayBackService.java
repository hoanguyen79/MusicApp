package com.music.ui.playsong.playback;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.music.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import lombok.SneakyThrows;

public class MediaPlayBackService extends MediaBrowserServiceCompat {
    private static final String TAG = "MediaPlayBackService";

    public static final String CHANNEL_ID = "d6125a05-7632-409c-bf42-a17c18d9e97a";

    private final int musicPlayBackIdNotification = 1;

    @Nullable
    private MediaPlayer mediaPlayer;

    @Nullable
    private MediaSessionCompat mediaSession;

    @NonNull
    private final IBinder binder = new LocalBinder();

    @NonNull
    private final List<MediaDescriptionCompat> playList = new ArrayList<>();
    private int currentPosition = 0;

    @NonNull
    private final MediaSessionCompat.Callback callback = new Callback() {
        @Override
        public void onPlay() {
            Log.i(TAG, "onPlay: ");

            mediaSession.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            mediaPlayer.start();
            showPauseNotification();
        }

        @Override
        public void onPause() {
            Log.i(TAG, "onPause: ");
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                showPlayingNotification();
                stopForeground(false);
            }
        }

        @Override
        public void onStop() {
            Log.i(TAG, "onStop: onStop");
            mediaSession.setActive(false);
            stopForeground(true);
            stopSelf();
        }

        @Override
        public void onSeekTo(long pos) {
            mediaPlayer.seekTo((int) pos);

            if (mediaPlayer.isPlaying()) {
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            } else {
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            }
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            for (Iterator<MediaDescriptionCompat> iterator = playList.iterator(); iterator.hasNext(); ) {
                final MediaDescriptionCompat media = iterator.next();

                if (media.toString().equals(description.toString())) {
                    iterator.remove();
                }
            }

            playList.add(description);

            Log.i(TAG, "onAddQueueItem: " + playList);
        }

        @Override
        public void onSkipToPrevious() {
            Log.i(TAG, "onSkipToPrevious: Bài hát trước: " + currentPosition);

            currentPosition--;

            if (currentPosition < 0) {
                currentPosition = playList.size() - 1;
            }

            mediaSession.getController().getTransportControls().playFromUri(
                    playList.get(currentPosition).getMediaUri(),
                    null
            );
            setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                Log.i(TAG, "onSetRepeatMode: Lặp lại danh sách phát bài hát");
                mediaPlayer.setLooping(false);
            }

            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                Log.i(TAG, "onSetRepeatMode: Lặp lại bài hát");
                mediaPlayer.setLooping(true);
            }

            mediaSession.setRepeatMode(repeatMode);
        }

        @Override
        public void onSkipToNext() {
            if (currentPosition >= playList.size() - 1) {
                currentPosition = 0;
            } else {
                currentPosition++;
            }

            mediaSession.getController().getTransportControls().playFromUri(
                    playList.get(currentPosition).getMediaUri(),
                    null
            );
            setMediaPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
        }

        @SneakyThrows
        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            mediaPlayer.reset();

            try {
                mediaPlayer.setDataSource(uri.toString());
            } catch (Exception e) {
                mediaPlayer.release();
                initMediaPlayer();
                mediaPlayer.setDataSource(uri.toString());
            }

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaSession.getController().getTransportControls().play();
                initMediaSessionMetadata(playList.get(currentPosition));
            });
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            final int playListSize = playList.size();

            for (int i = 0; i < playListSize; i++) {
                MediaDescriptionCompat mediaDescriptionCompat = playList.get(i);

                if (Objects.requireNonNull(mediaDescriptionCompat.getMediaId()).equals(mediaId)) {
                    mediaSession.getController().getTransportControls().playFromUri(mediaDescriptionCompat.getMediaUri(), null);
                    currentPosition = i;
                    break;
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        initMediaPlayer();
        initMediaSession();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();

        if (mediaSession != null) {
            mediaSession.release();
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.setOnCompletionListener(mp -> {
            if (mediaSession != null &&
                mediaSession.getController()
                        .getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                mediaSession.getController().getTransportControls().skipToNext();
            }
        });
    }

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(getBaseContext(), TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                            PlaybackStateCompat.ACTION_PLAY_PAUSE |
                            PlaybackStateCompat.ACTION_STOP);
        mediaSession.setPlaybackState(stateBuilder.build());

        mediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);

        mediaSession.setCallback(callback);

        setSessionToken(mediaSession.getSessionToken());
    }

    private void initMediaSessionMetadata(@NonNull MediaDescriptionCompat media) {
        if (mediaSession == null || mediaPlayer == null) {
            return;
        }

        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, media.getMediaId());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, (String) media.getTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, (String) media.getSubtitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, (String) media.getDescription());
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, String.valueOf(media.getIconUri()));

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void showPauseNotification() {
        createNotification("Pause", R.drawable.ic_round_pause_48);
    }

    private void showPlayingNotification() {
        createNotification("Play", R.drawable.ic_round_play_arrow_48);
    }

    private void createNotification(@NonNull String titleOfBtnTogglePlayPause,
                                    @DrawableRes int iconBtnTogglePlayPause) {
        if (mediaSession == null) {
            return;
        }

        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID);

        builder.setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getBaseContext(), PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_round_music_note_24)
                .setShowWhen(false)
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_round_skip_previous_48, "Previous",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(getBaseContext(), PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                ))
                .addAction(new NotificationCompat.Action(
                        iconBtnTogglePlayPause, titleOfBtnTogglePlayPause,
                        MediaButtonReceiver.buildMediaButtonPendingIntent(getBaseContext(), PlaybackStateCompat.ACTION_PLAY_PAUSE))
                )
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_round_skip_next_48, "Next",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(getBaseContext(), PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                ))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getBaseContext(), PlaybackStateCompat.ACTION_STOP))
                )
                .setPriority(NotificationCompat.PRIORITY_LOW);

        Glide.with(getApplicationContext()).asBitmap().load(description.getIconUri()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                builder.setLargeIcon(resource);
                startForeground(musicPlayBackIdNotification, builder.build());
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    private void setMediaPlaybackState(int state) {
        if (mediaSession == null || mediaPlayer == null) return;

        PlaybackStateCompat.Builder playBackStateBuilder = new PlaybackStateCompat.Builder();

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playBackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                            PlaybackStateCompat.ACTION_PAUSE |
                                            PlaybackStateCompat.ACTION_SEEK_TO |
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
            playBackStateBuilder.setState(state, mediaPlayer.getCurrentPosition(), 1.0f);
        } else {
            playBackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                            PlaybackStateCompat.ACTION_PLAY |
                                            PlaybackStateCompat.ACTION_SEEK_TO |
                                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
            playBackStateBuilder.setState(state, mediaPlayer.getCurrentPosition(), 0f);
        }

        mediaSession.setPlaybackState(playBackStateBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return super.onBind(intent);
        }

        return binder;
    }

    public class LocalBinder extends Binder {
        @Nullable
        public MediaPlayer getMediaPlayer() {
            return mediaPlayer;
        }
    }
}
