package com.imn.iicnma.ui.search

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.imn.iicnma.data.repository.search.SearchRepository
import com.imn.iicnma.domain.model.Movie
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@OptIn(FlowPreview::class)
class SearchViewModel @ViewModelInject constructor(
    private val searchRepository: SearchRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Movie>>? = null

    var isSearchedAnyThing = false

    fun search(query: String): Flow<PagingData<Movie>>? {
        if (query.trim().isEmpty()) return null

        isSearchedAnyThing = true
        val lastResult = currentSearchResult
        if (query == currentQueryValue && lastResult != null) {
            return lastResult
        }

        savedStateHandle[KEY_QUERY_SAVED_STATE] = query

        val newResult = searchRepository.search(query).cachedIn(viewModelScope)

        currentSearchResult = newResult
        return newResult
    }

    fun getSavedQuery() = savedStateHandle.get(KEY_QUERY_SAVED_STATE) as? String

    companion object {
        const val KEY_QUERY_SAVED_STATE = "key_query_saved_state"
    }
}