package com.imn.iicnma.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imn.iicnma.R
import com.imn.iicnma.databinding.FragmentHomeBinding
import com.imn.iicnma.domain.model.Movie
import com.imn.iicnma.ui.common.base.BaseFragment
import com.imn.iicnma.ui.common.loadstate.ListLoadStateAdapter
import com.imn.iicnma.utils.ViewLifecycleDelegate
import com.imn.iicnma.utils.isPortrait
import com.imn.iicnma.utils.listenOnLoadStates
import com.imn.iicnma.utils.navigateSafe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val homeViewModel: HomeViewModel by viewModels()

    private val homeAdapter by ViewLifecycleDelegate { HomeAdapter(::onMovieClicked) }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        listenOnMoviesPagedData()
        listenOnPagerLoadStates()
    }

    private fun listenOnMoviesPagedData() = viewLifecycleOwner.lifecycleScope.launch {
        homeViewModel.movies.collectLatest { homeAdapter.submitData(it) }
    }

    private fun listenOnPagerLoadStates() = viewLifecycleOwner.lifecycleScope.launch {
        with(binding) {
            homeAdapter.listenOnLoadStates(
                recyclerView,
                loadStateView,
                { homeAdapter.itemCount == 0 },
                getString(R.string.no_popular_movies)
            )
        }
    }

    private fun initUI() = with(binding) {
        Timber.v("before postponeEnterTransition")
        postponeEnterTransition()

        recyclerView.apply {
            adapter = homeAdapter.withLoadStateFooter(
                footer = ListLoadStateAdapter { homeAdapter.retry() }
            )
            layoutManager = GridLayoutManager(context, getSpansCount()).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position >= homeAdapter.itemCount) getSpansCount() else 1
                    }
                }
            }
            doOnPreDraw { startPostponedEnterTransition(); Timber.v("startPostponedEnterTransition called") }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                private var dySum = 0

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE,
                        RecyclerView.SCROLL_STATE_SETTLING,
                        -> {
                            pageTitle.isVisible = (dySum <= 0)
                            dySum = 0
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    dySum += dy
                }
            })
        }
    }

    private fun getSpansCount() = if (isPortrait()) 2 else 4

    private fun onMovieClicked(
        movie: Movie,
        posterImageView: ImageView,
        titleTextView: TextView,
        dateTextView: TextView,
    ) {
        val extras = FragmentNavigatorExtras(
            posterImageView to posterImageView.transitionName,
            titleTextView to titleTextView.transitionName,
            dateTextView to dateTextView.transitionName,
        )
        findNavController().navigateSafe(
            HomeFragmentDirections.actionNavigationHomeToMovieDetails(movie), extras
        )
    }
}