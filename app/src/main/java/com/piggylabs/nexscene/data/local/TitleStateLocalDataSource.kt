package com.piggylabs.nexscene.data.local

import android.content.Context
import com.piggylabs.nexscene.data.local.repository.TitleStateRepository
import kotlinx.coroutines.flow.Flow

class TitleStateLocalDataSource(context: Context) {

    private val repository = TitleStateRepository(context)

    suspend fun getState(itemId: Int, mediaType: String): TitleUserState {
        return repository.getState(itemId = itemId, mediaType = mediaType)
    }

    suspend fun upsert(state: TitleUserState) {
        repository.upsert(state)
    }

    fun observeWatchlistItems(): Flow<List<TitleUserState>> {
        return repository.observeWatchlistItems()
    }

    fun observeWatchedItems(): Flow<List<TitleUserState>> {
        return repository.observeWatchedItems()
    }

    suspend fun setWatchlist(itemId: Int, mediaType: String, inWatchlist: Boolean) {
        repository.setWatchlist(itemId = itemId, mediaType = mediaType, inWatchlist = inWatchlist)
    }

    suspend fun setWatched(itemId: Int, mediaType: String, watched: Boolean) {
        repository.setWatched(itemId = itemId, mediaType = mediaType, watched = watched)
    }
}
