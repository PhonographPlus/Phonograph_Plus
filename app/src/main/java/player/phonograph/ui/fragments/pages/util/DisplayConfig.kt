/*
 * Copyright (c) 2022 chr_56
 */


package player.phonograph.ui.fragments.pages.util

import player.phonograph.App
import player.phonograph.R
import player.phonograph.model.sort.SortMode
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import player.phonograph.settings.dataStore
import player.phonograph.ui.adapter.ViewHolderType
import player.phonograph.ui.adapter.ViewHolderTypes
import player.phonograph.ui.fragments.pages.util.DisplayConfigTarget.AlbumPage
import player.phonograph.ui.fragments.pages.util.DisplayConfigTarget.ArtistPage
import player.phonograph.ui.fragments.pages.util.DisplayConfigTarget.GenrePage
import player.phonograph.ui.fragments.pages.util.DisplayConfigTarget.PlaylistPage
import player.phonograph.ui.fragments.pages.util.DisplayConfigTarget.SongPage
import player.phonograph.util.debug
import player.phonograph.util.ui.isLandscape
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import android.content.Context
import android.content.res.Resources
import android.util.Log
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal sealed class DisplayConfigTarget {
    object SongPage : DisplayConfigTarget()
    object AlbumPage : DisplayConfigTarget()
    object ArtistPage : DisplayConfigTarget()
    object GenrePage : DisplayConfigTarget()
    object PlaylistPage : DisplayConfigTarget()
}


@Suppress("REDUNDANT_ELSE_IN_WHEN")
class DisplayConfig internal constructor(private val page: DisplayConfigTarget) {

    private val isLandscape: Boolean
        get() = isLandscape(App.instance.resources)

    @ViewHolderType
    fun layoutType(size: Int): Int =
        if (gridMode(size)) ViewHolderTypes.GRID else when (page) {
            is PlaylistPage -> ViewHolderTypes.LIST_SINGLE_ROW
            is GenrePage    -> ViewHolderTypes.LIST_NO_IMAGE
            else            -> ViewHolderTypes.LIST
        }

    fun gridMode(size: Int): Boolean = size > maxGridSizeForList

    val maxGridSize: Int
        get() {
            val res = App.instance.resources
            return when (page) {
                is PlaylistPage -> 1 //always
                else            ->
                    if (isLandscape) res.getInteger(R.integer.max_columns_land) else res.getInteger(R.integer.max_columns)
            }
        }

    private val maxGridSizeForList: Int
        get() {
            val res = App.instance.resources
            return when (page) {
                is GenrePage, is PlaylistPage -> Int.MAX_VALUE //always
                else                          ->
                    if (isLandscape) res.getInteger(R.integer.default_list_columns_land) else res.getInteger(R.integer.default_list_columns)
            }
        }

    var sortMode: SortMode
        get() {
            val setting = Setting(App.instance).Composites
            return setting[when (page) {
                is SongPage     -> Keys.songSortMode
                is AlbumPage    -> Keys.albumSortMode
                is ArtistPage   -> Keys.artistSortMode
                is GenrePage    -> Keys.genreSortMode
                is PlaylistPage -> Keys.playlistSortMode
            }].data
        }
        set(value) {
            val setting = Setting(App.instance).Composites
            setting[when (page) {
                is SongPage     -> Keys.songSortMode
                is AlbumPage    -> Keys.albumSortMode
                is ArtistPage   -> Keys.artistSortMode
                is GenrePage    -> Keys.genreSortMode
                is PlaylistPage -> Keys.playlistSortMode
            }].data = value
        }

    /**
     * @return true if success
     */
    fun updateSortMode(mode: SortMode): Boolean =
        if (mode != sortMode) {
            debug { Log.d("DisplayConfig", "SortMode $sortMode -> $mode ($page)") }
            sortMode = mode
            true
        } else {
            false
        }

