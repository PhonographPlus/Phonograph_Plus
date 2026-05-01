/*
 *  Copyright (c) 2022~2026 chr_56
 */

package player.phonograph.model.playlist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface PlaylistLocation : Parcelable, Comparable<PlaylistLocation> {
    fun id(): Long
}

@Parcelize
sealed class VirtualPlaylistLocation(@param:PlaylistType val type: Int) : PlaylistLocation {
    override fun id(): Long = produceLocationSectionedId(type.toLong(), SECTION_VIRTUAL)
    override fun compareTo(other: PlaylistLocation): Int = 1

    data object Favorite : VirtualPlaylistLocation(PLAYLIST_TYPE_FAVORITE)
    data object LastAdded : VirtualPlaylistLocation(PLAYLIST_TYPE_LAST_ADDED)
    data object History : VirtualPlaylistLocation(PLAYLIST_TYPE_HISTORY)
    data object MyTopTrack : VirtualPlaylistLocation(PLAYLIST_TYPE_MY_TOP_TRACK)
    data object Random : VirtualPlaylistLocation(PLAYLIST_TYPE_RANDOM)
}


@Parcelize
data class DatabasePlaylistLocation(val databaseId: Long) : PlaylistLocation {
    override fun id(): Long = produceLocationSectionedId(databaseId, SECTION_DATABASE)

    override fun toString(): String = "Database(id: $databaseId)"
    override fun compareTo(other: PlaylistLocation): Int = when (other) {
        is DatabasePlaylistLocation -> other.databaseId.compareTo(databaseId)
        is FilePlaylistLocation     -> 0
        else                        -> -1
    }
}

@Parcelize
data class FilePlaylistLocation(
    val path: String,
    val storageVolume: String,
    val mediastoreId: Long,
) : PlaylistLocation {
    override fun id(): Long = produceLocationSectionedId(mediastoreId, SECTION_MEDIASTORE)

    override fun toString(): String = path
    override fun compareTo(other: PlaylistLocation): Int = if (other is FilePlaylistLocation) {
        other.path.compareTo(path)
    } else {
        -1
    }
}

private const val SECTION_MEDIASTORE = 0
private const val SECTION_DATABASE = 1 shl 1
private const val SECTION_VIRTUAL = 1 shl 2

private const val SID_SHIFT = Long.SIZE_BITS - Byte.SIZE_BITS // 56
private const val SID_MASK_LOWER = (1L shl SID_SHIFT) - 1 // 0x00ff_ffff_ffff_ffff

/**
 * Generate a sectioned 64-bits ID: the lowers 56 bits is cut from [id], the higher 8 bits is shifted from [section]
 */
private fun produceLocationSectionedId(id: Long, section: Int): Long {
    return (id and SID_MASK_LOWER) + (section shl SID_SHIFT)
}