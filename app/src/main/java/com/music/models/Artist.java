package com.music.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Artist implements Parcelable {
    /**
     * ID nghệ sĩ
     */
    @NonNull
    @DocumentId
    private String id = StringUtils.EMPTY;

    /**
     * Nghệ danh
     */
    @NonNull
    private String name = StringUtils.EMPTY;

    /**
     * Tên thật của nghệ sĩ
     */
    @NonNull
    private String realName = StringUtils.EMPTY;

    /**
     * Giới thiệu
     */
    @NonNull
    private String biography = StringUtils.EMPTY;

    /**
     * Ảnh cover
     */
    @NonNull
    private Uri cover = Uri.EMPTY;

    /**
     * Ảnh đại diện
     */
    @NonNull
    private Uri thumbnail = Uri.EMPTY;

    /**
     * Quốc gia của nghệ sĩ
     */
    @NonNull
    private String national = StringUtils.EMPTY;

    protected Artist(Parcel in) {
        id = in.readString();
        name = in.readString();
        realName = in.readString();
        biography = in.readString();
        cover = in.readParcelable(Uri.class.getClassLoader());
        thumbnail = in.readParcelable(Uri.class.getClassLoader());
        national = in.readString();
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @NonNull
    @PropertyName("real_name")
    public String getRealName() {
        return realName;
    }

    @PropertyName("real_name")
    public void setRealName(@NonNull String realName) {
        this.realName = realName;
    }

    public void setCover(@NonNull String cover) {
        this.cover = Uri.parse(cover);
    }

    public void setThumbnail(@NonNull String thumbnail) {
        this.thumbnail = Uri.parse(thumbnail);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(realName);
        dest.writeString(biography);
        dest.writeParcelable(cover, flags);
        dest.writeParcelable(thumbnail, flags);
        dest.writeString(national);
    }
}
