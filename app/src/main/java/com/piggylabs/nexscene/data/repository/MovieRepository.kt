package com.piggylabs.nexscene.data.repository

import com.piggylabs.nexscene.data.api.MovieApi
import com.piggylabs.nexscene.data.api.MovieApiResponse
import com.piggylabs.nexscene.data.api.SimilarApiResponse
import com.piggylabs.nexscene.data.api.TrailerApiResponse
import com.piggylabs.nexscene.data.api.TvApiResponse
import com.piggylabs.nexscene.data.api.CastApiResponse

class MovieRepository {
    suspend fun getPopularMovies(): MovieApiResponse = MovieApi.getPopularMovies()

    suspend fun searchMovies(query: String): MovieApiResponse = MovieApi.searchMovies(query)

    suspend fun searchTvShows(query: String): TvApiResponse = MovieApi.searchTvShows(query)

    suspend fun getPopularTvShows(): TvApiResponse = MovieApi.getPopularTvShows()

    suspend fun getMovieTrailer(movieId: Int): TrailerApiResponse = MovieApi.getMovieTrailer(movieId)

    suspend fun getLeadingCast(itemId: Int, mediaType: String): CastApiResponse =
        MovieApi.getLeadingCast(itemId = itemId, mediaType = mediaType)

    suspend fun getSimilarTitles(itemId: Int, mediaType: String): SimilarApiResponse =
        MovieApi.getSimilarTitles(itemId = itemId, mediaType = mediaType)

    fun posterUrl(path: String?): String? = MovieApi.posterUrl(path)
}
