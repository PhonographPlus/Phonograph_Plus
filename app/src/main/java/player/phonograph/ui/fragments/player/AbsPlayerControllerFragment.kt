/*
 * Copyright (c) 2022 chr_56 & Abou Zeid (kabouzeid) (original author)
 */

package player.phonograph.ui.fragments.player

import player.phonograph.R
import player.phonograph.misc.MusicProgressViewUpdateHelperDelegate
import player.phonograph.misc.SimpleOnSeekbarChangeListener
import player.phonograph.model.getReadableDurationString
import player.phonograph.service.MusicPlayerRemote
import player.phonograph.service.queue.CurrentQueueState
import player.phonograph.service.queue.RepeatMode
import player.phonograph.service.queue.ShuffleMode
import player.phonograph.ui.modules.panel.AbsMusicServiceFragment
import player.phonograph.ui.views.PlayPauseDrawable
import player.phonograph.util.theme.getTintedDrawable
import player.phonograph.util.theme.themeFooterColor
import player.phonograph.util.theme.themeIconColor
import util.theme.color.isColorLight
import util.theme.color.primaryTextColor
import util.theme.color.secondaryDisabledTextColor
import util.theme.color.secondaryTextColor
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat.createBlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import android.content.Context
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class AbsPlayerControllerFragment<V : ViewBinding> : AbsMusicServiceFragment() {

    abstract val binding: PlayerControllerBinding<V>

    private val progressViewUpdateHelperDelegate =
        MusicProgressViewUpdateHelperDelegate(::updateProgressViews)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(progressViewUpdateHelperDelegate)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.inflate(inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        super.onViewCreated(view, savedInstanceState)

        binding.setUpPrevButton { MusicPlayerRemote.back() }
        binding.setUpNextButton { MusicPlayerRemote.playNextSong() }
        binding.setUpShuffleButton { MusicPlayerRemote.toggleShuffleMode() }
        binding.setUpRepeatButton { MusicPlayerRemote.cycleRepeatMode() }

        val footerColor = themeFooterColor(requireContext())
        _backgroundColor.value = footerColor
        calculateColor(context, footerColor)
        lightColor = context.primaryTextColor(true)

        binding.preparePlayPauseButton(context)
        binding.setPlayPauseButton(binding.playPauseDrawable)
        binding.updatePlayPauseColor(controlsColor)
        binding.updateButtonsColor(controlsColor)

        binding.setUpProgressSlider(lightColor)
        binding.updateProgressTextColor(lightColor)

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                CurrentQueueState.repeatMode.collect { repeatMode ->
                    binding.updateRepeatModeIcon(repeatMode)
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                CurrentQueueState.shuffleMode.collect { shuffleMode ->
                    binding.updateShuffleModeIcon(shuffleMode)
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                MusicPlayerRemote.currentState.collect {
                    binding.updatePlayPauseDrawableState(
                        lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                    )
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                backgroundColor.collect { newColor ->
                    calculateColor(requireContext(), newColor)
                    binding.updatePlayPauseColor(controlsColor)
                    binding.updateButtonsColor(controlsColor)
                    binding.updateRepeatModeIcon(MusicPlayerRemote.repeatMode)
                    binding.updateShuffleModeIcon(MusicPlayerRemote.shuffleMode)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        binding.setPlayPauseButton(binding.playPauseDrawable)
        binding.updatePlayPauseDrawableState(true)
    }

    override fun onServiceDisconnected() {
        super.onServiceDisconnected()
        val activity = requireActivity()
        binding.setPlayPauseButton(
            activity.getTintedDrawable(R.drawable.ic_refresh_white_24dp, themeIconColor(activity))
        )
    }

    private val _backgroundColor: MutableStateFlow<Int> = MutableStateFlow(0)
    protected val backgroundColor get() = _backgroundColor.asStateFlow()

    private var lightColor = 0
    private var controlsColor = 0
    private var disabledControlsColor = 0
    private fun calculateColor(context: Context, backgroundColor: Int) {
        val darkmode = !isColorLight(backgroundColor)
        controlsColor = context.secondaryTextColor(darkmode)
        disabledControlsColor = context.secondaryDisabledTextColor(darkmode)
    }

    private fun updateProgressViews(progress: Int, total: Int) = binding.updateProgressViews(progress, total)

    fun modifyColor(backgroundColor: Int) {
        _backgroundColor.update { backgroundColor }
    }

    abstract fun show()
    abstract fun hide()


    @Suppress("PropertyName", "MemberVisibilityCanBePrivate")
    abstract class PlayerControllerBinding<V : ViewBinding> {

        //region Binding
        protected var _viewBinding: V? = null
        val viewBinding: V get() = _viewBinding!!

        val isAttached get() = _viewBinding != null


        protected var _prevButton: ImageButton? = null
        val prevButton: ImageButton get() = _prevButton!!
        protected var _nextButton: ImageButton? = null
        val nextButton: ImageButton get() = _nextButton!!

        protected var _repeatButton: ImageButton? = null
        val repeatButton: ImageButton get() = _repeatButton!!
        protected var _shuffleButton: ImageButton? = null
        val shuffleButton: ImageButton get() = _shuffleButton!!

        protected var _progressSlider: SeekBar? = null
        val progressSlider: SeekBar get() = _progressSlider!!
        protected var _songTotalTime: TextView? = null
        val songTotalTime: TextView get() = _songTotalTime!!
        protected var _songCurrentProgress: TextView? = null
        val songCurrentProgress: TextView get() = _songCurrentProgress!!
        //endregion


        abstract fun bind(viewBinding: V)
        abstract fun inflate(inflater: LayoutInflater): View
        open fun unbind() {
            _prevButton = null
            _nextButton = null
            _repeatButton = null
            _shuffleButton = null
            _progressSlider = null
            _songTotalTime = null
            _songCurrentProgress = null
            _viewBinding = null
        }


        //region Color
        fun updateButtonsColor(@ColorInt color: Int) {
            nextButton.setColorFilter(color, SRC_IN)
            prevButton.setColorFilter(color, SRC_IN)
            repeatButton.setColorFilter(color, SRC_IN)
            shuffleButton.setColorFilter(color, SRC_IN)
        }

        fun updateRepeatModeIcon(repeatMode: RepeatMode) =
            repeatButton.setImageResource(
                when (repeatMode) {
                    RepeatMode.NONE               -> R.drawable.ic_repeat_off_white_24dp
                    RepeatMode.REPEAT_QUEUE       -> R.drawable.ic_repeat_white_24dp
                    RepeatMode.REPEAT_SINGLE_SONG -> R.drawable.ic_repeat_one_white_24dp
                }
            )

        fun updateShuffleModeIcon(shuffleMode: ShuffleMode) =
            when (shuffleMode) {
                ShuffleMode.NONE    -> shuffleButton.setImageResource(R.drawable.ic_shuffle_disabled_white_24dp)
                ShuffleMode.SHUFFLE -> shuffleButton.setImageResource(R.drawable.ic_shuffle_white_24dp)
            }

        fun updateProgressTextColor(color: Int) {
            songTotalTime.setTextColor(color)
            songCurrentProgress.setTextColor(color)
        }
        //endregion

        //region Text
        fun updateProgressViews(progress: Int, total: Int) {
            progressSlider.max = total
            progressSlider.progress = progress
            songTotalTime.text = getReadableDurationString(total.toLong())
            songCurrentProgress.text = getReadableDurationString(progress.toLong())
        }
        //endregion


        //region Behaviour
        lateinit var playPauseDrawable: PlayPauseDrawable
        abstract fun preparePlayPauseButton(context: Context)
        abstract fun setPlayPauseButton(drawable: Drawable?)
        abstract fun updatePlayPauseColor(controlsColor: Int)

        fun updatePlayPauseDrawableState(animate: Boolean) {
            playPauseDrawable.update(!MusicPlayerRemote.isPlaying, animate)
        }

        fun setUpPrevButton(onClickListener: View.OnClickListener) {
            prevButton.setOnClickListener(onClickListener)
        }

        fun setUpNextButton(onClickListener: View.OnClickListener) {
            nextButton.setOnClickListener(onClickListener)
        }

        fun setUpShuffleButton(onClickListener: View.OnClickListener) {
            shuffleButton.setOnClickListener(onClickListener)
        }

        fun setUpRepeatButton(onClickListener: View.OnClickListener) {
            repeatButton.setOnClickListener(onClickListener)
        }

        fun setUpProgressSlider(color: Int) {
            val colorFilter = createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_IN)
            progressSlider.thumb.mutate().colorFilter = colorFilter
            progressSlider.progressDrawable.mutate().colorFilter = colorFilter
            progressSlider.setOnSeekBarChangeListener(object : SimpleOnSeekbarChangeListener() {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        MusicPlayerRemote.seekTo(progress)
                        updateProgressViews(
                            MusicPlayerRemote.songProgressMillis,
                            MusicPlayerRemote.songDurationMillis
                        )
                    }
                }
            })
        }
        //endregion
    }
}
