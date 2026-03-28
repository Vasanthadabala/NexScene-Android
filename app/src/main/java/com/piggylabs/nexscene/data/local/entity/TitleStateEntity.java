package com.piggylabs.nexscene.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;

@Entity(
        tableName = "title_state",
        primaryKeys = {"itemId", "mediaType"}
)
public class TitleStateEntity {
    public int itemId;
    @NonNull
    public String mediaType;
    @NonNull
    public String title;
    @Nullable
    public String posterUrl;
    public int userRating;
    public boolean inWatchlist;
    public boolean watched;
    public long updatedAt;

    public TitleStateEntity(
            int itemId,
            @NonNull String mediaType,
            @NonNull String title,
            @Nullable String posterUrl,
            int userRating,
            boolean inWatchlist,
            boolean watched,
            long updatedAt
    ) {
        this.itemId = itemId;
        this.mediaType = mediaType;
        this.title = title;
        this.posterUrl = posterUrl;
        this.userRating = userRating;
        this.inWatchlist = inWatchlist;
        this.watched = watched;
        this.updatedAt = updatedAt;
    }
}
