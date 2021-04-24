package com.music.ui.home.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.databinding.SlideImageContainerBinding;
import com.music.models.Album;

import java.util.List;

public class AlbumSliderVerticalAdapter extends RecyclerView.Adapter<AlbumSliderVerticalAdapter.AlbumSliderVerticalViewHolder> {
    @NonNull
    private final List<Album> albums;

    public AlbumSliderVerticalAdapter(@NonNull List<Album> albums) {
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumSliderVerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumSliderVerticalViewHolder(
                SlideImageContainerBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumSliderVerticalViewHolder holder, int position) {
        holder.setData(albums.get(position));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumSliderVerticalViewHolder extends RecyclerView.ViewHolder {
        private final SlideImageContainerBinding binding;

        private AlbumSliderVerticalViewHolder(@NonNull SlideImageContainerBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        private void setData(@NonNull Album album) {
            binding.tvTitle.setText(album.getName());
            binding.tvDescription.setText(album.getDescription());
            Glide.with(itemView).load(album.getCover()).into(binding.ivCover);
        }
    }
}
