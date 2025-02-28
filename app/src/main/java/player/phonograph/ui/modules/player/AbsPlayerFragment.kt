package player.phonograph.ui.modules.player

import com.github.chr56.android.menu_dsl.attach
import com.github.chr56.android.menu_dsl.menuItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import lib.storage.launcher.IOpenFileStorageAccessible
import lib.storage.launcher.OpenDocumentContract
import org.koin.core.context.GlobalContext
import player.phonograph.App
import player.phonograph.R
import player.phonograph.mechanism.IFavorite
import player.phonograph.mechanism.event.MediaStoreTracker
import player.phonograph.model.Song
import player.phonograph.model.lyrics.LrcLyrics
import player.phonograph.service.MusicPlayerRemote
import player.phonograph.service.queue.CurrentQueueState
import player.phonograph.service.queue.QueueManager
import player.phonograph.ui.dialogs.LyricsDialog
import player.phonograph.ui.dialogs.NowPlayingScreenPreferenceDialog
import player.phonograph.ui.dialogs.QueueSnapshotsDialog
import player.phonograph.ui.dialogs.SleepTimerDialog
import player.phonograph.ui.dialogs.SpeedControlDialog
import player.phonograph.ui.modules.panel.AbsMusicServiceFragment
import player.phonograph.ui.modules.player.PlayerAlbumCoverFragment.Companion.VISIBILITY_ANIM_DURATION
import player.phonograph.ui.modules.playlist.dialogs.CreatePlaylistDialogActivity
import player.phonograph.util.NavigationUtil
import player.phonograph.util.theme.getTintedDrawable
import player.phonograph.util.theme.themeFooterColor
import player.phonograph.util.theme.tintButtons
import player.phonograph.util.ui.setUpFastScrollRecyclerViewColor
import player.phonograph.util.warning
import util.theme.color.toolbarIconColor
import util.theme.materials.MaterialColor
import util.theme.view.menu.setMenuColor
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.annotation.MainThread
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withCreated
import androidx.lifecycle.withResumed
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.animation.AnimatorSet
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbsPlayerFragment :
        AbsMusicServiceFragment()/* , PaletteColorHolder */, SlidingUpPanelLayout.PanelSlideListener {

    protected lateinit var playbackControlsFragment: AbsPlayerControllerFragment<*>
    protected val viewModel: PlayerFragmentViewModel by viewModels()
    protected val lyricsViewModel: LyricsViewModel by viewModels({ requireActivity() })

    // recycle view
    protected lateinit var layoutManager: LinearLayoutManager
    protected lateinit var playingQueueAdapter: PlayingQueueAdapter
    private var _wrappedAdapter: RecyclerView.Adapter<*>? = null
    protected val wrappedAdapter: RecyclerView.Adapter<*> get() = _wrappedAdapter!!
    private var _recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    protected val recyclerViewDragDropManager: RecyclerViewDragDropManager get() = _recyclerViewDragDropManager!!

    protected lateinit var playerToolbar: Toolbar

    internal lateinit var impl: Impl

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initToolbar()
        playbackControlsFragment =
            childFragmentManager.findFragmentById(R.id.playback_controls_fragment) as AbsPlayerControllerFragment<*>

        observeState()
        lastPaletteColor = themeFooterColor(view.context)
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(requireActivity())
        playingQueueAdapter = PlayingQueueAdapter(requireActivity())
        playingQueueAdapter.dataset = MusicPlayerRemote.playingQueue
        playingQueueAdapter.current = MusicPlayerRemote.position
        _recyclerViewDragDropManager = RecyclerViewDragDropManager()
        _wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(playingQueueAdapter)
        recyclerViewDragDropManager.setInitiateOnTouch(true)
        recyclerViewDragDropManager.setInitiateOnLongPress(false)

        val playerRecyclerView = fetchRecyclerView()

        val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
        playerRecyclerView.setUpFastScrollRecyclerViewColor(requireContext(), MaterialColor.Grey._500.asColor)
        playerRecyclerView.layoutManager = layoutManager
        playerRecyclerView.adapter = wrappedAdapter
        playerRecyclerView.itemAnimator = animator
        recyclerViewDragDropManager.attachRecyclerView(playerRecyclerView)
        layoutManager.scrollToPositionWithOffset(MusicPlayerRemote.position + 1, 0)
    }

    protected abstract fun fetchRecyclerView(): FastScrollRecyclerView

    override fun onDestroyView() {
        favoriteMenuItem = null
        lyricsMenuItem = null
        super.onDestroyView()
        _recyclerViewDragDropManager?.let {
            recyclerViewDragDropManager.release()
            _recyclerViewDragDropManager = null
        }
        _wrappedAdapter?.let {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            _wrappedAdapter = null
        }
        currentAnimatorSet?.end()
        currentAnimatorSet?.cancel()
    }

    //
    // Toolbar
    //

    private var lyricsMenuItem: MenuItem? = null

    private fun initToolbar() {
        playerToolbar = getImplToolbar()
        playerToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        playerToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        requireContext().attach(playerToolbar.menu) {
            // visible
            menuItem(getString(R.string.lyrics)) {
                order = 0
                icon = requireContext()
                    .getTintedDrawable(R.drawable.ic_comment_text_outline_white_24dp, Color.WHITE)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_ALWAYS
                visible = false
                itemId = R.id.action_show_lyrics
                onClick {
                    if (lyricsViewModel.hasLyrics) {
                        LyricsDialog().show(childFragmentManager, "LYRICS")
                    }
                    true
                }
            }.also {
                lyricsMenuItem = it
            }

            menuItem(getString(R.string.action_add_to_favorites)) {
                order = 1
                icon =
                    requireContext().getTintedDrawable(
                        R.drawable.ic_favorite_border_white_24dp, Color.WHITE
                    )
                // default state
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_ALWAYS
                itemId = R.id.action_toggle_favorite
                onClick {
                    val song = viewModel.currentSong.value
                    if (song != null) lifecycleScope.launch(Dispatchers.IO) {
                        val favorite = GlobalContext.get().get<IFavorite>()
                        favorite.toggleFavorite(context, song)
                    }
                    true
                }
            }.apply {
                favoriteMenuItem = this
            }

            // collapsed
            menuItem {
                title = getString(R.string.action_clear_playing_queue)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    MusicPlayerRemote.queueManager.clearQueue()
                    true
                }
            }
            menuItem {
                title = getString(R.string.action_save_playing_queue)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    startActivity(
                        CreatePlaylistDialogActivity.Parameter.buildLaunchingIntentForCreating(
                            requireContext(), MusicPlayerRemote.playingQueue
                        )
                    )
                    true
                }
            }
            menuItem {
                title = getString(R.string.action_choose_lyrics)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    val activity = requireActivity()
                    val accessor = activity as? IOpenFileStorageAccessible
                    if (accessor != null) {
                        accessor.openFileStorageAccessDelegate.launch(OpenDocumentContract.Config(arrayOf("*/*"))) { uri ->
                            if (uri == null) return@launch
                            CoroutineScope(Dispatchers.IO).launch {
                                val lyricsViewModel = ViewModelProvider(activity)[LyricsViewModel::class.java]
                                lyricsViewModel.appendLyricsFrom(activity, uri)
                            }
                        }
                    } else {
                        warning("Lyrics", "Can not open file from $activity")
                    }
                    true
                }
            }
            menuItem {
                title = getString(R.string.action_sleep_timer)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    SleepTimerDialog()
                        .show(childFragmentManager, "SET_SLEEP_TIMER")
                    true
                }
            }
            menuItem {
                title = getString(R.string.equalizer)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    NavigationUtil.openEqualizer(requireActivity())
                    true
                }
            }
            menuItem {
                title = getString(R.string.action_speed)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    SpeedControlDialog().show(childFragmentManager, "SPEED_CONTROL_DIALOG")
                    true
                }
            }
            menuItem {
                title = getString(R.string.change_now_playing_screen)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    NowPlayingScreenPreferenceDialog()
                        .show(childFragmentManager, "NOW_PLAYING_SCREEN")
                    true
                }
            }
            menuItem {
                title = context.getString(R.string.playing_queue_history)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    QueueSnapshotsDialog()
                        .show(childFragmentManager, "QUEUE_SNAPSHOTS")
                    true
                }
            }
            menuItem {
                title = getString(R.string.action_clean_missing_items)
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                onClick {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.action_clean)
                        .setMessage(R.string.action_clean_missing_items)
                        .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                            val queueManager: QueueManager = GlobalContext.get().get()
                            queueManager.clean()
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .tintButtons()
                        .show()
                    true
                }
            }
        }
        setMenuColor(requireContext(), playerToolbar, playerToolbar.menu, Color.WHITE)

    }

    protected abstract fun getImplToolbar(): Toolbar

    private fun showToolbar(toolbar: View) {
        toolbar.visibility = View.VISIBLE
        toolbar.animate().alpha(1f).duration = VISIBILITY_ANIM_DURATION
    }

    private fun hideToolbar(toolbar: View) {
        toolbar.animate().alpha(0f).setDuration(VISIBILITY_ANIM_DURATION)
            .withEndAction { toolbar.visibility = View.GONE }
    }

    protected abstract fun getToolBarContainer(): View?

    var favoriteMenuItem: MenuItem? = null

    override fun onPause() {
        recyclerViewDragDropManager.cancelDrag()
        super.onPause()
    }

    open fun onShow() {
        playbackControlsFragment.show()
    }

    open fun onHide() {
        playbackControlsFragment.hide()
        collapseToNormal()
    }

    override fun onPanelStateChanged(panel: View, previousState: PanelState, newState: PanelState) {
        when (newState) {
            PanelState.EXPANDED  -> {
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, collapseBackPressedCallback)
            }

            PanelState.COLLAPSED -> {
                collapseBackPressedCallback.remove()
                lifecycleScope.launch(Dispatchers.Main) {
                    withCreated {
                        resetToCurrentPosition()
                    }
                }
            }

            PanelState.ANCHORED  -> {
                // this fixes a bug where the panel would get stuck for some reason
                collapseToNormal()
            }

            else                 -> Unit
        }
    }

    protected val collapseBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                collapseToNormal()
            }
        }

    protected abstract fun collapseToNormal()

    protected abstract fun resetToCurrentPosition()

    private lateinit var listener: MediaStoreListener
    override fun onCreate(savedInstanceState: Bundle?) {
        listener = MediaStoreListener()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(listener)
    }

    private inner class MediaStoreListener : MediaStoreTracker.LifecycleListener() {
        override fun onMediaStoreChanged() {
            lifecycleScope.launch(Dispatchers.Main) { updateAdapter() }
            viewModel.updateFavoriteState(context ?: App.instance, MusicPlayerRemote.currentSong)
        }
    }


    @MainThread
    protected open suspend fun updateAdapter() {
        lifecycle.withCreated {
            playingQueueAdapter.dataset = MusicPlayerRemote.playingQueue
            playingQueueAdapter.current = MusicPlayerRemote.position
        }
    }

    internal interface Impl {
        fun init()
        fun updateCurrentSong(song: Song)
        fun setUpPanelAndAlbumCoverHeight()
        fun generateAnimators(@ColorInt oldColor: Int, @ColorInt newColor: Int): AnimatorSet
        fun forceChangeColor(@ColorInt newColor: Int)
    }

    protected var lastPaletteColor = 0
    protected var currentAnimatorSet: AnimatorSet? = null


    @MainThread
    private fun changeHighlightColor(newColor: Int, animated: Boolean = true) {
        if (animated) {
            currentAnimatorSet?.end()
            currentAnimatorSet?.cancel()
            currentAnimatorSet = impl.generateAnimators(lastPaletteColor, newColor).also {
                it.doOnEnd {
                    lastPaletteColor = newColor
                }
                it.start()
            }
        } else {
            impl.forceChangeColor(newColor)
        }
    }

    private fun observeState() {
        observe(CurrentQueueState.queue) { queue ->
            playingQueueAdapter.dataset = queue.get() ?: MusicPlayerRemote.playingQueue
            playingQueueAdapter.current = MusicPlayerRemote.position
        }
        observe(CurrentQueueState.position) { position ->
            playingQueueAdapter.current = position
        }
        observe(CurrentQueueState.currentSong) { song ->
            viewModel.updateCurrentSong(requireContext(), song)
            if (song != null) withStarted { impl.updateCurrentSong(song) }
        }
        observe(viewModel.currentSong) {
            if (it != null) lyricsViewModel.loadLyricsFor(requireContext(), it)
        }
        observe(CurrentQueueState.shuffleMode) {
            updateAdapter()
        }
        observe(viewModel.favoriteState) {
            if (it.first != null && it.first == viewModel.currentSong.value) {
                val isFavorite = it.second
                lifecycle.withStarted {
                    val res =
                        if (isFavorite) R.drawable.ic_favorite_white_24dp else R.drawable.ic_favorite_border_white_24dp
                    val color = toolbarIconColor(requireContext(), Color.TRANSPARENT)
                    favoriteMenuItem?.apply {
                        icon = requireContext().getTintedDrawable(res, color)
                        title =
                            if (isFavorite) getString(R.string.action_remove_from_favorites)
                            else getString(R.string.action_add_to_favorites)
                    }
                }
            }
        }
        observe(viewModel.showToolbar) {
            val container = getToolBarContainer() ?: return@observe
            withStarted {
                if (it) {
                    showToolbar(container)
                } else {
                    hideToolbar(container)
                }
            }
        }
        observe(lyricsViewModel.lyricsInfo) { lyricsInfo ->
            withContext(Dispatchers.Main) {
                lyricsMenuItem?.isVisible = !lyricsInfo.isNullOrEmpty()
                val activated = lyricsInfo?.activatedLyrics
                MusicPlayerRemote.replaceLyrics(activated as? LrcLyrics)
            }
        }
        observe(viewModel.paletteColor) { newColor ->
            playbackControlsFragment.modifyColor(newColor)
            withResumed {
                changeHighlightColor(newColor, lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            }
        }
    }

    /* override val paletteColor @ColorInt get() = viewModel.paletteColor.value */
    val paletteColorState get() = viewModel.paletteColor


    protected inline fun <reified T> observe(
        flow: StateFlow<T>,
        state: Lifecycle.State = Lifecycle.State.CREATED,
        lifecycle: Lifecycle = this.lifecycle,
        scope: CoroutineScope = lifecycle.coroutineScope,
        flowCollector: FlowCollector<T>,
    ) {
        scope.launch {
            lifecycle.repeatOnLifecycle(state) {
                flow.collect(flowCollector)
            }
        }
    }

}
