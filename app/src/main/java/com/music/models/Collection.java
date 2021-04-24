package com.music.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentId;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Collection {
    /**
     * ID bộ sưu tập
     */
    @NonNull
    @DocumentId
    private String id = StringUtils.EMPTY;

    /**
     * Tên bộ sưu tập
     */
    @NonNull
    private String name = StringUtils.EMPTY;

    /**
     * Danh sách albums của bộ sưu tập
     */
    @NonNull
    private List<Album> albums = new ArrayList<>();

    /**
     * Viết hoa tên bộ sưu tập khi gán tên
     *
     * @param name Tên bộ sưu tập
     */
    public void setName(@NonNull String name) {
        this.name = WordUtils.capitalize(name);
    }
}
