/*
 * Copyright (c) 2022 chr_56 & Abou Zeid (kabouzeid) (original author)
 */

package player.phonograph.ui.fragments.pages

import player.phonograph.R
import player.phonograph.model.Genre
import player.phonograph.repo.loader.Genres
import player.phonograph.ui.adapter.DisplayAdapter
import player.phonograph.ui.fragments.pages.adapter.GenreDisplayAdapter
import androidx.fragment.app.viewModels
import android.content.Context
import kotlinx.coroutines.CoroutineScope

class GenrePage : AbsDisplayPage<Genre, DisplayAdapter<Genre>>() {

    override val viewModel: AbsDisplayPageViewModel<Genre> get() = _viewModel

    private val _viewModel: GenrePageViewModel by viewModels()

    class GenrePageViewModel : AbsDisplayPageViewModel<Genre>() {
        override suspend fun loadDataSetImpl(context: Context, scope: CoroutineScope): Collection<Genre> {
            return Genres.all(context)
        }

        override val headerTextRes: Int get() = R.plurals.item_genres
    }

    override fun displayConfig(): PageDisplayConfig = GenrePageDisplayConfig(requireContext())

    override fun initAdapter(): DisplayAdapter<Genre> {
        return GenreDisplayAdapter(mainActivity)
    }

    companion object {
        const val TAG = "GenrePage"
    }
}
