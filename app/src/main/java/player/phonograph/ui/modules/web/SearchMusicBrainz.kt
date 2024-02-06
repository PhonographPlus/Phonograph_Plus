/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.ui.modules.web

import mms.musicbrainz.MusicBrainzArtist
import mms.musicbrainz.MusicBrainzRecording
import mms.musicbrainz.MusicBrainzRelease
import mms.musicbrainz.MusicBrainzReleaseGroup
import mms.musicbrainz.MusicBrainzSearchResult
import mms.musicbrainz.MusicBrainzSearchResultArtists
import mms.musicbrainz.MusicBrainzSearchResultRecording
import mms.musicbrainz.MusicBrainzSearchResultReleases
import mms.musicbrainz.MusicBrainzSearchResultReleasesGroup
import mms.musicbrainz.MusicBrainzTrack
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MusicBrainzSearch(viewModel: WebSearchViewModel, page: PageSearch.MusicBrainzSearch) {

    val parameterState by page.queryParameter.collectAsState()
    var searchResults: MusicBrainzSearchResult? by remember { mutableStateOf(null) }

    Column {
        val context = LocalContext.current
        MusicBrainzSearchBox(
            parameterState, page::updateQueryParameter,
            Modifier.wrapContentHeight()
        ) { action ->
            val delegate = viewModel.clientDelegateMusicBrainz(context)
            val deferred = delegate.request(context, action)
            viewModel.viewModelScope.launch(Dispatchers.IO) {
                when (val respond = deferred.await()) {
                    is MusicBrainzSearchResultArtists       -> searchResults = respond
                    is MusicBrainzSearchResultRecording     -> searchResults = respond
                    is MusicBrainzSearchResultReleases      -> searchResults = respond
                    is MusicBrainzSearchResultReleasesGroup -> searchResults = respond
                    else                                    -> {}
                }
            }
        }


        MusicBrainzSearchResult(searchResults, Modifier.align(Alignment.CenterHorizontally)) { action ->
            viewModel.viewModelScope.launch {
                val delegate = viewModel.clientDelegateMusicBrainz(context)
                val detailPage =
                    when (val response = delegate.request(context, action).await()) {
                        is MusicBrainzArtist       -> PageDetail.MusicBrainzDetail(response)
                        is MusicBrainzRecording    -> PageDetail.MusicBrainzDetail(response)
                        is MusicBrainzRelease      -> PageDetail.MusicBrainzDetail(response)
                        is MusicBrainzReleaseGroup -> PageDetail.MusicBrainzDetail(response)
                        is MusicBrainzTrack        -> PageDetail.MusicBrainzDetail(response)
                        else                       -> null
                    }
                if (detailPage != null) {
                    viewModel.navigator.navigateTo(detailPage)
                }
            }
        }
    }
}