/*
 * Copyright (c) 2022~2023 chr_56
 */

package util.phonograph.playlist.saf

import lib.activityresultcontract.ActivityResultContractUtil.chooseDirViaSAF
import lib.activityresultcontract.ActivityResultContractUtil.createFileViaSAF
import lib.activityresultcontract.IOpenFileStorageAccess
import player.phonograph.R
import player.phonograph.model.Song
import player.phonograph.model.playlist.Playlist
import player.phonograph.util.coroutineToast
import player.phonograph.util.reportError
import player.phonograph.util.sentPlaylistChangedLocalBoardCast
import player.phonograph.util.text.currentDate
import player.phonograph.util.text.dateTimeSuffix
import player.phonograph.util.warning
import util.phonograph.playlist.m3u.M3UWriter
import androidx.documentfile.provider.DocumentFile
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.IOException

/**
 * @param context must be [IOpenFileStorageAccess]
 */
suspend fun createPlaylistViaSAF(
    context: Context,
    playlistName: String,
    songs: List<Song>,
): Unit = withContext(Dispatchers.IO) {
    // check
    require(context is IOpenFileStorageAccess)
    while (context.openFileStorageAccessTool.busy) yield()
    // launch
    val uri = createFileViaSAF(context, "$playlistName.m3u")
    openOutputStreamSafe(context, uri, "rwt")?.use { stream ->
        try {
            M3UWriter.write(stream, songs, true)
            coroutineToast(context, R.string.success)
            delay(250)
            sentPlaylistChangedLocalBoardCast()
        } catch (e: Exception) {
            reportError(e, TAG, "Failed to write $uri")
            coroutineToast(context, R.string.failed)
        }
    }
}


/**
 * @param context must be [IOpenFileStorageAccess]
 */
suspend fun createPlaylistsViaSAF(
    context: Context,
    playlists: List<Playlist>,
    initialPosition: String,
) = withContext(Dispatchers.IO) {
    // check
    if (playlists.isEmpty()) return@withContext
    require(context is IOpenFileStorageAccess)
    while (context.openFileStorageAccessTool.busy) yield()
    // launch
    coroutineToast(
        context,
        context.getString(R.string.direction_open_folder_with_saf),
        true
    )
    val treeUri = chooseDirViaSAF(context, initialPosition)
    val dir = DocumentFile.fromTreeUri(context, treeUri)
    if (dir != null && dir.isDirectory) {
        for (playlist in playlists) {
            val file = dir.createFile(PLAYLIST_MIME_TYPE, playlist.name + dateTimeSuffix(currentDate()))
            if (file != null) {
                openOutputStreamSafe(context, file.uri, "rwt")?.use { outputStream ->
                    val songs: List<Song> = playlist.getSongs(context)
                    try {
                        M3UWriter.write(outputStream, songs, true)
                    } catch (e: IOException) {
                        reportError(e, TAG, "")
                        coroutineToast(context, R.string.failed)
                    }
                }
            } else {
                warning(
                    TAG, context.getString(
                        R.string.failed_to_save_playlist,
                        playlist.name
                    )
                )
            }
        }
        coroutineToast(context, R.string.success)
    } else {
        warning(
            TAG, "${context.getString(R.string.failed)}: uri $treeUri"
        )
    }
}


private const val TAG = "PlaylistCreate"
