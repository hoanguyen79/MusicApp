package com.music.ui.home.adapters.song;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.databinding.CardImageContainerBinding;
import com.music.models.Song;
import com.music.ui.home.HomeFragmentDirections;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SongChartVerticalAdapter extends RecyclerView.Adapter<SongChartVerticalAdapter.SongChartVerticalViewHolder> {
    @NonNull
    private final List<Song> songs;

    public SongChartVerticalAdapter(@NonNull List<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongChartVerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongChartVerticalViewHolder(
                CardImageContainerBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SongChartVerticalViewHolder holder, int position) {
        final Song song = songs.get(position);

        holder.setSong(song);

        holder.binding.songContainer.setOnClickListener(view -> {
            HomeFragmentDirections.ActionNavigationToPlaySongFragment action =
                    HomeFragmentDirections.actionNavigationToPlaySongFragment(song, new Song[]{});
            Navigation.findNavController(view).navigate(action);
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongChartVerticalViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final CardImageContainerBinding binding;

        public SongChartVerticalViewHolder(@NonNull CardImageContainerBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void setSong(@NonNull Song song) {
            binding.tvTitle.setText(song.getName());
            binding.tvDescription.setText(StringUtils.join(song.getArtists(), ", "));

            Glide.with(itemView).load(song.getThumbnail()).into(binding.ivImage);
        }
    }
}
