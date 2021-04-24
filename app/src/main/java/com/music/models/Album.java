package com.music.models;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentId;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Album {
    /**
     * ID album
     */
    @NonNull
    @DocumentId
    private String id = StringUtils.EMPTY;

    /**
     * Tên album
     */
    @NonNull
    private String name = StringUtils.EMPTY;

    /**
     * Mô tả ngắn gọn của album
     */
    @NonNull
    private String description = StringUtils.EMPTY;

    /**
     * Đường dẫn hình ảnh album
     */
    @NonNull
    private String cover = StringUtils.EMPTY;

    /**
     * Viết hoa ký tự đầu của mỗi từ trong tên album
     */
    public void setName(@NonNull String name) {
        this.name = WordUtils.capitalize(name);
    }
}
