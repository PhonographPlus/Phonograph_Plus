/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.model.pages

import androidx.annotation.StringDef

const val PAGE_EMPTY = "EMPTY"
const val PAGE_SONG = "SONG"
const val PAGE_ALBUM = "ALBUM"
const val PAGE_ARTIST = "ARTIST"
const val PAGE_PLAYLIST = "PLAYLIST"
const val PAGE_GENRE = "GENRE"
const val PAGE_FOLDER = "FOLDER"
const val PAGE_FILES = "FILES"

@StringDef(
    value = [
        PAGE_EMPTY,
        PAGE_SONG,
        PAGE_ALBUM,
        PAGE_ARTIST,
        PAGE_PLAYLIST,
        PAGE_GENRE,
        PAGE_FOLDER,
        PAGE_FILES,
    ],
)
@Retention(AnnotationRetention.SOURCE)
annotation class HomePage
