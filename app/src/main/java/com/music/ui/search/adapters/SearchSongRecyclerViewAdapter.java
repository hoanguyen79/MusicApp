package com.music.ui.search.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.databinding.SearchSongItemLayoutBinding;
import com.music.models.Song;
import com.music.ui.search.SearchFragmentDirections;

import java.util.List;

public class SearchSongRecyclerViewAdapter extends RecyclerView.Adapter<SearchSongRecyclerViewAdapter.SongVerticalViewHolder> {
    @NonNull
    private final List<Song> songs;

    public SearchSongRecyclerViewAdapter(@NonNull List<Song> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongVerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongVerticalViewHolder(
                SearchSongItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SongVerticalViewHolder holder, int position) {
        holder.setSong(songs.get(position));
        holder.binding.songItem.setOnClickListener(v -> {
            final SearchFragmentDirections.ActionNavigationToPlaySongFragment action =
                    SearchFragmentDirections.actionNavigationToPlaySongFragment(songs.get(position), new Song[]{});
            Navigation.findNavController(v).navigate(action);
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongVerticalViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final SearchSongItemLayoutBinding binding;

        public SongVerticalViewHolder(@NonNull SearchSongItemLayoutBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void setSong(@NonNull Song song) {
            Glide.with(itemView).load(song.getThumbnail()).into(binding.thumbnail);
            binding.name.setText(song.getName());
            binding.artists.setText(song.getArtistsNames());
        }
    }
}
