/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.actions

import player.phonograph.mechanism.PlaylistEdit
import player.phonograph.model.playlist.FilePlaylist
import player.phonograph.model.playlist.Playlist
import player.phonograph.service.MusicPlayerRemote
import player.phonograph.service.queue.ShuffleMode
import player.phonograph.ui.dialogs.AddToPlaylistDialog
import player.phonograph.ui.dialogs.ClearPlaylistDialog
import player.phonograph.ui.dialogs.RenamePlaylistDialog
import androidx.fragment.app.FragmentActivity
import android.content.Context
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Playlist.actionPlay(context: Context): Boolean =
    getSongs(context).let { songs ->
        if (songs.isNotEmpty())
            songs.actionPlay(ShuffleMode.NONE, 0)
        else
            false
    }

fun Playlist.actionShuffleAndPlay(context: Context) =
    getSongs(context).let { songs ->
        if (songs.isNotEmpty())
            songs.actionPlay(ShuffleMode.SHUFFLE, Random.nextInt(songs.size))
        else
            false
    }

fun Playlist.actionPlayNext(context: Context): Boolean =
    MusicPlayerRemote.playNext(ArrayList(getSongs(context)))

fun Playlist.actionAddToCurrentQueue(context: Context): Boolean =
    MusicPlayerRemote.enqueue(ArrayList(getSongs(context)))

fun Playlist.actionAddToPlaylist(activity: FragmentActivity) {
    AddToPlaylistDialog.create(getSongs(activity))
        .show(activity.supportFragmentManager, "ADD_PLAYLIST")
}

fun FilePlaylist.actionRenamePlaylist(activity: FragmentActivity) {
    RenamePlaylistDialog.create(this)
        .show(activity.supportFragmentManager, "RENAME_PLAYLIST")
}

fun Playlist.actionDeletePlaylist(activity: FragmentActivity) {
    ClearPlaylistDialog.create(listOf(this))
        .show(activity.supportFragmentManager, "CLEAR_PLAYLIST")
}

fun List<Playlist>.actionDeletePlaylists(activity: Context): Boolean =
    fragmentActivity(activity) {
        ClearPlaylistDialog.create(this)
            .show(it.supportFragmentManager, "CLEAR_PLAYLIST")
        true
    }

fun Playlist.actionSavePlaylist(activity: FragmentActivity) {
    CoroutineScope(Dispatchers.Default).launch {
        PlaylistEdit.duplicate(activity, this@actionSavePlaylist)
    }
}

fun List<Playlist>.actionSavePlaylists(activity: Context) {
    CoroutineScope(Dispatchers.Default).launch {
        PlaylistEdit.duplicate(activity, this@actionSavePlaylists)
    }
}