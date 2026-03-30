package com.piggylabs.nexscene.data.repository

import com.piggylabs.nexscene.data.api.MovieApi
import com.piggylabs.nexscene.data.api.MovieApiResponse
import com.piggylabs.nexscene.data.api.ProvidersApiResponse
import com.piggylabs.nexscene.data.api.SimilarApiResponse
import com.piggylabs.nexscene.data.api.TrailerApiResponse
import com.piggylabs.nexscene.data.api.TvApiResponse
import com.piggylabs.nexscene.data.api.CastApiResponse
import com.piggylabs.nexscene.data.api.TitleDetailsApiResponse

class MovieRepository {
    suspend fun getPopularMovies(): MovieApiResponse = MovieApi.getPopularMovies()

    suspend fun searchMovies(query: String): MovieApiResponse = MovieApi.searchMovies(query)

    suspend fun searchTvShows(query: String): TvApiResponse = MovieApi.searchTvShows(query)

    suspend fun getPopularTvShows(): TvApiResponse = MovieApi.getPopularTvShows()

    suspend fun getTopRatedMovies(): MovieApiResponse = MovieApi.getTopRatedMovies()

    suspend fun getTopRatedTvShows(): TvApiResponse = MovieApi.getTopRatedTvShows()

    suspend fun discoverMoviesByGenre(genreId: Int): MovieApiResponse =
        MovieApi.discoverMoviesByGenre(genreId)

    suspend fun discoverTvByGenre(genreId: Int): TvApiResponse =
        MovieApi.discoverTvByGenre(genreId)

    suspend fun getMovieTrailer(movieId: Int): TrailerApiResponse = MovieApi.getMovieTrailer(movieId)

    suspend fun getTrailer(itemId: Int, mediaType: String): TrailerApiResponse =
        MovieApi.getTrailer(itemId = itemId, mediaType = mediaType)

    suspend fun getLeadingCast(itemId: Int, mediaType: String): CastApiResponse =
        MovieApi.getLeadingCast(itemId = itemId, mediaType = mediaType)

    suspend fun getSimilarTitles(itemId: Int, mediaType: String): SimilarApiResponse =
        MovieApi.getSimilarTitles(itemId = itemId, mediaType = mediaType)

    suspend fun getTitleDetails(itemId: Int, mediaType: String): TitleDetailsApiResponse =
        MovieApi.getTitleDetails(itemId = itemId, mediaType = mediaType)

    suspend fun getWatchProviders(
        itemId: Int,
        mediaType: String,
        countryCode: String
    ): ProvidersApiResponse = MovieApi.getWatchProviders(
        itemId = itemId,
        mediaType = mediaType,
        countryCode = countryCode
    )

    fun posterUrl(path: String?): String? = MovieApi.posterUrl(path)
}
