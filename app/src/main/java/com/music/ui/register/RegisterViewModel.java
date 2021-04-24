package com.music.ui.register;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.music.network.Resource;
import com.music.repositories.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterViewModel extends ViewModel {
    @NonNull
    private final UserRepository userRepository;

    @Inject
    public RegisterViewModel(SavedStateHandle savedStateHandle, @NonNull UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<Resource<FirebaseUser>> login(@NonNull String email, @NonNull String password) {
        return userRepository.createUserWithEmailAndPassword(email, password);
    }

    public LiveData<Resource<Void>> updateProfile(@NonNull FirebaseUser user, @NonNull String displayName) {
        return userRepository.updateProfileUser(user, displayName);
    }
}
