package com.music.ui.search.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.databinding.SearchArtistItemLayoutBinding;
import com.music.models.Artist;
import com.music.ui.search.SearchFragmentDirections;

import java.util.List;

public class SearchArtistRecyclerViewAdapter extends RecyclerView.Adapter<SearchArtistRecyclerViewAdapter.ArtistVerticalViewHolder> {
    @NonNull
    private final List<Artist> artists;

    public SearchArtistRecyclerViewAdapter(@NonNull List<Artist> artists) {
        this.artists = artists;
    }

    @NonNull
    @Override
    public ArtistVerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistVerticalViewHolder(
                SearchArtistItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistVerticalViewHolder holder, int position) {
        holder.setArtist(artists.get(position));

        holder.itemView.setOnClickListener(v -> {
            final SearchFragmentDirections.ActionNavigationToArtistFragment action =
                    SearchFragmentDirections.actionNavigationToArtistFragment(artists.get(position));
            Navigation.findNavController(holder.itemView).navigate(action);
        });
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    static class ArtistVerticalViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final SearchArtistItemLayoutBinding binding;

        public ArtistVerticalViewHolder(@NonNull SearchArtistItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setArtist(@NonNull Artist artist) {
            Glide.with(itemView).load(artist.getThumbnail()).into(binding.thumbnail);
            binding.name.setText(artist.getName());
        }
    }
}
