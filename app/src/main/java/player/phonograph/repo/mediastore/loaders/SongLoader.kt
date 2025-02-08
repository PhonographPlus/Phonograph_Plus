/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.repo.mediastore.loaders

import player.phonograph.model.Song
import player.phonograph.model.file.FileEntity
import player.phonograph.repo.mediastore.internal.intoFirstSong
import player.phonograph.repo.mediastore.internal.intoSongs
import player.phonograph.repo.mediastore.internal.querySongs
import android.content.Context
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.DATE_ADDED
import android.provider.MediaStore.MediaColumns.DATE_MODIFIED

object SongLoader : Loader<Song> {

    override suspend fun all(context: Context): List<Song> =
        querySongs(context).intoSongs()

    override suspend fun id(context: Context, id: Long): Song =
        querySongs(context, "${MediaStore.Audio.AudioColumns._ID} =? ", arrayOf(id.toString())).intoFirstSong()

    fun path(context: Context, path: String): Song =
        querySongs(context, "${MediaStore.Audio.AudioColumns.DATA} =? ", arrayOf(path)).intoFirstSong()

    /**
     * @param withoutPathFilter true if disable path filter
     */
    fun searchByPath(context: Context, path: String, withoutPathFilter: Boolean): List<Song> =
        querySongs(
            context,
            "${MediaStore.Audio.AudioColumns.DATA} LIKE ? ",
            arrayOf(path),
            withoutPathFilter = withoutPathFilter
        ).intoSongs()

    fun searchByTitle(context: Context, title: String): List<Song> {
        val cursor =
            querySongs(context, "${MediaStore.Audio.AudioColumns.TITLE} LIKE ?", arrayOf("%$title%"))
        return cursor.intoSongs()
    }

    suspend fun searchByFileEntity(context: Context, file: FileEntity.File): List<Song> {
        return if (file.id > 0) listOf(id(context, file.id))
        else searchByPath(context, file.location.sqlPattern, true)
    }

    fun since(context: Context, timestamp: Long, useModifiedDate: Boolean = false): List<Song> {
        val dateRef = if (useModifiedDate) DATE_MODIFIED else DATE_ADDED
        val cursor =
            querySongs(
                context = context,
                selection = "$dateRef > ?",
                selectionValues = arrayOf(timestamp.toString()),
                sortOrder = "$dateRef DESC"
            )
        return cursor.intoSongs()
    }

    suspend fun latest(context: Context): Song? {
        return all(context).maxByOrNull { it.dateModified }
    }
}
