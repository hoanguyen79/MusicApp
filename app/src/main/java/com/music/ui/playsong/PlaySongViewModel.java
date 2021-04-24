package com.music.ui.playsong;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.music.repositories.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PlaySongViewModel extends ViewModel {
    @NonNull
    private final UserRepository userRepository;

    @Inject
    public PlaySongViewModel(@NonNull UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateHistory(@NonNull String songId) {
        userRepository.updateHistory(songId);
    }
}
