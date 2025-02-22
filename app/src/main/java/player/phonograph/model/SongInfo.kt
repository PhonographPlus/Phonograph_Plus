/*
 * Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.model

import org.jaudiotagger.tag.FieldKey
import player.phonograph.R
import androidx.annotation.StringRes
import android.content.res.Resources


/**
 * retrieve corresponding a tag name string resource id for a music tag
 */
@StringRes
fun FieldKey.res(): Int =
    when (this) {
        FieldKey.TITLE        -> R.string.title
        FieldKey.ARTIST       -> R.string.artist
        FieldKey.ALBUM        -> R.string.album
        FieldKey.ALBUM_ARTIST -> R.string.album_artist
        FieldKey.COMPOSER     -> R.string.composer
        FieldKey.LYRICIST     -> R.string.lyricist
        FieldKey.YEAR         -> R.string.year
        FieldKey.GENRE        -> R.string.genre
        FieldKey.DISC_NO      -> R.string.disk_number
        FieldKey.DISC_TOTAL   -> R.string.disk_number_total
        FieldKey.TRACK        -> R.string.track
        FieldKey.TRACK_TOTAL  -> R.string.track_total
        FieldKey.RATING       -> R.string.rating
        FieldKey.COMMENT      -> R.string.comment
        else                  -> -1
    }

fun FieldKey.text(resources: Resources): String {
    val stringRes = res()
    return if (stringRes > 0) resources.getString(stringRes) else name
}

val allFieldKey =
    setOf(
        FieldKey.TITLE,
        FieldKey.ARTIST,
        FieldKey.ALBUM,
        FieldKey.ALBUM_ARTIST,
        FieldKey.COMPOSER,
        FieldKey.LYRICIST,
        FieldKey.YEAR,
        FieldKey.GENRE,
        FieldKey.DISC_NO,
        FieldKey.DISC_TOTAL,
        FieldKey.TRACK,
        FieldKey.TRACK_TOTAL,
        FieldKey.RATING,
        FieldKey.COMMENT,
    ) + FieldKey.values()
