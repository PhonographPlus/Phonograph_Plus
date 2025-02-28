/*
 *  Copyright (c) 2022~2024 chr_56
 */

package player.phonograph.mechanism

import lib.storage.documentProviderUriAbsolutePath
import player.phonograph.model.Song
import player.phonograph.repo.loader.Songs
import player.phonograph.util.asList
import player.phonograph.util.reportError
import androidx.core.provider.DocumentsContractCompat
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import java.io.File

interface IUriParser<O> {
    fun check(scheme: String?, authority: String?): Boolean
    suspend fun parse(context: Context, uri: Uri): Collection<O>
}

val SongUriParsers: List<IUriParser<Song>> = listOf(
    ApplicationContentUriParser,
    DocumentsProviderUriParser,
    MediaProviderUriParser,
    MediaUriParser,
)

private object MediaProviderUriParser : IUriParser<Song> {
    override fun check(scheme: String?, authority: String?): Boolean =
        scheme != null && scheme == ContentResolver.SCHEME_CONTENT
                && authority != null && authority == AUTHORITY_MEDIA_PROVIDER

    override suspend fun parse(context: Context, uri: Uri): Collection<Song> {
        val songId = DocumentsContractCompat.getDocumentId(uri)!!.split(":")[1].toLongOrNull()
        return if (songId != null) Songs.id(context, songId).asList() else emptyList()
    }
}

private object MediaUriParser : IUriParser<Song> {
    override fun check(scheme: String?, authority: String?): Boolean =
        scheme != null && scheme == ContentResolver.SCHEME_CONTENT
                && authority != null && authority == AUTHORITY_MEDIA

    override suspend fun parse(context: Context, uri: Uri): Collection<Song> {
        val songId = uri.lastPathSegment?.toLongOrNull()
        return if (songId != null) Songs.id(context, songId).asList() else emptyList()
    }
}


private object DocumentsProviderUriParser : IUriParser<Song> {
    override fun check(scheme: String?, authority: String?): Boolean =
        scheme != null && scheme == ContentResolver.SCHEME_CONTENT
                && authority != null && authority == AUTHORITY_DOCUMENTS_PROVIDER

    override suspend fun parse(context: Context, uri: Uri): Collection<Song> {
        val file = File(documentProviderUriAbsolutePath(uri, context) ?: return emptyList())
        return Songs.searchByPath(context, file.absolutePath, withoutPathFilter = true)
    }
}

private object ApplicationContentUriParser : IUriParser<Song> {
    override fun check(scheme: String?, authority: String?): Boolean =
        scheme != null && scheme == ContentResolver.SCHEME_CONTENT && authority != null

    override suspend fun parse(context: Context, uri: Uri): Collection<Song> {
        val filePath = queryFilePath(context, uri) ?: return emptyList()
        return Songs.searchByPath(context, File(filePath).absolutePath, withoutPathFilter = true)
    }
}

private const val AUTHORITY_MEDIA = "media"
private const val AUTHORITY_MEDIA_PROVIDER = "com.android.providers.media.documents"
private const val AUTHORITY_DOCUMENTS_PROVIDER = "com.android.externalstorage.documents"

private fun queryFilePath(context: Context, uri: Uri): String? {
    val column = "_data"
    val projection = arrayOf(column)

    try {
        val cursor: Cursor? =
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(column)
                return it.getString(columnIndex)
            }
        }
    } catch (e: SecurityException) {
        reportError(e, TAG, "Permission issue, can not locate $uri, please check storage access permissions")
    } catch (e: Exception) {
        reportError(e, TAG, "Failed parse $uri")
    }
    return null
}

private const val TAG = "UriParse"