/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.model.lyrics

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class LyricsSource : Parcelable {
    Embedded,
    ExternalPrecise,
    ExternalDecorated,
    ManuallyLoaded,
    Unknown;

}