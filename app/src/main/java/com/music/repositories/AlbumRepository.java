package com.music.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.music.models.Album;
import com.music.models.Collection;
import com.music.network.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlbumRepository {
    private static final String TAG = "AlbumRepository";

    @NonNull
    private final FirebaseFirestore database;

    @Inject
    public AlbumRepository(@NonNull FirebaseFirestore database) {
        this.database = database;
    }

    @NonNull
    public LiveData<Resource<List<Album>>> getRecommendAlbums() {
        final MutableLiveData<Resource<List<Album>>> resource = new MutableLiveData<>();

        Log.i(TAG, "getRecommendAlbums: Đang tải danh sách 'Album Slider'");
        resource.postValue(Resource.loading("Đang tải danh sách 'Album Slider'"));

        database.collection("collections")
                .whereEqualTo("name", "Album Slider").limit(1).get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot result = task.getResult();

                    if (!task.isSuccessful() || result == null || result.isEmpty()) {
                        Log.e(TAG, "getRecommendAlbums: Tải danh sách 'Album Slider' thất bại", task.getException());
                        resource.postValue(Resource.error("Không thể tải danh sách Album Slider", null));
                    } else {
                        Log.i(TAG, "getRecommendAlbums: Tải danh sách 'Album Slider' thành công");
                        Collection collection = result.toObjects(Collection.class).get(0);
                        resource.postValue(Resource.success(collection.getAlbums()));
                    }
                });

        return resource;
    }
}
