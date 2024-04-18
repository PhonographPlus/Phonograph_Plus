/*
 * Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.mechanism.playlist.mediastore

import legacy.phonograph.MediaStoreCompat
import player.phonograph.R
import player.phonograph.model.playlist.FilePlaylist
import player.phonograph.util.coroutineToast
import player.phonograph.util.sentPlaylistChangedLocalBoardCast
import player.phonograph.util.warning
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * delete playlist by path via MediaStore
 * @return playlists failing to delete
 */
suspend fun deletePlaylistsViaMediastore(
    context: Context,
    filePlaylists: List<FilePlaylist>,
): List<FilePlaylist> = withContext(Dispatchers.IO) {
    var result = 0
    val failList = mutableListOf<FilePlaylist>()
    // try to delete
    for (index in filePlaylists.indices) {
        val output = context.contentResolver.delete(
            MediaStoreCompat.Audio.Playlists.EXTERNAL_CONTENT_URI,
            "${MediaStore.Audio.Media._ID} = ?",
            arrayOf(filePlaylists[index].id.toString())
        )
        if (output == 0) {
            Log.w(
                "DeletePlaylist",
                "fail to delete playlist ${filePlaylists[index].name}(id:${filePlaylists[index].id})"
            )
            failList.add(filePlaylists[index])
        }
        result += output
    }
    coroutineToast(
        context,
        context.resources.getQuantityString(R.plurals.msg_deletion_result, result, result, filePlaylists.size),
    )
    if (failList.isNotEmpty())
        warning("DeletePlaylist", failList.fold("Failed to delete:") { acc, s -> "$acc, $s" })
    sentPlaylistChangedLocalBoardCast()
    failList
}