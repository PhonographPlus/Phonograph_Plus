package player.phonograph.ui.activities

import lib.phonograph.misc.menuProvider
import mt.pref.ThemeColor
import player.phonograph.actions.menu.genreDetailToolbar
import player.phonograph.databinding.ActivityGenreDetailBinding
import player.phonograph.mechanism.event.MediaStoreTracker
import player.phonograph.model.Genre
import player.phonograph.model.ItemLayoutStyle
import player.phonograph.model.Song
import player.phonograph.repo.loader.Songs
import player.phonograph.ui.activities.base.AbsSlidingMusicPanelActivity
import player.phonograph.ui.adapter.ConstDisplayConfig
import player.phonograph.ui.fragments.pages.adapter.SongDisplayAdapter
import player.phonograph.util.parcelable
import player.phonograph.util.ui.setUpFastScrollRecyclerViewColor
import util.theme.view.toolbar.setToolbarColor
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class GenreDetailActivity : AbsSlidingMusicPanelActivity() {

    private var _viewBinding: ActivityGenreDetailBinding? = null
    private val binding: ActivityGenreDetailBinding get() = _viewBinding!!

    private lateinit var genre: Genre
    private lateinit var adapter: SongDisplayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        genre = parseIntent(intent) ?: throw IllegalArgumentException()
        loadDataSet(this)
        _viewBinding = ActivityGenreDetailBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)

        setUpToolBar()
        setUpRecyclerView()

        lifecycle.addObserver(MediaStoreListener())
    }

    override fun createContentView(): View {
        return wrapSlidingMusicPanel(binding.root)
    }

    private var isRecyclerViewPrepared: Boolean = false

    private fun loadDataSet(context: Context) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: List<Song> = Songs.genres(context, genre.id)

            while (!isRecyclerViewPrepared) yield() // wait until ready

            withContext(Dispatchers.Main) {
                if (isRecyclerViewPrepared) adapter.dataset = list
            }
        }
    }

    private fun setUpRecyclerView() {
        adapter =
            SongDisplayAdapter(
                this,
                ConstDisplayConfig(layoutStyle = ItemLayoutStyle.LIST, usePalette = false, showSectionName = false)
            )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GenreDetailActivity)
            adapter = this@GenreDetailActivity.adapter
        }
        binding.recyclerView.setUpFastScrollRecyclerViewColor(this, accentColor)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
        isRecyclerViewPrepared = true
    }

    private fun setUpToolBar() {
        binding.toolbar.setBackgroundColor(primaryColor)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = genre.name
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        addMenuProvider(menuProvider(this::setupMenu))
        setToolbarColor(binding.toolbar, ThemeColor.primaryColor(this))
    }

    private fun setupMenu(menu: Menu) {
        genreDetailToolbar(menu, this, genre)
    }


    private inner class MediaStoreListener : MediaStoreTracker.LifecycleListener() {
        override fun onMediaStoreChanged() {
            loadDataSet(this@GenreDetailActivity)
        }
    }

    private fun checkIsEmpty() {
        binding.empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        binding.recyclerView.adapter = null
        super.onDestroy()
        _viewBinding = null
    }

    companion object {
        private const val EXTRA_GENRE = "extra_genre"
        fun launchIntent(from: Context, genre: Genre): Intent =
            Intent(from, GenreDetailActivity::class.java).apply {
                putExtra(EXTRA_GENRE, genre)
            }

        private fun parseIntent(intent: Intent) = intent.extras?.parcelable<Genre>(EXTRA_GENRE)
    }
}
