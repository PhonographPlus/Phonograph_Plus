/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.ui.modules.search

import player.phonograph.model.pages.HomePage
import player.phonograph.model.pages.PAGE_ALBUM
import player.phonograph.model.pages.PAGE_ARTIST
import player.phonograph.model.pages.PAGE_GENRE
import player.phonograph.model.pages.PAGE_PLAYLIST
import player.phonograph.model.pages.PAGE_SONG
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SearchResultPageAdapter(
    activity: FragmentActivity,
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = SearchType.entries.size

    override fun createFragment(position: Int): Fragment {
        return when (SearchType.entries[position]) {
            SearchType.SONGS     -> SongSearchResultPageFragment()
            SearchType.ALBUMS    -> AlbumSearchResultPageFragment()
            SearchType.ARTISTS   -> ArtistSearchResultPageFragment()
            SearchType.PLAYLISTS -> PlaylistSearchResultPageFragment()
            SearchType.GENRES    -> GenreSearchResultPageFragment()
            SearchType.QUEUE     -> QueueSearchResultPageFragment()
        }
    }

    fun lookup(@HomePage name: String?): Int =
        when (name) {
            PAGE_SONG     -> SearchType.SONGS.ordinal
            PAGE_ALBUM    -> SearchType.ALBUMS.ordinal
            PAGE_ARTIST   -> SearchType.ARTISTS.ordinal
            PAGE_PLAYLIST -> SearchType.PLAYLISTS.ordinal
            PAGE_GENRE    -> SearchType.GENRES.ordinal
            else          -> 0
        }

}