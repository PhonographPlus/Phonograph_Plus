/*
 * Copyright (c) 2022~2023 chr_56
 */

package util.phonograph.playlist.saf

import legacy.phonograph.MediaStoreCompat.Audio.Playlists.CONTENT_TYPE
import legacy.phonograph.MediaStoreCompat.Audio.Playlists.ENTRY_CONTENT_TYPE
import lib.activityresultcontract.ActivityResultContractUtil.chooseFileViaSAF
import lib.activityresultcontract.IOpenFileStorageAccess
import lib.storage.getAbsolutePath
import player.phonograph.R
import player.phonograph.model.Song
import player.phonograph.model.playlist.FilePlaylist
import player.phonograph.util.coroutineToast
import player.phonograph.util.reportError
import util.phonograph.playlist.m3u.M3UWriter
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.IOException

/**
 * @param context must be [IOpenFileStorageAccess]
 */
suspend fun appendToPlaylistViaSAF(
    context: Context,
    songs: List<Song>,
    filePlaylist: FilePlaylist,
) = withContext(Dispatchers.IO) {
    // check
    if (songs.isEmpty()) return@withContext
    require(context is IOpenFileStorageAccess)
    require(filePlaylist.id > 0 || filePlaylist.associatedFilePath.contains('/'))
    while (context.openFileStorageAccessTool.busy) yield()
    // config
    val playlistPath = filePlaylist.associatedFilePath
    val mimeTypes = arrayOf(PLAYLIST_MIME_TYPE, CONTENT_TYPE, ENTRY_CONTENT_TYPE)
    // launch
    val uri = chooseFileViaSAF(context, playlistPath, mimeTypes)
    // check
    if (!checkUri(context, filePlaylist, uri)) {
        val returningPath = uri.getAbsolutePath(context)
        val message = buildString {
            append(context.getString(R.string.failed_to_save_playlist, filePlaylist.name)).append('\n')
            append(context.getString(R.string.file_incorrect)).append('\n')
            append("Playlist($playlistPath) -> File($returningPath) ")
        }
        reportError(IllegalStateException(message), TAG, context.getString(R.string.failed))
        return@withContext
    }
    // write
    try {
        openOutputStreamSafe(context, uri, "wa")?.use { outputStream ->
            M3UWriter.write(outputStream, songs, false)
            coroutineToast(context, context.getString(R.string.success))
        }
    } catch (e: IOException) {
        coroutineToast(
            context,
            context.getString(
                R.string.failed_to_save_playlist,
                filePlaylist.name
            ) + ": Unknown!"
        )
    }
}

private fun checkUri(context: Context, target: FilePlaylist, uri: Uri): Boolean =
    uri.getAbsolutePath(context) == target.associatedFilePath

private const val TAG = "PlaylistAppend"