    var gridSize: Int
        get() {
            val pref = DisplaySetting(App.instance)

            return when (page) {
                is SongPage     -> {
                    if (isLandscape) pref.songGridSizeLand
                    else pref.songGridSize
                }

                is AlbumPage    -> {
                    if (isLandscape) pref.albumGridSizeLand
                    else pref.albumGridSize
                }

                is ArtistPage   -> {
                    if (isLandscape) pref.artistGridSizeLand
                    else pref.artistGridSize
                }

                is GenrePage    -> {
                    if (isLandscape) pref.genreGridSizeLand
                    else pref.genreGridSize
                }

                is PlaylistPage -> 1
                else            -> 1
            }
        }
        set(value) {
            if (value <= 0) return
            val pref = DisplaySetting(App.instance)
            // todo valid input
            when (page) {
                is SongPage     -> {
                    if (isLandscape) pref.songGridSizeLand = value
                    else pref.songGridSize = value
                }

                is AlbumPage    -> {
                    if (isLandscape) pref.albumGridSizeLand = value
                    else pref.albumGridSize = value
                }

                is ArtistPage   -> {
                    if (isLandscape) pref.artistGridSizeLand = value
                    else pref.artistGridSize = value
                }

                is GenrePage    -> {
                    if (isLandscape) pref.genreGridSizeLand = value
                    else pref.genreGridSize = value
                }

                is PlaylistPage -> {}
                else            -> {}
            }
        }
    var colorFooter: Boolean
        get() {
            val pref = DisplaySetting(App.instance)
            return when (page) {
                is SongPage   -> {
                    pref.songColoredFooters
                }

                is AlbumPage  -> {
                    pref.albumColoredFooters
                }

                is ArtistPage -> {
                    pref.artistColoredFooters
                }

                else          -> false
            }
        }
        set(value) {
            val pref = DisplaySetting(App.instance)
            // todo valid input
            when (page) {
                is SongPage   -> {
                    pref.songColoredFooters = value
                }

                is AlbumPage  -> {
                    pref.albumColoredFooters = value
                }

                is ArtistPage -> {
                    pref.artistColoredFooters = value
                }

                else          -> {}
            }
        }
}

internal class DisplaySetting(context: Context) {

    private val store = context.dataStore
    private val resources: Resources = context.resources

    // List-Appearance
    var albumGridSize: Int by IntPref(
        ALBUM_GRID_SIZE,
        resources.getInteger(R.integer.default_grid_columns)
    )
    var songGridSize: Int by IntPref(
        SONG_GRID_SIZE,
        resources.getInteger(R.integer.default_list_columns)
    )
    var artistGridSize: Int by IntPref(
        ARTIST_GRID_SIZE,
        resources.getInteger(R.integer.default_list_columns)
    )
    var genreGridSize: Int by IntPref(
        GENRE_GRID_SIZE,
        resources.getInteger(R.integer.default_list_columns)
    )
    var albumGridSizeLand: Int by IntPref(
        ALBUM_GRID_SIZE_LAND,
        resources.getInteger(R.integer.default_grid_columns_land)
    )
    var songGridSizeLand: Int by IntPref(
        SONG_GRID_SIZE_LAND,
        resources.getInteger(R.integer.default_grid_columns_land)
    )
    var artistGridSizeLand: Int by IntPref(
        ARTIST_GRID_SIZE_LAND,
        resources.getInteger(R.integer.default_grid_columns_land)
    )
    var genreGridSizeLand: Int by IntPref(
        GENRE_GRID_SIZE_LAND,
        resources.getInteger(R.integer.default_grid_columns_land)
    )
    var albumColoredFooters by BooleanPref(ALBUM_COLORED_FOOTERS, true)
    var songColoredFooters by BooleanPref(SONG_COLORED_FOOTERS, true)
    var artistColoredFooters by BooleanPref(ARTIST_COLORED_FOOTERS, true)

    @Suppress("FunctionName", "SameParameterValue")
    private fun BooleanPref(key: String, defaultValue: Boolean) =
        Delegate(booleanPreferencesKey(key), defaultValue, store)

    @Suppress("FunctionName")
    private fun IntPref(key: String, defaultValue: Int) =
        Delegate(intPreferencesKey(key), defaultValue, store)

    private class Delegate<T>(
        private val key: Preferences.Key<T>,
        private val defaultValue: T,
        private val dataStore: DataStore<Preferences>,
    ) : ReadWriteProperty<Any?, T> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = runBlocking {
            dataStore.data.first()[key] ?: defaultValue
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            runBlocking {
                dataStore.edit { mutablePreferences ->
                    mutablePreferences[key] = value
                }
            }
        }
    }

    companion object {
        // List Size
        private const val ALBUM_GRID_SIZE = "album_grid_size"
        private const val ALBUM_GRID_SIZE_LAND = "album_grid_size_land"
        private const val SONG_GRID_SIZE = "song_grid_size"
        private const val SONG_GRID_SIZE_LAND = "song_grid_size_land"
        private const val ARTIST_GRID_SIZE = "artist_grid_size"
        private const val ARTIST_GRID_SIZE_LAND = "artist_grid_size_land"
        private const val GENRE_GRID_SIZE = "genre_grid_size"
        private const val GENRE_GRID_SIZE_LAND = "genre_grid_size_land"

        // Colored Footers
        private const val ALBUM_COLORED_FOOTERS = "album_colored_footers"
        private const val SONG_COLORED_FOOTERS = "song_colored_footers"
        private const val ARTIST_COLORED_FOOTERS = "artist_colored_footers"
    }
}