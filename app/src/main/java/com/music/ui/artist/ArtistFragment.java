package com.music.ui.artist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.music.R;
import com.music.databinding.FragmentArtistBinding;
import com.music.models.Artist;
import com.music.utils.ToolbarHelper;

import java.util.Objects;

public class ArtistFragment extends Fragment {
    @Nullable
    private FragmentArtistBinding binding;

    @NonNull
    public FragmentArtistBinding getBinding() {
        return Objects.requireNonNull(binding);
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ArtistFragmentArgs args;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ArtistViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentArtistBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ArtistViewModel.class);
        args = ArtistFragmentArgs.fromBundle(requireArguments());

        final Artist artist = args.getArtist();

        Glide.with(this).load(artist.getThumbnail()).into(binding.ivArtistCover);
        binding.tvArtistName.setText(artist.getName());

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        ToolbarHelper.hideToolbar(requireActivity());
        requireActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        ToolbarHelper.hideToolbar(requireActivity());
        requireActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}