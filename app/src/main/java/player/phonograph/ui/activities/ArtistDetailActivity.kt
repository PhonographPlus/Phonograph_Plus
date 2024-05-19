package player.phonograph.ui.activities

import com.github.chr56.android.menu_dsl.attach
import com.github.chr56.android.menu_dsl.menuItem
import lib.phonograph.misc.menuProvider
import mt.pref.ThemeColor
import mt.tint.requireLightStatusbar
import mt.tint.setActivityToolbarColor
import mt.tint.setActivityToolbarColorAuto
import mt.tint.setNavigationBarColor
import mt.tint.viewtint.tintMenuActionIcons
import mt.util.color.primaryTextColor
import mt.util.color.resolveColor
import mt.util.color.secondaryTextColor
import mt.util.color.toolbarTitleColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import player.phonograph.App
import player.phonograph.R
import player.phonograph.actions.menu.artistDetailToolbar
import player.phonograph.coil.CustomArtistImageStore
import player.phonograph.databinding.ActivityArtistDetailBinding
import player.phonograph.mechanism.event.MediaStoreTracker
import player.phonograph.misc.IPaletteColorProvider
import player.phonograph.model.Artist
import player.phonograph.model.albumCountString
import player.phonograph.model.getReadableDurationString
import player.phonograph.model.songCountString
import player.phonograph.model.totalDuration
import player.phonograph.repo.loader.Songs
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import player.phonograph.ui.activities.base.AbsSlidingMusicPanelActivity
import player.phonograph.ui.adapter.ConstDisplayConfig
import player.phonograph.ui.adapter.ItemLayoutStyle
import player.phonograph.ui.fragments.pages.adapter.SongDisplayAdapter
import player.phonograph.util.theme.getTintedDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtistDetailActivity : AbsSlidingMusicPanelActivity(), IPaletteColorProvider {

    private lateinit var viewBinding: ActivityArtistDetailBinding
    private val model: ArtistDetailActivityViewModel by viewModel { parametersOf(parseIntent(intent)) }

    private lateinit var albumAdapter: HorizontalAlbumDisplayAdapter
    private lateinit var songAdapter: SongDisplayAdapter

    private var usePalette = Setting(App.instance)[Keys.albumArtistColoredFooters].data
        set(value) {
            field = value
            Setting(App.instance)[Keys.albumArtistColoredFooters].data = usePalette
            albumAdapter.config = ConstDisplayConfig(ItemLayoutStyle.LIST, usePalette)
            val dataset = albumAdapter.dataset
            synchronized(albumAdapter) {
                albumAdapter.dataset = emptyList()
                albumAdapter.dataset = dataset
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityArtistDetailBinding.inflate(layoutInflater)
        model.load(this)

        autoSetStatusBarColor = false
        autoSetNavigationBarColor = false
        autoSetTaskDescriptionColor = false

        super.onCreate(savedInstanceState)

        setUpToolbar()
        setUpViews()
        observeData()

        lifecycle.addObserver(MediaStoreListener())
    }

    override fun createContentView(): View = wrapSlidingMusicPanel(viewBinding.root)

    private fun setUpViews() {
        viewBinding.innerAppBar.addOnOffsetChangedListener { _, verticalOffset ->
            viewBinding.mainContent.setPaddingTop(verticalOffset)
        }

        songAdapter =
            SongDisplayAdapter(this, ConstDisplayConfig(ItemLayoutStyle.LIST, false))
        with(viewBinding.songsRecycleView) {
            adapter = songAdapter
            layoutManager =
                LinearLayoutManager(this@ArtistDetailActivity, VERTICAL, false)
        }

        albumAdapter =
            HorizontalAlbumDisplayAdapter(this)
        with(viewBinding.albumRecycleView) {
            adapter = albumAdapter
            layoutManager = LinearLayoutManager(this@ArtistDetailActivity, HORIZONTAL, false)
        }

        setColors(resolveColor(this, R.attr.defaultFooterColor))
    }

    private fun observeData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.artist.collect {
                    setUpArtist(it ?: Artist())
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.albums.collect {
                    albumAdapter.dataset = it ?: emptyList()
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.songs.collect {
                    songAdapter.dataset = it ?: emptyList()
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.paletteColor.collect { color ->
                    setColors(color)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> if (resultCode == RESULT_OK) {
                val artist = model.artist.value!!
                CustomArtistImageStore.instance(this)
                    .setCustomArtistImage(this, artist.id, artist.name, data!!.data!!)
            }

            else                      -> if (resultCode == RESULT_OK) {
                model.load(this)
            }
        }
    }

    private fun setColors(color: Int) {
        viewBinding.header.setBackgroundColor(color)
        if (ThemeColor.coloredNavigationBar(this)) setNavigationBarColor(color)
        setTaskDescriptionColor(color)

        setSupportActionBar(viewBinding.toolbar) // needed to auto readjust the toolbar content color
        setActivityToolbarColor(viewBinding.toolbar, color)
        viewBinding.toolbar.setTitleTextColor(toolbarTitleColor(this, color))

        setStatusbarColor(color)
        val secondaryTextColor = secondaryTextColor(color)
        viewBinding.durationIcon.setImageDrawable(
            getTintedDrawable(R.drawable.ic_timer_white_24dp, secondaryTextColor)
        )
        viewBinding.songCountIcon.setImageDrawable(
            getTintedDrawable(R.drawable.ic_music_note_white_24dp, secondaryTextColor)
        )
        viewBinding.albumCountIcon.setImageDrawable(
            getTintedDrawable(R.drawable.ic_album_white_24dp, secondaryTextColor)
        )
        viewBinding.durationText.setTextColor(secondaryTextColor)
        viewBinding.songCountText.setTextColor(secondaryTextColor)
        viewBinding.albumCountText.setTextColor(secondaryTextColor)
        viewModel.updateActivityColor(color)
    }

    override val paletteColor: StateFlow<Int> get() = model.paletteColor

    private fun setUpToolbar() {
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addMenuProvider(menuProvider(this::setupMenu))
        setActivityToolbarColorAuto(viewBinding.toolbar)
    }

    private fun setupMenu(menu: Menu) {
        artistDetailToolbar(menu, this, model.artist.value ?: Artist(), primaryTextColor(viewModel.activityColor.value))
        attach(menu) {
            menuItem(title = getString(R.string.colored_footers)) {
                checkable = true
                checked = usePalette
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    it.isChecked = !it.isChecked
                    usePalette = it.isChecked
                    true
                }
            }
        }
        tintMenuActionIcons(viewBinding.toolbar, menu, primaryTextColor(viewModel.activityColor.value))
    }

    override fun onBackPressed() {
        viewBinding.albumRecycleView.stopScroll()
        viewBinding.songsRecycleView.stopScroll()
        super.onBackPressed()
    }

    private inner class MediaStoreListener : MediaStoreTracker.LifecycleListener() {
        override fun onMediaStoreChanged() {
            model.load(this@ArtistDetailActivity)
        }
    }

    override fun setStatusbarColor(color: Int) {
        super.setStatusbarColor(color)
        requireLightStatusbar(false)
    }

    private fun setUpArtist(artist: Artist) {
        model.loadArtistImage(this, artist, viewBinding.image)
        supportActionBar!!.title = artist.name
        viewBinding.songCountText.text = songCountString(this, artist.songCount)
        viewBinding.albumCountText.text = albumCountString(this, artist.albumCount)
        viewBinding.durationText.text = getReadableDurationString(Songs.artist(this, artist.id).totalDuration())
    }


    private fun View.setPaddingTop(top: Int) =
        setPadding(paddingLeft, top, paddingRight, paddingBottom)

    companion object {
        const val REQUEST_CODE_SELECT_IMAGE = 1000
        private const val EXTRA_ARTIST_ID = "extra_artist_id"

        fun launchIntent(from: Context, artistId: Long): Intent =
            Intent(from, ArtistDetailActivity::class.java).apply {
                putExtra(EXTRA_ARTIST_ID, artistId)
            }

        private fun parseIntent(intent: Intent): Long = intent.extras?.getLong(EXTRA_ARTIST_ID) ?: -1
    }
}
