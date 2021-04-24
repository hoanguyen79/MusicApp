package com.music.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentId;
import com.music.utils.NumberUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "set", builderMethodName = "Builder")
public class Song implements Parcelable {
    /**
     * ID bài hát
     */
    @NonNull
    @DocumentId
    @Builder.Default
    private String id = StringUtils.EMPTY;

    /**
     * Tên bài hát
     */
    @NonNull
    @Builder.Default
    private String name = StringUtils.EMPTY;

    /**
     * Đường dẫn hình ảnh
     */
    @NonNull
    @Builder.Default
    private Uri thumbnail = Uri.EMPTY;

    /**
     * Lượt nghe của bài hát
     */
    private long listens;

    /**
     * Năm phát hành
     */
    private int year;

    /**
     * Lượt thích của bài hát
     */
    private long like;

    /**
     * Độ dài bài hát
     */
    private int duration;

    /**
     * Đường dẫn bài hát
     */
    @NonNull
    @Builder.Default
    private Uri mp3 = Uri.EMPTY;

    /**
     * Danh sách nghệ sĩ
     */
    @NonNull
    @Builder.Default
    private List<String> artists = Collections.emptyList();

    /**
     * Danh sách album mà bài hát này thuộc về
     */
    @NonNull
    @Builder.Default
    private List<String> albums = Collections.emptyList();

    protected Song(@NonNull Parcel in) {
        id = in.readString();
        name = in.readString();
        thumbnail = in.readParcelable(Uri.class.getClassLoader());
        listens = in.readLong();
        year = in.readInt();
        like = in.readLong();
        duration = in.readInt();
        mp3 = in.readParcelable(Uri.class.getClassLoader());
        artists = in.createStringArrayList();
        albums = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(thumbnail, flags);
        dest.writeLong(listens);
        dest.writeInt(year);
        dest.writeLong(like);
        dest.writeInt(duration);
        dest.writeParcelable(mp3, flags);
        dest.writeStringList(artists);
        dest.writeStringList(albums);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    /**
     * Viết hoa ký tự đầu của mỗi từ trong tên bài hát
     */
    public void setName(@NonNull String name) {
        this.name = WordUtils.capitalize(name);
    }

    /**
     * Nối tên các nghệ sĩ lại và cách nhau bởi dấu phẩy
     *
     * @return Danh sách tên nghệ sĩ
     */
    @NonNull
    public String getArtistsNames() {
        return TextUtils.join(", ", artists);
    }

    /**
     * Định dạng lại lượt thích, phân cách nhau bởi dấu chấm
     *
     * @return Lượt thích đã format
     */
    @NonNull
    public String getFormatLike() {
        return NumberUtils.formatWithSuffix(like);
    }

    /**
     * Định dạng lại lượt nghe, phân cách nhau bởi dấu chấm
     *
     * @return Lượt nghe đã format
     */
    @NonNull
    public String getFormatListens() {
        return NumberUtils.formatWithSuffix(listens);
    }

    /**
     * Trả về thời lượng bài hát ở dạng mili giây
     *
     * @return Thời lượng bài hát ở dạng mili giây
     */
    public int getDuration() {
        return duration * 1000;
    }

    public void setThumbnail(@NonNull String thumbnail) {
        this.thumbnail = Uri.parse(thumbnail);
    }

    public void setMp3(@NonNull String mp3) {
        this.mp3 = Uri.parse(mp3);
    }
}
