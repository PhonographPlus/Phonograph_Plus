/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.repo.database

import org.koin.core.context.GlobalContext
import player.phonograph.mechanism.event.MediaStoreTracker
import player.phonograph.model.Song
import player.phonograph.model.playlist2.FilePlaylistLocation
import player.phonograph.model.playlist2.Playlist
import player.phonograph.repo.database.DatabaseConstants.FAVORITE_DB
import player.phonograph.repo.loader.Songs
import player.phonograph.repo.mediastore.loaders.PlaylistLoader2
import player.phonograph.util.text.currentTimestamp
import player.phonograph.util.warning
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoritesStore constructor(context: Context) :
        SQLiteOpenHelper(context, FAVORITE_DB, null, VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(creatingSongsTableSQL)
        db.execSQL(creatingPlaylistsTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL(creatingPlaylistsTableSQL)
        } else {
            warning(FAVORITE_DB, "Can not upgrade database `favorite.db` from $oldVersion to $newVersion ")
            // db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SONGS")
            // db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_PLAYLISTS")
            onCreate(db)
        }
    }

    fun clearAll() {
        clearAllSongs()
        clearAllPlaylists()
    }

    fun clearAllSongs() = clearTable(TABLE_NAME_SONGS)
    fun clearAllPlaylists() = clearTable(TABLE_NAME_PLAYLISTS)

    private fun clearTable(tableName: String) {
        val database = writableDatabase
        database.delete(tableName, null, null)
        mediaStoreTracker.notifyAllListeners()
    }

    suspend fun getAllSongs(context: Context): List<Song> = getAllSongsImpl(context)

    suspend fun getAllPlaylists(context: Context): List<Playlist> = getAllPlaylistsImpl(context)


    private suspend fun getAllSongsImpl(context: Context): List<Song> {
        return parseCursorImpl(TABLE_NAME_SONGS) { cursor ->
            Songs.path(context, cursor.getString(1))
        }
    }

    private suspend fun getAllPlaylistsImpl(context: Context): List<Playlist> {
        return parseCursorImpl(TABLE_NAME_PLAYLISTS) { cursor ->
            PlaylistLoader2.searchByPath(context, cursor.getString(1))
        }
    }

    private suspend fun <T> parseCursorImpl(tableName: String, operation: suspend (Cursor) -> T?): List<T> {
        return query(tableName).use { cursor ->
            val notEmpty = cursor.moveToFirst()
            if (notEmpty) {
                val result = mutableListOf<T>()
                do {
                    val item = operation(cursor)
                    if (item != null) result.add(item)
                } while (cursor.moveToNext())
                result
            } else {
                emptyList()
            }
        }
    }

    private fun query(tableName: String): Cursor {
        val database = readableDatabase
        return database.query(
            tableName,
            arrayOf(COLUMNS_ID, COLUMNS_PATH, COLUMNS_TITLE, COLUMNS_TIMESTAMP),
            null, null, null, null, "$COLUMNS_TIMESTAMP DESC"
        )
    }



    fun containsSong(songId: Long?, path: String?): Boolean =
        containsImpl(TABLE_NAME_SONGS, songId, path)

    fun containsPlaylist(playlist: Playlist): Boolean =
        if (!playlist.isVirtual())
            containsImpl(TABLE_NAME_PLAYLISTS, playlist.id, (playlist.location as FilePlaylistLocation).path)
        else false

    fun containsPlaylist(playlistId: Long?, path: String?): Boolean =
        containsImpl(TABLE_NAME_PLAYLISTS, playlistId, path)

    private fun containsImpl(table: String, id: Long?, path: String?): Boolean {
        val database = readableDatabase
        val cursor = database.query(
            table,
            arrayOf(COLUMNS_ID, COLUMNS_PATH, COLUMNS_TITLE, COLUMNS_TIMESTAMP),
            "$COLUMNS_ID =? OR $COLUMNS_PATH =?",
            arrayOf(id?.toString() ?: "0", path ?: ""),
            null, null, null,
        )
        return cursor.use { it.moveToFirst() }
    }

    fun addSong(song: Song): Boolean =
        addImpl(TABLE_NAME_SONGS, song.id, song.data, song.title)

    fun addPlaylist(playlist: Playlist): Boolean =
        addImpl(TABLE_NAME_PLAYLISTS, playlist.id, (playlist.location as FilePlaylistLocation).path, playlist.name)

    private fun addImpl(tableName: String, id: Long, path: String, name: String?): Boolean {
        val database = writableDatabase
        database.beginTransaction()
        return try {
            val values = ContentValues(4)
                .apply {
                    put(COLUMNS_ID, id)
                    put(COLUMNS_PATH, path)
                    put(COLUMNS_TITLE, name)
                    put(COLUMNS_TIMESTAMP, currentTimestamp())
                }
            database.insert(tableName, null, values)
            database.setTransactionSuccessful()
            true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        } finally {
            database.endTransaction()
            mediaStoreTracker.notifyAllListeners()
        }
    }

    fun addSongs(songs: Collection<Song>): Boolean {
        val data = songs.map {
            ContentValues(4).apply {
                put(COLUMNS_ID, it.id)
                put(COLUMNS_PATH, it.data)
                put(COLUMNS_TITLE, it.title)
                put(COLUMNS_TIMESTAMP, currentTimestamp())
            }
        }
        return addMultipleImpl(TABLE_NAME_SONGS, data)
    }

    fun addPlaylists(playlists: Collection<Playlist>): Boolean {
        val data = playlists.map {
            ContentValues(4).apply {
                put(COLUMNS_ID, it.id)
                put(COLUMNS_PATH, (it.location as FilePlaylistLocation).path)
                put(COLUMNS_TITLE, it.name)
                put(COLUMNS_TIMESTAMP, currentTimestamp())
            }
        }
        return addMultipleImpl(TABLE_NAME_PLAYLISTS, data)
    }

    private fun addMultipleImpl(tableName: String, lines: List<ContentValues>): Boolean {
        val database = writableDatabase
        database.beginTransaction()
        return try {
            for (line in lines) {
                database.insert(tableName, null, line)
            }
            database.setTransactionSuccessful()
            true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        } finally {
            database.endTransaction()
            mediaStoreTracker.notifyAllListeners()
        }
    }

    fun removeSong(song: Song): Boolean =
        removeImpl(TABLE_NAME_SONGS, song.id, song.data)

    fun removePlaylist(playlist: Playlist): Boolean =
        removeImpl(TABLE_NAME_PLAYLISTS, playlist.id, (playlist.location as FilePlaylistLocation).path)

    private fun removeImpl(table: String, id: Long, path: String): Boolean {
        val database = writableDatabase
        database.beginTransaction()
        return try {
            val result = database.delete(
                table,
                "$COLUMNS_ID =? AND $COLUMNS_PATH =?",
                arrayOf(id.toString(), path)
            )
            database.setTransactionSuccessful()
            result > 0
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        } finally {
            database.endTransaction()
            mediaStoreTracker.notifyAllListeners()
        }
    }

    private val mediaStoreTracker: MediaStoreTracker by GlobalContext.get().inject()

    companion object {
        private const val VERSION = 2

        private const val TABLE_NAME_SONGS = "songs"
        private const val TABLE_NAME_PLAYLISTS = "playlists"

        const val COLUMNS_ID = "id" // long
        const val COLUMNS_PATH = "path" // string
        const val COLUMNS_TITLE = "title" // string
        const val COLUMNS_TIMESTAMP = "timestamp" // long


        private const val creatingSongsTableSQL =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME_SONGS (" +
                    "$COLUMNS_ID LONG NOT NULL PRIMARY KEY," +
                    " $COLUMNS_PATH TEXT NOT NULL," +
                    " $COLUMNS_TITLE TEXT," +
                    " $COLUMNS_TIMESTAMP LONG);"


        private const val creatingPlaylistsTableSQL =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME_PLAYLISTS (" +
                    "$COLUMNS_ID LONG NOT NULL PRIMARY KEY," +
                    " $COLUMNS_PATH TEXT NOT NULL," +
                    " $COLUMNS_TITLE TEXT," +
                    " $COLUMNS_TIMESTAMP LONG);"


        fun get() = GlobalContext.get().get<FavoritesStore>()
    }
}
