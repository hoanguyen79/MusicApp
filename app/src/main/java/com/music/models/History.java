package com.music.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.PropertyName;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static lombok.Builder.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "Builder", setterPrefix = "set")
public class History {
    @NonNull
    @DocumentId
    @Default
    private String id = StringUtils.EMPTY;

    @NonNull
    private DocumentReference songReference;

    @NonNull
    private long listenedAt;

    @PropertyName("song_reference")
    public DocumentReference getSongReference() {
        return songReference;
    }

    @PropertyName("song_reference")
    public void setSongReference(DocumentReference songReference) {
        this.songReference = songReference;
    }

    @PropertyName("listened_at")
    public void setListenedAt(long listenedAt) {
        this.listenedAt = listenedAt;
    }

    @PropertyName("listened_at")
    public long getListenedAt() {
        return listenedAt;
    }
}
