/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.ui.modules.player

import player.phonograph.mechanism.lyrics.LyricsLoader
import player.phonograph.model.Song
import player.phonograph.model.lyrics.AbsLyrics
import player.phonograph.model.lyrics.LyricsInfo
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class LyricsViewModel : ViewModel() {

    private var _lyricsInfo: MutableStateFlow<LyricsInfo?> = MutableStateFlow(null)
    val lyricsInfo get() = _lyricsInfo.asStateFlow()

    val hasLyrics get() = !_lyricsInfo.value.isNullOrEmpty()

    private suspend fun replace(lyricsInfo: LyricsInfo?) {
        _lyricsInfo.emit(lyricsInfo)
    }

    private suspend fun activate(lyrics: AbsLyrics) {
        val existed = this@LyricsViewModel.lyricsInfo.value
        val info = if (existed != null) LyricsInfo.withActivated(existed, lyrics) else LyricsInfo(listOf(lyrics), 0)
        _lyricsInfo.emit(info)
    }

    private suspend fun activate(position: Int) {
        val existed = this@LyricsViewModel.lyricsInfo.value
        if (existed != null)
            _lyricsInfo.emit(LyricsInfo.withActivated(existed, position))
    }

    private suspend fun append(lyrics: AbsLyrics) {
        val existed = this@LyricsViewModel.lyricsInfo.value
        val info = if (existed != null) LyricsInfo.withAppended(existed, lyrics) else LyricsInfo(listOf(lyrics), 0)
        _lyricsInfo.emit(info)
    }

    private var loadLyricsJob: Job? = null
    /**
     * start load all possible lyrics for [song]
     */
    fun loadLyricsFor(context: Context, song: Song) {
        // cancel old song's lyrics after switching
        loadLyricsJob?.cancel()
        // load new lyrics
        loadLyricsJob = viewModelScope.launch {
            val enableLyrics = Setting(context)[Keys.enableLyrics].flowData()
            if (enableLyrics) {
                replace(
                    if (song != Song.EMPTY_SONG) {
                        LyricsLoader.loadLyrics(File(song.data), song)
                    } else {
                        null
                    }
                )
            }
        }
    }

    /**
     * replace current activated lyrics with [lyrics]
     */
    suspend fun activateLyrics(lyrics: AbsLyrics) {
        activate(lyrics)
    }

    /**
     * replace current activated lyrics with the [position]th one
     */
    suspend fun activateLyrics(position: Int) {
        activate(position)
    }

    /**
     * append a new lyrics from [uri] to current
     */
    suspend fun appendLyricsFrom(context: Context, uri: Uri) {
        val lyrics = LyricsLoader.parseFromUri(context, uri)
        if (lyrics != null) append(lyrics)
    }

    private var _requireLyricsFollowing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val requireLyricsFollowing get() = _requireLyricsFollowing.asStateFlow()

    fun updateRequireLyricsFollowing(newState: Boolean) {
        _requireLyricsFollowing.update { newState }
    }
}