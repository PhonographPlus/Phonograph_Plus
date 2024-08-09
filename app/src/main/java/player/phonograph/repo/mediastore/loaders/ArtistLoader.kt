/*
 *  Copyright (c) 2022~2023 chr_56 & Karim Abou Zeid (kabouzeid)
 */

package player.phonograph.repo.mediastore.loaders

import player.phonograph.model.Artist
import player.phonograph.repo.mediastore.internal.generateArtists
import player.phonograph.repo.mediastore.internal.intoSongs
import player.phonograph.repo.mediastore.internal.querySongs
import android.content.Context
import android.provider.MediaStore.Audio.AudioColumns
import kotlinx.coroutines.runBlocking

object ArtistLoader : Loader<Artist> {

    override fun all(context: Context): List<Artist> {
        val songs = querySongs(context, sortOrder = null).intoSongs()
        return if (songs.isEmpty()) return emptyList() else runBlocking { generateArtists(context, songs) }
    }

    override fun id(context: Context, id: Long): Artist {
        val songs = ArtistSongLoader.id(context, id)
        val albums = ArtistAlbumLoader.id(context, id)
        return Artist(id, songs.firstOrNull()?.artistName ?: Artist.UNKNOWN_ARTIST_DISPLAY_NAME, albums.size, songs.size)
    }

    fun searchByName(context: Context, query: String): List<Artist> {
        val songs = querySongs(context, "${AudioColumns.ARTIST} LIKE ?", arrayOf("%$query%"), null).intoSongs()
        return if (songs.isEmpty()) return emptyList() else runBlocking { generateArtists(context, songs) }
    }

}
