package com.piggylabs.nexscene.ui.screens.wishlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.nexscene.data.local.TitleStateLocalDataSource
import com.piggylabs.nexscene.data.local.TitleUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WishListUiState(
    val watchlistItems: List<TitleUserState> = emptyList(),
    val watchedItems: List<TitleUserState> = emptyList()
)

class WishListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WishListUiState())
    val uiState: StateFlow<WishListUiState> = _uiState.asStateFlow()

    private var localDataSource: TitleStateLocalDataSource? = null

    fun initLocal(context: Context) {
        if (localDataSource != null) return
        val local = TitleStateLocalDataSource(context.applicationContext)
        localDataSource = local

        viewModelScope.launch {
            combine(
                local.observeWatchlistItems(),
                local.observeWatchedItems()
            ) { watchlist, watched ->
                WishListUiState(
                    watchlistItems = watchlist,
                    watchedItems = watched
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun removeFromWatchlist(itemId: Int, mediaType: String) {
        val local = localDataSource ?: return
        viewModelScope.launch {
            local.setWatchlist(itemId = itemId, mediaType = mediaType, inWatchlist = false)
        }
    }

    fun unmarkWatched(itemId: Int, mediaType: String) {
        val local = localDataSource ?: return
        viewModelScope.launch {
            local.setWatched(itemId = itemId, mediaType = mediaType, watched = false)
        }
    }
}
