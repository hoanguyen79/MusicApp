package com.music.ui.chart;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.music.databinding.ChartSongContainerBinding;
import com.music.models.Song;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.stream.Collectors;

public class SongChartVerticalAdapter extends FirestorePagingAdapter<Song, SongChartVerticalAdapter.SongChartVerticalViewHolder> {
    @NonNull
    private static final String TAG = "SongChartHorizontalAdap";

    @NonNull
    private final View viewLoading;

    public SongChartVerticalAdapter(@NonNull FirestorePagingOptions<Song> options, @NonNull View viewLoading) {
        super(options);

        this.viewLoading = viewLoading;
    }

    @NonNull
    @Override
    public SongChartVerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongChartVerticalViewHolder(
                ChartSongContainerBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    protected void onBindViewHolder(@NonNull SongChartVerticalViewHolder holder, int position, @NonNull Song model) {
        holder.bindData(model);

        holder.binding.songItem.setOnClickListener(view -> {
            final Song[] playList = Objects.requireNonNull(getCurrentList())
                    .snapshot()
                    .stream()
                    .map(song -> song.toObject(Song.class))
                    .collect(Collectors.toList())
                    .toArray((Song[]) Array.newInstance(Song.class, getItemCount()));

            ChartFragmentDirections.ActionNavigationToPlaySongFragment action =
                    ChartFragmentDirections.actionNavigationToPlaySongFragment(
                            model,
                            playList
                    );

            Navigation.findNavController(view).navigate(action);
        });
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {
        switch (state) {
            case LOADING_INITIAL:
                viewLoading.setVisibility(View.VISIBLE);
                Log.i(TAG, "onLoadingStateChanged: Khởi tạo dữ liệu");
                break;
            case LOADING_MORE:
                viewLoading.setVisibility(View.VISIBLE);
                Log.i(TAG, "onLoadingStateChanged: Đang tải thêm dữ liệu");
                break;
            case LOADED:
                viewLoading.setVisibility(View.GONE);
                Log.i(TAG, "onLoadingStateChanged: Đã tải: " + getItemCount() + " bài hát");
                break;
            case FINISHED:
                viewLoading.setVisibility(View.GONE);
                Log.i(TAG, "onLoadingStateChanged: Đã tải tất cả bài hát");
                break;
            case ERROR:
                viewLoading.setVisibility(View.GONE);
                Log.i(TAG, "onLoadingStateChanged: Đã xảy ra lỗi khi tải bài hát");
                break;
        }
    }

    static class SongChartVerticalViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final ChartSongContainerBinding binding;

        public SongChartVerticalViewHolder(@NonNull ChartSongContainerBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bindData(@NonNull Song song) {
            binding.name.setText(song.getName());
            binding.artists.setText(song.getArtistsNames());
            Glide.with(itemView).load(song.getThumbnail()).into(binding.thumbnail);
        }
    }
}
