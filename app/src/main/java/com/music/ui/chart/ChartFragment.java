package com.music.ui.chart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.music.databinding.FragmentChartBinding;
import com.music.models.Song;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChartFragment extends Fragment {
    @Nullable
    private FragmentChartBinding binding;

    @SuppressWarnings({"NotNullFieldNotInitialized", "FieldCanBeLocal"})
    @NonNull
    private ChartViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChartBinding.inflate(inflater, container, false);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);

        binding.rvSongChartVertical.setHasFixedSize(true);
        binding.rvSongChartVertical.setLayoutManager(linearLayoutManager);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ChartViewModel.class);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(20)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Song> options = new FirestorePagingOptions.Builder<Song>()
                .setLifecycleOwner(getViewLifecycleOwner())
                .setQuery(viewModel.getQueryFetchTopSongs(), config, Song.class)
                .build();

        binding.rvSongChartVertical.setAdapter(new SongChartVerticalAdapter(options, binding.prbLoading));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}