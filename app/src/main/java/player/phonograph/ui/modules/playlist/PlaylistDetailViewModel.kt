/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.ui.modules.playlist

import player.phonograph.mechanism.playlist.PlaylistProcessors
import player.phonograph.mechanism.playlist.PlaylistReader
import player.phonograph.mechanism.playlist.PlaylistWriter
import player.phonograph.model.Song
import player.phonograph.model.UIMode
import player.phonograph.model.playlist.Playlist
import androidx.lifecycle.ViewModel
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@Suppress("LocalVariableName")
class PlaylistDetailViewModel(_playlist: Playlist, _songs: List<Song>) : ViewModel() {

    val playlist: Playlist = _playlist
    private val reader: PlaylistReader = PlaylistProcessors.reader(playlist)
    private val writer: PlaylistWriter? = PlaylistProcessors.writer(playlist)

    private val _currentMode: MutableStateFlow<UIMode> = MutableStateFlow(UIMode.Common)
    val currentMode get() = _currentMode.asStateFlow()

    private val _songs: MutableStateFlow<List<Song>> = MutableStateFlow(_songs)
    val songs get() = _songs.asStateFlow()

    private val _searchResults: MutableStateFlow<List<Song>> = MutableStateFlow(emptyList())
    val searchResults get() = _searchResults.asStateFlow()

    suspend fun execute(context: Context, action: PlaylistAction): Boolean = when (action) {
        is Fetch      -> fetch(context)
        is Refresh    -> refresh(context, action.fetch)
        is Search     -> search(action.keyword)
        is UpdateMode -> updateMode(action.mode)
        is EditAction -> edit(context, action)
    }

    private suspend fun refresh(context: Context, fetch: Boolean): Boolean = withContext(Dispatchers.IO) {
        reader.refresh(context)
        if (fetch) fetch(context)
        true
    }

    private suspend fun fetch(context: Context): Boolean = withContext(Dispatchers.IO) {
        _songs.emit(
            reader.allSongs(context)
        )
        true
    }

    private suspend fun search(keyword: String): Boolean = withContext(Dispatchers.IO) {
        _searchResults.emit(_songs.value.filter { it.title.contains(keyword) })
        true
    }

    private fun updateMode(newMode: UIMode): Boolean {
        _currentMode.value = newMode
        return true
    }

    private suspend fun edit(context: Context, action: EditAction): Boolean {
        return when (action) {
            is EditAction.Delete -> deleteItem(context, action.song, action.position)
            is EditAction.Move   -> moveItem(context, action.from, action.to)
        }
    }

    suspend fun deleteItem(context: Context, song: Song, position: Int): Boolean =
        writer?.removeSong(context, song, position.toLong()) ?: false

    suspend fun moveItem(context: Context, fromPosition: Int, toPosition: Int): Boolean =
        writer?.moveSong(context, fromPosition, toPosition) ?: false


}