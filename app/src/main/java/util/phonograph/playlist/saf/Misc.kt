/*
 * Copyright (c) 2022~2023 chr_56
 */

package util.phonograph.playlist.saf

import lib.activityresultcontract.ActivityResultContractUtil
import lib.activityresultcontract.IOpenDirStorageAccess
import lib.storage.getAbsolutePath
import player.phonograph.R
import player.phonograph.model.playlist.FilePlaylist
import player.phonograph.util.coroutineToast
import player.phonograph.util.reportError
import player.phonograph.util.warning
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.yield
import java.io.FileNotFoundException
import java.io.OutputStream

@SuppressLint("Recycle")
internal fun openOutputStreamSafe(context: Context, uri: Uri, mode: String): OutputStream? =
    try {
        val outputStream = context.contentResolver.openOutputStream(uri, mode)
        if (outputStream == null) warning(TAG, "Failed!")
        outputStream
    } catch (e: FileNotFoundException) {
        reportError(e, TAG, "Not found $uri")
        null
    }


internal fun checkUri(context: Context, target: FilePlaylist, uri: Uri): Boolean =
    uri.getAbsolutePath(context) == target.associatedFilePath


/**
 * open SAF at the common directory of [paths]
 * @param context must be [IOpenDirStorageAccess]
 * @return DocumentUri
 */
internal suspend fun chooseCommonDirViaSAF(context: Context, paths: List<String>): Uri? {
    // check
    if (paths.isEmpty()) return null
    require(context is IOpenDirStorageAccess)
    while (context.openDirStorageAccessTool.busy) yield()
    // common root
    val commonRoot = commonPathRoot(paths)
    coroutineToast(
        context,
        context.getString(R.string.direction_open_folder_with_saf),
        true
    )
    // launch
    val treeUri =
        ActivityResultContractUtil.chooseDirViaSAF(
            context,
            commonRoot.ifEmpty { Environment.getExternalStorageDirectory().absolutePath }
        )
    Log.v(TAG,"treeUri: $treeUri")
    return treeUri
}

/**
 * common path root of a list of paths
 * @param paths list of path separating by '/'
 * @return common root path of these [paths], **empty** if no common
 */
internal fun commonPathRoot(paths: Collection<String>): String {

    val fragments = paths.map { path -> path.split('/').filter { it.isNotEmpty() } }
    val result = mutableListOf<String>()

    var index = 0
    while (true) {
        val col = fragments.mapNotNull { it.getOrNull(index) }.toSet()
        if (col.size == 1) {
            result.add(col.first())
            index++
        } else {// size > 1 or size == 0
            break
        }
    }

    return result.fold("") { acc, s -> "$acc/$s" }
}

private const val TAG = "Playlist"

internal const val PLAYLIST_MIME_TYPE = "audio/x-mpegurl"