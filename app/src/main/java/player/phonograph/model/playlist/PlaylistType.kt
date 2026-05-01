/*
 *  Copyright (c) 2022~2024 chr_56
 */

package player.phonograph.model.playlist

import androidx.annotation.IntDef

const val PLAYLIST_TYPE_FILE = 1

const val PLAYLIST_TYPE_FAVORITE = 2
const val PLAYLIST_TYPE_LAST_ADDED = 4
const val PLAYLIST_TYPE_HISTORY = 8
const val PLAYLIST_TYPE_MY_TOP_TRACK = 16
const val PLAYLIST_TYPE_RANDOM = 32

@IntDef(
    value = [PLAYLIST_TYPE_FILE,
        PLAYLIST_TYPE_FAVORITE,
        PLAYLIST_TYPE_LAST_ADDED,
        PLAYLIST_TYPE_HISTORY,
        PLAYLIST_TYPE_MY_TOP_TRACK,
        PLAYLIST_TYPE_RANDOM],
)
@Retention(AnnotationRetention.SOURCE)
annotation class PlaylistType