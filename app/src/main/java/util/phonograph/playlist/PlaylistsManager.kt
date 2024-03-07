/*
 * Copyright (c) 2022~2023 chr_56
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package util.phonograph.playlist

import lib.activityresultcontract.ICreateFileStorageAccess
import lib.activityresultcontract.IOpenDirStorageAccess
import lib.activityresultcontract.IOpenFileStorageAccess
import player.phonograph.App
import player.phonograph.R
import player.phonograph.model.Song
import player.phonograph.model.playlist.FilePlaylist
import player.phonograph.model.playlist.Playlist
import player.phonograph.model.playlist.SmartPlaylist
import player.phonograph.repo.mediastore.loaders.PlaylistLoader
import player.phonograph.settings.Keys
import player.phonograph.settings.PLAYLIST_OPS_BEHAVIOUR_AUTO
import player.phonograph.settings.PLAYLIST_OPS_BEHAVIOUR_FORCE_LEGACY
import player.phonograph.settings.PLAYLIST_OPS_BEHAVIOUR_FORCE_SAF
import player.phonograph.settings.Setting
import player.phonograph.util.coroutineToast
import player.phonograph.util.sentPlaylistChangedLocalBoardCast
import player.phonograph.util.text.currentDate
import player.phonograph.util.text.dateTimeSuffix
import player.phonograph.util.warning
import util.phonograph.playlist.m3u.M3UWriter
import util.phonograph.playlist.mediastore.addToPlaylistViaMediastore
import util.phonograph.playlist.mediastore.createOrFindPlaylistViaMediastore
import util.phonograph.playlist.saf.appendToPlaylistViaSAF
import util.phonograph.playlist.saf.createPlaylistViaSAF
import util.phonograph.playlist.saf.createPlaylistsViaSAF
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object PlaylistsManager {

    private const val TAG = "PlaylistManager"

    /**
     * @param context must be ICreateFileStorageAccess
     */
    suspend fun createPlaylist(
        context: Context,
        name: String,
        songs: List<Song>,
    ) {
        if (shouldUseSAF(context) && context is ICreateFileStorageAccess) {
            CoroutineScope(Dispatchers.IO).launch { // independent scope to avoid scope is canceled
                createPlaylistViaSAF(context, playlistName = name, songs = songs)
            }
        } else {
            coroutineScope {
                createPlaylistLegacy(context, playlistName = name, songs = songs) // legacy ways}
            }
        }
    }

    private suspend fun createPlaylistLegacy(context: Context, playlistName: String, songs: List<Song>) {
        val id = createOrFindPlaylistViaMediastore(context, playlistName)
        if (PlaylistLoader.checkExistence(context, id)) {
            addToPlaylistViaMediastore(context, songs, id, true)
            coroutineToast(context, R.string.success)
            delay(250)
            sentPlaylistChangedLocalBoardCast()
        } else {
            warning(TAG, "Failed to save playlist (id=$id)")
            coroutineToast(context, R.string.failed)
        }
    }


    suspend fun appendPlaylist(
        context: Context,
        songs: List<Song>,
        filePlaylist: FilePlaylist,
    ) = withContext(Dispatchers.Default) {
        if (shouldUseSAF(context) && context is IOpenFileStorageAccess) {
            coroutineToast(context, R.string.direction_open_file_with_saf)
            appendToPlaylistViaSAF(context, songs = songs, filePlaylist = filePlaylist)
        } else {
            addToPlaylistViaMediastore(context, songs, filePlaylist.id, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                coroutineToast(context, R.string.failed)
        }
    }

    suspend fun appendPlaylist(
        context: Context,
        songs: List<Song>,
        playlistId: Long,
    ) = appendPlaylist(context, songs, PlaylistLoader.id(context, playlistId))

    private fun shouldUseSAF(context: Context): Boolean {
        val preference = Setting(context)[Keys.playlistFilesOperationBehaviour]
        return when (val behavior = preference.data) {
            PLAYLIST_OPS_BEHAVIOUR_FORCE_SAF    -> true
            PLAYLIST_OPS_BEHAVIOUR_FORCE_LEGACY -> false
            PLAYLIST_OPS_BEHAVIOUR_AUTO         -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            else                                -> {
                preference.data = PLAYLIST_OPS_BEHAVIOUR_AUTO // reset to default
                throw IllegalStateException("$behavior is not a valid option")
            }
        }
    }

    suspend fun duplicatePlaylistsViaSaf(
        context: Context,
        filePlaylists: List<Playlist>,
    ) = withContext(Dispatchers.IO) {
        if (context is IOpenDirStorageAccess) {
            createPlaylistsViaSAF(context, filePlaylists, defaultDirectory.absolutePath)
        } else {
            legacySavePlaylists(context, filePlaylists) // legacy ways
        }
    }

    suspend fun duplicatePlaylistViaSaf(
        context: Context,
        playlist: Playlist,
    ) = createPlaylist(context, playlist.name + dateTimeSuffix(currentDate()), playlist.getSongs(context))

    private suspend fun legacySavePlaylists(context: Context, filePlaylists: List<Playlist>) {
        var successes = 0
        var failures = 0
        var dir: String? = ""
        val failureList = StringBuffer()
        for (playlist in filePlaylists) {
            try {
                val filename: String =
                    if (playlist is SmartPlaylist) {
                        // Since AbsCustomPlaylists are dynamic, we add a timestamp after their names.
                        playlist.name + dateTimeSuffix(currentDate())
                    } else {
                        playlist.name
                    }
                val songs = playlist.getSongs(context)
                dir = M3UWriter.write(File(Environment.DIRECTORY_DOWNLOADS), songs, filename).parent
                successes++
            } catch (e: IOException) {
                failures++
                failureList.append(playlist.name).append(" ")
                Log.w(TAG, e.message ?: "")
            }
        }
        val msg =
            if (failures == 0) String.format(
                App.instance.applicationContext.getString(R.string.saved_x_playlists_to_x),
                successes, dir
            ) else String.format(
                App.instance.applicationContext.getString(R.string.saved_x_playlists_to_x_failed_to_save_x),
                successes, dir, failureList
            )
        coroutineToast(context, msg)
    }

    private val defaultDirectory: File get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
}
