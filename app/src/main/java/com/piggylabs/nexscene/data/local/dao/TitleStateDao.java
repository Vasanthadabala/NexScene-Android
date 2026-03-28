package com.piggylabs.nexscene.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.piggylabs.nexscene.data.local.entity.TitleStateEntity;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface TitleStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(TitleStateEntity state);

    @Query("SELECT * FROM title_state WHERE itemId = :itemId AND mediaType = :mediaType LIMIT 1")
    TitleStateEntity getState(int itemId, String mediaType);

    @Query("SELECT * FROM title_state WHERE inWatchlist = 1 ORDER BY updatedAt DESC")
    Flow<List<TitleStateEntity>> observeWatchlistItems();

    @Query("SELECT * FROM title_state WHERE watched = 1 ORDER BY updatedAt DESC")
    Flow<List<TitleStateEntity>> observeWatchedItems();

    @Query("SELECT * FROM title_state")
    List<TitleStateEntity> getAllStates();

    @Query("UPDATE title_state SET inWatchlist = :inWatchlist, updatedAt = :updatedAt WHERE itemId = :itemId AND mediaType = :mediaType")
    void setWatchlist(int itemId, String mediaType, boolean inWatchlist, long updatedAt);

    @Query("UPDATE title_state SET watched = :watched, updatedAt = :updatedAt WHERE itemId = :itemId AND mediaType = :mediaType")
    void setWatched(int itemId, String mediaType, boolean watched, long updatedAt);

    @Query("DELETE FROM title_state")
    void clearAllStates();
}
