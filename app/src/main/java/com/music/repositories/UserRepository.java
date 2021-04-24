package com.music.repositories;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.music.R;
import com.music.exceptions.NotLoginException;
import com.music.models.History;
import com.music.models.Song;
import com.music.network.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class UserRepository {
    @NonNull
    private final Context application;

    @NonNull
    private final FirebaseAuth firebaseAuth;

    @NonNull
    private final FirebaseFirestore db;

    @Inject
    public UserRepository(@NonNull @ApplicationContext Context application,
                          @NonNull FirebaseAuth firebaseAuth,
                          @NonNull FirebaseFirestore firebaseFirestore) {
        this.application = application;
        this.firebaseAuth = firebaseAuth;
        this.db = firebaseFirestore;
    }

    /**
     * Tạo tài khoản Firebase
     *
     * @param email    Địa chỉ email
     * @param password Mật khẩu
     */
    @NonNull
    public LiveData<Resource<FirebaseUser>> createUserWithEmailAndPassword(@NonNull String email,
                                                                           @NonNull String password) {
        final MutableLiveData<Resource<FirebaseUser>> resultMutableLiveData =
                new MutableLiveData<>(Resource.loading("Đang đăng ký"));

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    resultMutableLiveData.setValue(Resource.success(authResult.getUser()));
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        String exceptionMessage = application.getString(R.string.the_email_address_is_already_in_use_by_another_account);
                        resultMutableLiveData.setValue(Resource.error(exceptionMessage, null));
                    } else {
                        resultMutableLiveData.setValue(Resource.error(Objects.requireNonNull(e.getMessage()), null));
                    }
                });

        return resultMutableLiveData;
    }

    /**
     * Cập nhật thông tin tài khoản
     *
     * @param firebaseUser Tài khoản firebase
     * @param displayName  Tên hiển thị
     */
    @NonNull
    public LiveData<Resource<Void>> updateProfileUser(@NonNull FirebaseUser firebaseUser,
                                                      @NonNull String displayName) {
        final MutableLiveData<Resource<Void>> resultMutableLiveData =
                new MutableLiveData<>(Resource.loading("Đang cập nhật thông tin"));

        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        firebaseUser.updateProfile(profile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                resultMutableLiveData.setValue(Resource.success(null));
            } else {
                if (task.getException() != null && task.getException().getMessage() != null) {
                    resultMutableLiveData.setValue(Resource.error(task.getException().getMessage(), null));
                } else {
                    resultMutableLiveData.setValue(Resource.error("Đã có lỗi không rõ xảy ra", null));
                }
            }
        });

        return resultMutableLiveData;
    }

    /**
     * Cập nhật lịch sử nghe nhạc
     *
     * @param songId Mã bài hát
     */
    public void updateHistory(@NonNull String songId) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            throw new NotLoginException();
        }

        final Map<String, Object> history2 = new HashMap<>();
        history2.put("song_reference", db.collection("songs").document(songId));
        history2.put("listened_at", Timestamp.now().getSeconds());

        db.collection("users")
                .document(user.getUid())
                .collection("histories")
                .document(songId)
                .set(history2, SetOptions.merge());
    }

    /**
     * @see UserRepository#updateHistory(String)
     */
    public void updateHistory(@NonNull Song song) {
        updateHistory(song.getId());
    }

    public LiveData<Resource<List<Song>>> getHistories() {
        final MutableLiveData<Resource<List<Song>>> resultMutableLiveData =
                new MutableLiveData<>(Resource.loading("Đang lấy lịch sử nghe nhạc"));
        final List<Song> songs = new ArrayList<>();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            throw new NotLoginException();
        }

        db.collection("users")
                .document(user.getUid())
                .collection("histories")
                .orderBy("listened_at", Query.Direction.DESCENDING)
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        final List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        for (DocumentSnapshot document : documents) {
                            final AsyncTask<History, Integer, Song> execute = new RunInBackground().execute(document.toObject(History.class));
                            try {
                                final Song song = execute.get();
                                songs.add(song);
                                resultMutableLiveData.setValue(Resource.success(songs));
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        resultMutableLiveData.setValue(Resource.error("Khong co danh sach bai hat", null));
                    }
                });

        return resultMutableLiveData;
    }

    public static class RunInBackground extends AsyncTask<History, Integer, Song> {
        @Override
        protected Song doInBackground(History... history) {
            final Task<DocumentSnapshot> documentSnapshotTask = history[0].getSongReference().get();

            Song song = null;

            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                song = documentSnapshot.toObject(Song.class);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            return song;
        }
    }
}
