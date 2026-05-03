/*
 *  Copyright (c) 2022~2026 chr_56
 */

package player.phonograph.model.repo.loader

import player.phonograph.model.Song
import android.content.Context

interface ITopTracks : Endpoint {
    suspend fun all(context: Context): List<Song>
    fun clear(): Boolean

    /**
     * bump statistics count for track ([songId])
     */
    fun bump(songId: Long)

    /**
     * calculate statistics now
     */
    fun refresh(context: Context)
}