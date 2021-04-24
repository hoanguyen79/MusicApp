package com.music.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.music.models.Song;
import com.music.network.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SongRepository {
    private static final String TAG = "SongRepository";

    @NonNull
    private final FirebaseFirestore database;

    @Inject
    public SongRepository(@NonNull FirebaseFirestore database) {
        this.database = database;
    }

    /**
     * Lấy danh sách bảng xếp hạng bài hát có lượt nghe cao nhất
     *
     * @param limit Số bài hát tối thiểu cần lấy
     * @return Danh sách bài hát
     */
    @NonNull
    public LiveData<Resource<List<Song>>> getTopSongs(int limit) {
        final MutableLiveData<Resource<List<Song>>> resource = new MutableLiveData<>();

        Log.i(TAG, "getTopSongs: Đang tải bảng xếp hạng bài hát");
        resource.postValue(Resource.loading("Đang tải bảng xếp hạng bài hát"));

        database.collection("songs")
                .orderBy("listens", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot result = task.getResult();

                    if (!task.isSuccessful() || result == null || result.isEmpty()) {
                        Log.e(TAG, "getTopSongs: Tải bảng xếp hạng bài hát thất bại", task.getException());
                        resource.postValue(Resource.error("Không thể tải danh sách bảng xếp hạng", null));
                    } else {
                        Log.i(TAG, "getTopSongs: Tải bảng xếp hạng bài hát thành công");
                        resource.postValue(Resource.success(result.toObjects(Song.class)));
                    }
                });

        return resource;
    }

    /**
     * Trả về câu query để hỗ trợ cho Firebase phân trang
     *
     * @return Câu truy vấn đến collection songs và sắp xếp theo lượt nghe cao nhất
     */
    @NonNull
    public Query getQueryFetchTopSongs() {
        return database.collection("songs").orderBy("listens", Query.Direction.DESCENDING);
    }

    @NonNull
    public LiveData<Resource<List<Song>>> getNewReleased(int limit) {
        final MutableLiveData<Resource<List<Song>>> resource =
                new MutableLiveData<>(Resource.loading("Đang tải danh sách bài hát mới phát hành"));

        database.collection("songs")
                .orderBy("year", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot result = task.getResult();

                    if (!task.isSuccessful() || result == null || result.isEmpty()) {
                        Log.e(TAG, "getNewReleased: Tải danh sách bài hát mới phát hành thất bại", task.getException());
                        resource.postValue(Resource.error("Không thể tải danh sách bảng xếp hạng", null));
                    } else {
                        Log.i(TAG, "getTopSongs: Tải danh sách bài hát mới phát hành thành công");
                        resource.postValue(Resource.success(result.toObjects(Song.class)));
                    }
                });

        return resource;
    }

    @NonNull
    public LiveData<Resource<Song>> getInfoOfSong(@NonNull String songId) {
        final MutableLiveData<Resource<Song>> resource =
                new MutableLiveData<>(Resource.loading("Đang tải thông tin bài hát: " + songId));

        database.collection("songs").document(songId).get().addOnCompleteListener(task -> {
            DocumentSnapshot result = task.getResult();

            if (task.isSuccessful() && result != null) {
                Log.i(TAG, "getInfoOfSong: Tải thông tin bài hát " + songId + " thành công");
                resource.postValue(Resource.success(result.toObject(Song.class)));
            } else {
                Log.e(
                        TAG,
                        "getInfoOfSong: Tải thông tin bài hát " + songId + " thất bại",
                        task.getException()
                );
                resource.postValue(Resource.error("Tải thông tin bài hát thất bại", null));
            }
        });

        return resource;
    }

    public Task<QuerySnapshot> searchSongByName(@NonNull String name) {
        return database.collection("songs")
                .whereGreaterThanOrEqualTo("name", name)
                .whereLessThanOrEqualTo("name", name + "\uF7FF")
                .limit(8)
                .get();
    }
}
