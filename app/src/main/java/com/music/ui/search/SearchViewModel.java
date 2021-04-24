package com.music.ui.search;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.QuerySnapshot;
import com.music.models.Artist;
import com.music.models.Song;
import com.music.network.Resource;
import com.music.repositories.ArtistRepository;
import com.music.repositories.SongRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {
    @NonNull
    private final SongRepository songRepository;

    @NonNull
    private final ArtistRepository artistRepository;

    @NonNull
    private MutableLiveData<Resource<List<Song>>> songs = new MutableLiveData<>();

    @NonNull
    private MutableLiveData<Resource<List<Artist>>> artists = new MutableLiveData<>();

    @Inject
    public SearchViewModel(@NonNull SongRepository songRepository, @NonNull ArtistRepository artistRepository) {
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
    }

    public void searchSongByName(@NonNull String name) {
        songs.postValue(Resource.loading("Đang tìm kiếm bài hát có từ khóa: " + name));

        songRepository.searchSongByName(name).addOnCompleteListener(task -> {
            QuerySnapshot result = task.getResult();

            if (!task.isSuccessful() || result == null || result.isEmpty()) {
                songs.postValue(Resource.error("Tìm kiếm bài hát có từ khóa: " + name + " thất bại", null));
            } else {
                songs.postValue(Resource.success(result.toObjects(Song.class)));
            }
        });
    }

    public void searchArtistByName(@NonNull String name) {
        artists.postValue(Resource.loading("Đang tìm kiếm nghệ sĩ có từ khóa: " + name));

        artistRepository.searchByName(name).addOnCompleteListener(task -> {
            QuerySnapshot result = task.getResult();

            if (!task.isSuccessful() || result == null || result.isEmpty()) {
                artists.postValue(Resource.error("Tìm kiếm bài hát có từ khóa: " + name + " thất bại", null));
            } else {
                artists.postValue(Resource.success(result.toObjects(Artist.class)));
            }
        });
    }

    @NonNull
    public LiveData<Resource<List<Song>>> getSongs() {
        return songs;
    }

    @NonNull
    public MutableLiveData<Resource<List<Artist>>> getArtists() {
        return artists;
    }
}