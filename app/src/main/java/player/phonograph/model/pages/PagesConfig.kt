/*
 *  Copyright (c) 2022~2025 chr_56
 */

package player.phonograph.model.pages

import androidx.annotation.Keep
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
data class PagesConfig(
    @SerialName("tabs") val tabs: List<String>,
) : List<String> by tabs, Parcelable {

    companion object {
        val DEFAULT_CONFIG = PagesConfig(
            mutableListOf(
                PAGE_SONG,
                PAGE_FOLDER,
                PAGE_FILES,
                PAGE_PLAYLIST,
                PAGE_ALBUM,
                PAGE_ARTIST,
                PAGE_GENRE,
            )
        )
    }
}