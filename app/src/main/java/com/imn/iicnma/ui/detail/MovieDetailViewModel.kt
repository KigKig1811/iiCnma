package com.imn.iicnma.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.imn.iicnma.data.repository.favorites.FavoritesRepository
import com.imn.iicnma.data.repository.movies.MovieRepository
import com.imn.iicnma.domain.model.utils.withStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private var isFavored = false

    fun isFavoredStatus(movieId: Long) =
        favoritesRepository.isFavored(movieId)
            .onEach { isFavored = it }
            .asLiveData()

    fun loadMovie(id: Long) = withStates(200) { movieRepository.getMovie(id) }
        .asLiveData(viewModelScope.coroutineContext)


    fun toggleFavorite(movieId: Long) = viewModelScope.launch {
        if (isFavored) {
            favoritesRepository.removeFromFavorites(movieId)
        } else {
            favoritesRepository.addToFavorites(movieId)
        }
    }
}