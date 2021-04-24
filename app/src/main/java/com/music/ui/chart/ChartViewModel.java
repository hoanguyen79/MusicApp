package com.music.ui.chart;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;
import com.music.repositories.SongRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChartViewModel extends ViewModel {
    @NonNull
    private final SongRepository songRepository;

    @Inject
    public ChartViewModel(SavedStateHandle savedStateHandle, @NonNull SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @NonNull
    public Query getQueryFetchTopSongs() {
        return songRepository.getQueryFetchTopSongs();
    }
}