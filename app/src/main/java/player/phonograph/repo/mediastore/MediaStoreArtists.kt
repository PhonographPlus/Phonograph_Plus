/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.repo.mediastore

import player.phonograph.model.Artist
import player.phonograph.repo.loader.IArtists
import player.phonograph.repo.mediastore.loaders.ArtistLoader
import android.content.Context

object MediaStoreArtists : IArtists {

    override suspend fun all(context: Context): List<Artist> = ArtistLoader.all(context)

    override suspend fun id(context: Context, id: Long): Artist = ArtistLoader.id(context, id)

    override suspend fun searchByName(context: Context, query: String) = ArtistLoader.searchByName(context, query)

}