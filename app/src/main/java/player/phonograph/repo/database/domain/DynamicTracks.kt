/*
 *  Copyright (c) 2022~2026 chr_56
 */

package player.phonograph.repo.database.domain

import player.phonograph.model.Song
import player.phonograph.model.repo.loader.Endpoint
import player.phonograph.repo.database.loaders.RecentlyPlayedTracksLoader
import player.phonograph.repo.database.loaders.TopTracksLoader
import player.phonograph.repo.database.store.HistoryStore
import player.phonograph.repo.database.store.SongPlayCountStore
import android.content.Context

object DynamicTracks {

    object RecentTracks : Endpoint {
        suspend fun all(context: Context): List<Song> = RecentlyPlayedTracksLoader.get().tracks(context)
        fun clear(): Boolean = HistoryStore.get().clear()

        fun add(songId: Long) = HistoryStore.get().addSongId(songId)
    }

    object TopTracks : Endpoint {
        suspend fun all(context: Context): List<Song> = TopTracksLoader.get().tracks(context)
        fun clear(): Boolean = SongPlayCountStore.get().clear()

        fun bump(songId: Long) = SongPlayCountStore.get().bumpPlayCount(songId)
        fun refresh(context: Context) = SongPlayCountStore.get().reCalculateScore(context)
    }

}