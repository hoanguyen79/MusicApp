package com.music.ui.account;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.models.Song;
import com.music.network.Resource;
import com.music.repositories.UserRepository;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AccountViewModel extends ViewModel {
    @NonNull
    private final FirebaseUser currentUser;

    @NonNull
    private final MutableLiveData<Resource<List<Song>>> histories;

    @Inject
    public AccountViewModel(@NonNull FirebaseAuth firebaseAuth, @NonNull UserRepository userRepository) {
        currentUser = Objects.requireNonNull(firebaseAuth.getCurrentUser());
        histories = (MutableLiveData<Resource<List<Song>>>) userRepository.getHistories();
    }

    @NonNull
    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    @NonNull
    public LiveData<Resource<List<Song>>> getHistories() {
        return histories;
    }
}