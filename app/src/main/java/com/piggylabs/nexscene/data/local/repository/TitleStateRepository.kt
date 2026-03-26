package com.piggylabs.nexscene.data.local.repository

import android.content.Context
import com.piggylabs.nexscene.data.local.TitleUserState
import com.piggylabs.nexscene.data.local.db.AppDataBase
import com.piggylabs.nexscene.data.local.entity.TitleStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TitleStateRepository(context: Context) {

    private val dao = AppDataBase.getDatabase(context).titleStateDao()

    suspend fun getState(itemId: Int, mediaType: String): TitleUserState = withContext(Dispatchers.IO) {
        val normalizedType = mediaType.ifBlank { "movie" }
        dao.getState(itemId, normalizedType)?.toUserState() ?: TitleUserState(
            itemId = itemId,
            mediaType = normalizedType
        )
    }

    suspend fun upsert(state: TitleUserState) = withContext(Dispatchers.IO) {
        dao.upsert(
            TitleStateEntity(
                state.itemId,
                state.mediaType.ifBlank { "movie" },
                state.title,
                state.posterUrl,
                state.userRating.coerceIn(0, 10),
                state.inWatchlist,
                state.watched,
                System.currentTimeMillis()
            )
        )
    }

    fun observeWatchlistItems(): Flow<List<TitleUserState>> {
        return dao.observeWatchlistItems().map { list -> list.map { it.toUserState() } }
    }

    fun observeWatchedItems(): Flow<List<TitleUserState>> {
        return dao.observeWatchedItems().map { list -> list.map { it.toUserState() } }
    }

    suspend fun setWatchlist(itemId: Int, mediaType: String, inWatchlist: Boolean) = withContext(Dispatchers.IO) {
        dao.setWatchlist(
            itemId,
            mediaType.ifBlank { "movie" },
            inWatchlist,
            System.currentTimeMillis()
        )
    }

    suspend fun setWatched(itemId: Int, mediaType: String, watched: Boolean) = withContext(Dispatchers.IO) {
        dao.setWatched(
            itemId,
            mediaType.ifBlank { "movie" },
            watched,
            System.currentTimeMillis()
        )
    }

    private fun TitleStateEntity.toUserState(): TitleUserState {
        return TitleUserState(
            itemId = itemId,
            mediaType = mediaType,
            title = title,
            posterUrl = posterUrl,
            userRating = userRating.coerceIn(0, 10),
            inWatchlist = inWatchlist,
            watched = watched
        )
    }
}
