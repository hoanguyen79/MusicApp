package com.music.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.music.R;
import com.music.databinding.FragmentSearchBinding;
import com.music.ui.search.adapters.SearchArtistRecyclerViewAdapter;
import com.music.ui.search.adapters.SearchSongRecyclerViewAdapter;
import com.music.utils.ToolbarHelper;

import java.util.Collections;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends Fragment implements TextWatcher {
    @Nullable
    private FragmentSearchBinding binding;

    @NonNull
    public FragmentSearchBinding getBinding() {
        return Objects.requireNonNull(binding);
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private SearchViewModel viewModel;

    @NonNull
    private final Handler handler = new Handler(Looper.myLooper());

    @Nullable
    private Runnable debounceRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        setupSearchSongRecyclerView();
        setupSearchArtistRecyclerView();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(requireView()).popBackStack());

        binding.edtSearch.requestFocus();
        binding.edtSearch.addTextChangedListener(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        viewModel.getSongs().observe(getViewLifecycleOwner(), response -> {
            switch (response.status) {
                case SUCCESS:
                    getBinding().frmLoading.setVisibility(View.GONE);
                    getBinding().layoutResultFindSong.setVisibility(View.VISIBLE);
                    getBinding().rvSongs.setAdapter(new SearchSongRecyclerViewAdapter(Objects.requireNonNull(response.data)));
                    break;
                case LOADING:
                    getBinding().layoutResultFindSong.setVisibility(View.GONE);
                    getBinding().frmLoading.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    Toast.makeText(requireActivity(), response.message, Toast.LENGTH_SHORT).show();
                    getBinding().frmLoading.setVisibility(View.GONE);
                    break;
            }
        });

        viewModel.getArtists().observe(getViewLifecycleOwner(), response -> {
            switch (response.status) {
                case SUCCESS:
                    getBinding().frmLoading.setVisibility(View.GONE);
                    getBinding().layoutResultFindArtist.setVisibility(View.VISIBLE);
                    getBinding().rvArtists.setAdapter(new SearchArtistRecyclerViewAdapter(Objects.requireNonNull(response.data)));
                    break;
                case LOADING:
                    getBinding().layoutResultFindSong.setVisibility(View.GONE);
                    getBinding().frmLoading.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    Toast.makeText(requireActivity(), response.message, Toast.LENGTH_SHORT).show();
                    getBinding().frmLoading.setVisibility(View.GONE);
                    break;
            }
        });
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

        ToolbarHelper.showToolbar(requireActivity());
        requireActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }

    private void setupSearchSongRecyclerView() {
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        getBinding().rvSongs.setHasFixedSize(true);
        getBinding().rvSongs.setLayoutManager(linearLayoutManager);
        getBinding().rvSongs.setAdapter(new SearchArtistRecyclerViewAdapter(Collections.emptyList()));
    }

    private void setupSearchArtistRecyclerView() {
        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);

        getBinding().rvArtists.setHasFixedSize(true);
        getBinding().rvArtists.setLayoutManager(linearLayoutManager);
        getBinding().rvArtists.setAdapter(new SearchSongRecyclerViewAdapter(Collections.emptyList()));
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (debounceRunnable != null) {
            handler.removeCallbacks(debounceRunnable);
        }

        if (s.toString().trim().isEmpty()) {
            return;
        }

        debounceRunnable = () -> {
            viewModel.searchSongByName(s.toString());
            viewModel.searchArtistByName(s.toString());
        };

        handler.postDelayed(debounceRunnable, 500);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}