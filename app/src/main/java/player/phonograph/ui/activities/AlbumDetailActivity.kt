package player.phonograph.ui.activities

import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import lib.phonograph.misc.menuProvider
import mt.pref.ThemeColor.primaryColor
import mt.tint.requireLightStatusbar
import mt.tint.setActivityToolbarColor
import mt.tint.setActivityToolbarColorAuto
import mt.tint.setNavigationBarColor
import mt.tint.viewtint.tintMenuActionIcons
import mt.util.color.primaryTextColor
import mt.util.color.secondaryTextColor
import player.phonograph.R
import player.phonograph.actions.menu.albumDetailToolbar
import player.phonograph.coil.loadImage
import player.phonograph.coil.target.PaletteTargetBuilder
import player.phonograph.databinding.ActivityAlbumDetailBinding
import player.phonograph.mechanism.event.MediaStoreTracker
import player.phonograph.misc.IPaletteColorProvider
import player.phonograph.model.Album
import player.phonograph.model.getReadableDurationString
import player.phonograph.model.getYearString
import player.phonograph.model.songCountString
import player.phonograph.model.totalDuration
import player.phonograph.ui.activities.base.AbsSlidingMusicPanelActivity
import player.phonograph.ui.fragments.pages.adapter.SongDisplayAdapter
import player.phonograph.util.NavigationUtil.goToArtist
import player.phonograph.util.theme.getTintedDrawable
import player.phonograph.util.ui.setUpFastScrollRecyclerViewColor
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Be careful when changing things in this Activity!
 */
class AlbumDetailActivity : AbsSlidingMusicPanelActivity(), IPaletteColorProvider {

    companion object {
        const val TAG_EDITOR_REQUEST = 2001
        const val EXTRA_ALBUM_ID = "extra_album_id"
    }

    private lateinit var viewBinding: ActivityAlbumDetailBinding
    private val model: AlbumDetailActivityViewModel by viewModels()

    private lateinit var adapter: SongDisplayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        val albumID = intent.extras!!.getLong(EXTRA_ALBUM_ID)
        model.albumId = albumID
        load()

        viewBinding = ActivityAlbumDetailBinding.inflate(layoutInflater)

        autoSetStatusBarColor = false
        autoSetNavigationBarColor = false
        autoSetTaskDescriptionColor = false
        super.onCreate(savedInstanceState)

        // activity
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        addMenuProvider(menuProvider(this::setupMenu))
        setActivityToolbarColorAuto(viewBinding.toolbar)

        // content
        setUpViews()

