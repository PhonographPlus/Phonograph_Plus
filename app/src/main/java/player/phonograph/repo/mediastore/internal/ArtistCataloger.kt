/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.repo.mediastore.internal

import player.phonograph.App
import player.phonograph.model.Artist
import player.phonograph.model.Song
import player.phonograph.model.sort.SortMode
import player.phonograph.model.sort.SortRef
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import player.phonograph.util.reportError
import player.phonograph.util.sort
import android.content.Context
import android.util.ArrayMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.yield

suspend fun generateArtists(context: Context, songs: List<Song>): List<Artist> =
    catalogArtists(songs, Setting(context).Composites[Keys.artistSortMode].flowData()).await()

private suspend fun catalogArtists(songs: List<Song>, sortMode: SortMode): Deferred<List<Artist>> = coroutineScope {
    async {

        var completed = false

        val flow = flow {
            for (song in songs) emit(song)
        }.catch { e ->
            reportError(e, TAG_ARTIST, "Fail to load artists")
        }

        // artistID <-> List of songs which are grouped by album
        val table: MutableMap<Long, MutableMap<Long, MutableList<Song>>> = ArrayMap()

        // artistID <-> artistName
        val artistNames: MutableMap<Long, String?> = ArrayMap()
        // albumID <-> albumName
        val albumNames: MutableMap<Long, String?> = ArrayMap()

        flow.onCompletion { completed = true }
            .collect { song -> // check artist
                if (table[song.artistId] == null) {
                    // create new artist
                    artistNames[song.artistId] = song.artistName
                    table[song.artistId] = ArrayMap()
                    // check album
                    if (table[song.artistId]!![song.albumId] == null) {
                        // create new album
                        albumNames[song.albumId] = song.albumName
                        table[song.artistId]!![song.albumId] = mutableListOf(song)
                    } else {
                        // add to existed album
                        table[song.artistId]!![song.albumId]!!.add(song)
                    }
                    //
                } else {
                    // add to existed artist
                    // (no ops)
                    // check album
                    if (table[song.artistId]!![song.albumId] == null) {
                        // create new album
                        albumNames[song.albumId] = song.albumName
                        table[song.artistId]!![song.albumId] = mutableListOf(song)
                    } else {
                        // add to existed album
                        table[song.artistId]!![song.albumId]!!.add(song)
                    }
                    //
                }
            }

        while (!completed) yield() // wait until result is ready

        // handle result
        return@async flow {
            for ((id, map) in table) {
                emit(Pair(id, map))
            }
        }.flowOn(Dispatchers.Default).map { (artistId, map) ->
            val albumList = flow {
                for ((id, list) in map) {
                    emit(Pair(id, list))
                }
            }.map { (id, list) ->
                createAlbum(id, list)
            }.catch { e ->
                reportError(e, TAG_ARTIST, "Fail to load artists")
            }.toList()

            Artist(
                id = artistId,
                name = artistNames[artistId] ?: Artist.UNKNOWN_ARTIST_DISPLAY_NAME,
                albumCount = albumList.size,
                songCount = albumList.fold(0) { acc, album -> acc + album.songCount }
            )
        }.catch { e ->
            reportError(e, TAG_ARTIST, "Fail to load artists")
        }.toList().sortAllArtist()
    }
}

internal fun List<Artist>.sortAllArtist(): List<Artist> {
    val sortMode = Setting(App.instance).Composites[Keys.artistSortMode].data
    val revert = sortMode.revert
    return when (sortMode.sortRef) {
        SortRef.ARTIST_NAME -> this.sort(revert) { it.name.lowercase() }
        SortRef.ALBUM_COUNT -> this.sort(revert) { it.albumCount }
        SortRef.SONG_COUNT  -> this.sort(revert) { it.songCount }
        else                -> this
    }
}

private const val TAG_ARTIST = "ArtistCataloger"