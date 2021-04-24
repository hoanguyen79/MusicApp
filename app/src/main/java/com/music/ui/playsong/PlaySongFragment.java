package com.music.ui.playsong;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.music.R;
import com.music.databinding.FragmentPlaySongBinding;
import com.music.models.Song;
import com.music.repositories.UserRepository;
import com.music.ui.playsong.playback.MediaPlayBackService;
import com.music.utils.UiModeUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PlaySongFragment extends Fragment {
    @Nullable
    private FragmentPlaySongBinding binding;

    @NonNull
    public FragmentPlaySongBinding getBinding() {
        return Objects.requireNonNull(binding);
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private PlaySongFragmentArgs args;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private PlaySongViewModel viewModel;

    @Nullable
    private MediaBrowserCompat mediaBrowser;

    @Nullable
    private MediaControllerCompat mediaController;

    @Nullable
    private MediaPlayer mediaPlayer;

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

    @NonNull
    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionReady() {
            final Song playNowSong = args.getPlayNowSong();

            // Xử lý nghe tiếp tục bài hát nếu bài hát muốn nghe trùng với bài hát đang nghe
            if (mediaController.getMetadata() != null) {
                final MediaMetadataCompat mediaMeta = mediaController.getMetadata();
                final String currentPlayingMediaId = mediaMeta.getDescription().getMediaId();

                if (playNowSong.getId().equals(currentPlayingMediaId)) {
                    // Cập nhật lại thời gian của bài hát trên Seekbar
                    updateSeekbar(mediaMeta);

                    // Nếu bài hát đang tạm dừng ở background và khi người dùng quay lại thì sẽ tiếp tục phát
                    if (!mediaPlayer.isPlaying()) {
                        mediaController.getTransportControls().play();
                    }

                    // Cập nhật lại icon của btnTogglePlayPause
                    handleUpdateImageSourceBtnTogglePlayPause(mediaController);
                    return;
                }
            }

            // Xử lý nghe một bài hát khác
            final List<Song> playList = Arrays.stream(
                    ArrayUtils.add(args.getPlayList(), playNowSong)
            ).distinct().collect(Collectors.toList());

            // Khởi động trình phát nhạc
            requireContext().startService(new Intent(requireContext(), MediaPlayBackService.class));

            // Thêm các bài hát vào hàng chờ
            for (Song song : playList) {
                mediaController.addQueueItem(new MediaDescriptionCompat.Builder()
                        .setMediaId(song.getId())
                        .setTitle(song.getName())
                        .setSubtitle(song.getArtistsNames())
                        .setDescription(song.getFormatListens() + " lượt nghe")
                        .setMediaUri(song.getMp3())
                        .setIconUri(song.getThumbnail())
                        .build()
                );
            }

            // Phát bài hát
            mediaController.getTransportControls().playFromMediaId(playNowSong.getId(), null);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackStateCompat) {
            if (binding != null) {
                handleUpdateImageSourceBtnTogglePlayPause(playbackStateCompat);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (binding != null) {
                updateUI(metadata.getDescription());
                updateSeekbar(metadata);
            }
        }
    };

    @NonNull
    private final Handler handler = new Handler(Looper.myLooper());

    @NonNull
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }

            handler.postDelayed(this, 100);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaySongBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(PlaySongViewModel.class);

        args = PlaySongFragmentArgs.fromBundle(requireArguments());

        mediaBrowser = new MediaBrowserCompat(requireActivity(),
                new ComponentName(requireActivity(), MediaPlayBackService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        if (mediaBrowser != null) {
                            mediaController = new MediaControllerCompat(requireContext(), mediaBrowser.getSessionToken());
                            MediaControllerCompat.setMediaController(requireActivity(), mediaController);
                            mediaController.registerCallback(controllerCallback);
                        }
                    }
                },
                null
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        requireContext().bindService(
                new Intent(requireContext(), MediaPlayBackService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        getBinding().btnTogglePlayPause.setOnClickListener(v -> {
            if (mediaController == null) {
                return;
            }

            int playBackState = mediaController.getPlaybackState().getState();

            if (playBackState == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.getTransportControls().pause();
            } else {
                mediaController.getTransportControls().play();
            }
        });

        getBinding().seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int msec, boolean fromUser) {
                getBinding().tvCurrentPosition.setText(DurationFormatUtils.formatDuration(msec, "mm:ss"));

                if (fromUser && mediaController != null) {
                    mediaController.getTransportControls().seekTo(msec);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        getBinding().btnSkipToNext.setOnClickListener(v -> {
            if (mediaController != null && mediaController.getTransportControls() != null) {
                mediaController.getTransportControls().skipToNext();
            }
        });

        getBinding().btnSkipToPrevious.setOnClickListener(v -> {
            if (mediaController != null && mediaController.getTransportControls() != null) {
                mediaController.getTransportControls().skipToPrevious();
            }
        });

        getBinding().btnRepeat.setOnClickListener(v -> {
            if (mediaController == null || mediaController.getTransportControls() == null) {
                return;
            }

            if (mediaController.getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ALL) {
                getBinding().btnRepeat.setImageResource(R.drawable.ic_round_repeat_one_24);
                mediaController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
            } else {
                getBinding().btnRepeat.setImageResource(R.drawable.ic_round_repeat_24);
                mediaController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
            }
        });

        updateUI(args.getPlayNowSong());
    }

    @Override
    public void onStart() {
        super.onStart();

        requireActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.media_player).setVisibility(View.GONE);

        if (mediaBrowser != null && !mediaBrowser.isConnected()) {
            mediaBrowser.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mediaController != null) {
            handleUpdateImageSourceBtnTogglePlayPause(mediaController);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        requireActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.media_player).setVisibility(View.VISIBLE);

        if (MediaControllerCompat.getMediaController(requireActivity()) != null) {
            MediaControllerCompat.getMediaController(requireActivity()).unregisterCallback(controllerCallback);
        }

        handler.removeCallbacks(runnable);

        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        requireContext().unbindService(serviceConnection);
    }

    private void handleUpdateImageSourceBtnTogglePlayPause(@NonNull MediaControllerCompat mediaController) {
        handleUpdateImageSourceBtnTogglePlayPause(mediaController.getPlaybackState());
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private void handleUpdateImageSourceBtnTogglePlayPause(@NonNull PlaybackStateCompat playbackStateCompat) {
        switch (playbackStateCompat.getState()) {
            case PlaybackStateCompat.STATE_BUFFERING:
                getBinding().prbBuffering.setVisibility(View.VISIBLE);
                break;
            case PlaybackStateCompat.STATE_CONNECTING:
                getBinding().prbBuffering.setVisibility(View.VISIBLE);
                break;
            case PlaybackStateCompat.STATE_ERROR:
                break;
            case PlaybackStateCompat.STATE_FAST_FORWARDING:
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                getBinding().prbBuffering.setVisibility(View.GONE);
                getBinding().btnTogglePlayPause.setVisibility(View.VISIBLE);
                getBinding().btnTogglePlayPause.setImageResource(R.drawable.ic_round_play_circle_64);

                // Xóa cập nhật thời gian đã nghe của bài hát khi dừng phát
                /*
                 19/01/2021: Khi pause sẽ vẫn tiếp tục theo dõi cập nhật thời gian bài hát, mục địch của
                 việc này là khi người dùng tua nhạc trên thanh thông báo thì ở bên player trong app vẫn sẽ
                 được cập nhật dù đang ở trạng thái pause
                 */
                // handler.removeCallbacks(runnable);
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                viewModel.updateHistory(mediaController.getMetadata().getDescription().getMediaId());
                getBinding().prbBuffering.setVisibility(View.GONE);
                getBinding().btnTogglePlayPause.setVisibility(View.VISIBLE);
                getBinding().btnTogglePlayPause.setImageResource(R.drawable.ic_round_pause_circle_64);

                // Thực hiện cập nhật thời gian đã nghe của bài hát khi đang phát
                handler.postDelayed(runnable, 0);
                break;
            case PlaybackStateCompat.STATE_REWINDING:
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                getBinding().btnTogglePlayPause.setVisibility(View.GONE);
                getBinding().prbBuffering.setVisibility(View.VISIBLE);
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                getBinding().btnTogglePlayPause.setVisibility(View.GONE);
                getBinding().prbBuffering.setVisibility(View.VISIBLE);
                break;
            case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                break;
        }
    }

    private void updateUI(@NonNull Song song) {
        getBinding().tvSongName.setText(song.getName());
        getBinding().tvSongArtists.setText(song.getArtistsNames());
        setBackgroundView(getBinding().frmLayout, song.getThumbnail());
        Glide.with(this)
                .load(song.getThumbnail())
                .circleCrop()
                .into(getBinding().ivThumbnail);
        getBinding().frmLoading.setVisibility(View.GONE);
    }

    private void updateUI(@NonNull MediaDescriptionCompat mediaDescriptionCompat) {
        getBinding().tvSongName.setText(mediaDescriptionCompat.getTitle());
        getBinding().tvSongArtists.setText(mediaDescriptionCompat.getSubtitle());
        Glide.with(this)
                .load(mediaDescriptionCompat.getIconUri())
                .circleCrop()
                .into(getBinding().ivThumbnail);
        setBackgroundView(getBinding().frmLayout, Objects.requireNonNull(mediaDescriptionCompat.getIconUri()));
    }

    private void updateSeekbar(@NonNull MediaMetadataCompat mediaMeta) {
        final long duration = mediaMeta.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

        getBinding().seekBar.setMax((int) duration);
        getBinding().tvLengthOfSong.setText(DurationFormatUtils.formatDuration(duration, "mm:ss"));
    }

    private void setBackgroundView(@NonNull View view, @NonNull Uri imageUrl) {
        binding = getBinding();

        Glide.with(this).asBitmap().override(100).load(imageUrl).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                Palette.from(bitmap).generate(palette -> {
                    if (palette == null) {
                        return;
                    }

                    final int defaultColor = getResources().getColor(R.color.blue_700);
                    final int grayColor = getResources().getColor(R.color.gray_300);
                    final int blackColor = getResources().getColor(R.color.black_800);

                    int[] colors = new int[]{
                            // Màu nằm bên dưới
                            palette.getDominantColor(defaultColor),
                            // Màu nằm bên trên, màu này bắt buộc phải khớp với màu của thanh ActionBar
                            UiModeUtils.isLightMode(requireContext()) ? Color.WHITE : blackColor
                    };

                    // Nếu màu chủ đạo quá tối thì sẽ lấy màu khác
                    if (ColorUtils.calculateLuminance(colors[0]) < 0.25) {
                        colors[0] = palette.getDarkVibrantColor(defaultColor);
                    }

                    // Hiển thị background gradient với màu đi từ dưới lên trên
                    view.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors));

                    // Nếu màu colors[0] quá tối thì sẽ chỉnh màu chữ thành trắng và ngược lại thành đen
                    if (ColorUtils.calculateLuminance(colors[0]) < 0.3) {
                        final ColorFilter colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                grayColor,
                                BlendModeCompat.SRC_IN
                        );

                        binding.tvSongName.setTextColor(grayColor);
                        binding.tvSongArtists.setTextColor(grayColor);
                        binding.tvCurrentPosition.setTextColor(grayColor);
                        binding.tvLengthOfSong.setTextColor(grayColor);
                        binding.btnTogglePlayPause.setColorFilter(grayColor);
                        binding.btnRepeat.setColorFilter(grayColor);
                        binding.btnSkipToNext.setColorFilter(grayColor);
                        binding.btnSkipToPrevious.setColorFilter(grayColor);
                        binding.seekBar.getProgressDrawable().setColorFilter(colorFilter);
                        binding.seekBar.getThumb().setColorFilter(colorFilter);
                        binding.btnHeart.setBackgroundResource(R.drawable.toggle_btn_favorite_light);
                    } else {
                        final ColorFilter colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                blackColor,
                                BlendModeCompat.SRC_IN
                        );

                        binding.tvSongName.setTextColor(blackColor);
                        binding.tvSongArtists.setTextColor(blackColor);
                        binding.tvCurrentPosition.setTextColor(blackColor);
                        binding.tvLengthOfSong.setTextColor(blackColor);
                        binding.btnTogglePlayPause.setColorFilter(blackColor);
                        binding.btnRepeat.setColorFilter(blackColor);
                        binding.btnSkipToNext.setColorFilter(blackColor);
                        binding.btnSkipToPrevious.setColorFilter(blackColor);
                        binding.seekBar.getProgressDrawable().setColorFilter(colorFilter);
                        binding.seekBar.getThumb().setColorFilter(colorFilter);
                        binding.btnHeart.setBackgroundResource(R.drawable.toggle_btn_favorite_dark);
                    }
                });
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) { }
        });
    }
}