        // MediaStore
        lifecycle.addObserver(MediaStoreListener())
    }

    override fun createContentView(): View = wrapSlidingMusicPanel(viewBinding.root)

    private fun setUpViews() {
        viewBinding.innerAppBar.addOnOffsetChangedListener { _, verticalOffset ->
            viewBinding.recyclerView.setPaddingTop(viewBinding.innerAppBar.totalScrollRange + verticalOffset)
        }
        // setUpSongsAdapter
        adapter =
            AlbumSongDisplayAdapter(this, album.songs, R.layout.item_list).apply {
                useImageText = true
                usePalette = false
            }
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        viewBinding.recyclerView.adapter = adapter
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (adapter.itemCount == 0) finish()
            }
        })
        model.isRecyclerViewPrepared = true
        // jump
        viewBinding.artistText.setOnClickListener {
            goToArtist(this, album.artistId)
        }
        // paletteColor
        lifecycleScope.launch {
            model.paletteColor.collect {
                updateColors(it)
            }
        }
    }

    private fun RecyclerView.setPaddingTop(top: Int) = setPadding(paddingLeft, top, paddingRight, paddingBottom)

    private fun updateColors(color: Int) {
        viewBinding.recyclerView.setUpFastScrollRecyclerViewColor(this, color)
        viewBinding.header.setBackgroundColor(color)
        setNavigationBarColor(color)
        setTaskDescriptionColor(color)


        viewBinding.toolbar.setBackgroundColor(color)
        setSupportActionBar(viewBinding.toolbar) // needed to auto readjust the toolbar content color
        setStatusbarColor(color)
        setActivityToolbarColor(viewBinding.toolbar, color)

        val secondaryTextColor = secondaryTextColor(color)

        val artistIcon = getTintedDrawable(R.drawable.ic_person_white_24dp, secondaryTextColor)!!
        viewBinding.artistText.setCompoundDrawablesWithIntrinsicBounds(artistIcon, null, null, null)
        viewBinding.artistText.setTextColor(primaryTextColor(color))
        viewBinding.artistText.compoundDrawablePadding = 16

        val songCountIcon = getTintedDrawable(R.drawable.ic_music_note_white_24dp, secondaryTextColor)!!
        viewBinding.songCountText.setTextColor(secondaryTextColor)
        viewBinding.songCountText.setCompoundDrawablesWithIntrinsicBounds(songCountIcon, null, null, null)
        viewBinding.songCountText.compoundDrawablePadding = 16

        val durationIcon = getTintedDrawable(R.drawable.ic_timer_white_24dp, secondaryTextColor)!!
        viewBinding.durationText.setTextColor(secondaryTextColor)
        viewBinding.durationText.setCompoundDrawablesWithIntrinsicBounds(durationIcon, null, null, null)
        viewBinding.durationText.compoundDrawablePadding = 16

        val albumYearIcon = getTintedDrawable(R.drawable.ic_event_white_24dp, secondaryTextColor)!!
        viewBinding.albumYearText.setTextColor(secondaryTextColor)
        viewBinding.albumYearText.setCompoundDrawablesWithIntrinsicBounds(albumYearIcon, null, null, null)
        viewBinding.albumYearText.compoundDrawablePadding = 16

        viewModel.updateActivityColor(color)
    }

    override val paletteColor: StateFlow<Int>
        get() = model.paletteColor

    private val album: Album get() = model.album

    private fun load() {
        val defaultColor = primaryColor(this)
        model.loadDataSet(
            this
        ) { album, songs ->
            updateAlbumsInfo(album)
            adapter.dataset = songs
            loadImage(this)
                .from(album.safeGetFirstSong())
                .into(PaletteTargetBuilder(defaultColor)
                    .onResourceReady { result, palette ->
                        viewBinding.image.setImageDrawable(result)
                        model.paletteColor.update { palette }
                    }
                    .onFail {
                        viewBinding.image.setImageResource(R.drawable.default_album_art)
                        model.paletteColor.update { defaultColor }
                    }
                    .build())
                .enqueue()
        }
    }

    private fun updateAlbumsInfo(album: Album) {
        supportActionBar!!.title = album.title
        viewBinding.artistText.text = album.artistName
        viewBinding.songCountText.text = songCountString(this, album.songCount)
        viewBinding.durationText.text = getReadableDurationString(album.songs.totalDuration())
        viewBinding.albumYearText.text = getYearString(album.year)
    }

    private fun setupMenu(menu: Menu) {
        albumDetailToolbar(menu, this, album, primaryTextColor(viewModel.activityColor.value))
        tintMenuActionIcons(viewBinding.toolbar, menu, primaryTextColor(viewModel.activityColor.value))
    }

    private var isWikiPreLoaded = false
    private val wikiDialog: MaterialDialog by lazy(LazyThreadSafetyMode.NONE) {
        MaterialDialog(this)
            .title(null, album.title)
            .message(R.string.loading)
            .positiveButton(android.R.string.ok, null, null)
            .apply {
                getActionButton(WhichButton.POSITIVE).updateTextColor(accentColor)
                getActionButton(WhichButton.NEGATIVE).updateTextColor(accentColor)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAG_EDITOR_REQUEST) {
            load()
            setResult(RESULT_OK)
        }
    }

    override fun onBackPressed() {
        viewBinding.recyclerView.stopScroll()
        super.onBackPressed()
    }

    private inner class MediaStoreListener : MediaStoreTracker.LifecycleListener() {
        override fun onMediaStoreChanged() {
            load()
        }
    }

    override fun setStatusbarColor(color: Int) {
        super.setStatusbarColor(color)
        requireLightStatusbar(false)
    }
}
