/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.model

@Suppress("MemberVisibilityCanBePrivate")
object SongClickMode {

    const val SONG_PLAY_NEXT = 101
    const val SONG_PLAY_NOW = 102
    const val SONG_APPEND_QUEUE = 103
    const val SONG_SINGLE_PLAY = 110

    const val QUEUE_PLAY_NEXT = 201
    const val QUEUE_PLAY_NOW = 202
    const val QUEUE_APPEND_QUEUE = 203
    const val QUEUE_SWITCH_TO_BEGINNING = 211
    const val QUEUE_SWITCH_TO_POSITION = 213
    const val QUEUE_SHUFFLE = 219

    const val FLAG_MASK_GOTO_POSITION_FIRST = 1 shl 3
    const val FLAG_MASK_PLAY_QUEUE_IF_EMPTY = 1 shl 4

    val allModes by lazy {
        intArrayOf(
            SONG_PLAY_NEXT,
            SONG_PLAY_NOW,
            SONG_APPEND_QUEUE,
            SONG_SINGLE_PLAY,
            QUEUE_PLAY_NEXT,
            QUEUE_PLAY_NOW,
            QUEUE_APPEND_QUEUE,
            QUEUE_SWITCH_TO_BEGINNING,
            QUEUE_SWITCH_TO_POSITION,
            QUEUE_SHUFFLE,
        )
    }

    val singleItemModes by lazy {
        intArrayOf(
            SONG_PLAY_NEXT,
            SONG_PLAY_NOW,
            SONG_APPEND_QUEUE,
            SONG_SINGLE_PLAY,
        )
    }

    val multipleItemsModes by lazy {
        intArrayOf(
            QUEUE_PLAY_NEXT,
            QUEUE_PLAY_NOW,
            QUEUE_APPEND_QUEUE,
            QUEUE_SWITCH_TO_BEGINNING,
            QUEUE_SWITCH_TO_POSITION,
            QUEUE_SHUFFLE,
        )
    }

    val modesRequiringInstantlyChangingState by lazy {
        intArrayOf(
            SONG_PLAY_NOW,
            SONG_SINGLE_PLAY,
            QUEUE_PLAY_NOW,
            QUEUE_SWITCH_TO_BEGINNING,
            QUEUE_SWITCH_TO_POSITION,
            QUEUE_SHUFFLE,
        )
    }


}