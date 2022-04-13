/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.model.lyrics2

import android.os.Parcel
import android.os.Parcelable
import java.lang.IllegalStateException

data class LyricsWithSource(val lyrics: AbsLyrics, val source: LyricsSource) : Parcelable {

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<LyricsWithSource> {
            override fun createFromParcel(parcel: Parcel): LyricsWithSource {
                return LyricsWithSource(parcel)
            }

            override fun newArray(size: Int): Array<LyricsWithSource?> {
                return arrayOfNulls(size)
            }
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(AbsLyrics::class.java.classLoader) ?: throw IllegalStateException("error when create LyricsWithSource from parcel"),
        LyricsSource(parcel.readInt())
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(lyrics, flags)
        parcel.writeInt(source.type)
    }
    override fun describeContents(): Int = 0
}

data class LyricsSource(val type: Int = UNKNOWN_SOURCE) {
    @Suppress("FunctionName")
    companion object {
        fun Embedded() = LyricsSource(EMBEDDED)
        fun ExternalPrecise() = LyricsSource(EXTERNAL_PRECISE)
        fun ExternalDecorated() = LyricsSource(EXTERNAL_DECORATED)

        const val EMBEDDED = 0
        const val EXTERNAL_PRECISE = 1
        const val EXTERNAL_DECORATED = 2

        fun Unknown() = LyricsSource(UNKNOWN_SOURCE)
        const val UNKNOWN_SOURCE = -1
    }
}
