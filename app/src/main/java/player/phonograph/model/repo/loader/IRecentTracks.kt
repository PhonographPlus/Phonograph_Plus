/*
 *  Copyright (c) 2022~2026 chr_56
 */

package player.phonograph.model.repo.loader

import player.phonograph.model.Song
import android.content.Context

interface IRecentTracks : Endpoint {
    suspend fun all(context: Context): List<Song>
    fun clear(): Boolean

    /**
     * Add a new entry of history track ([songId])
     */
    fun add(songId: Long)
}