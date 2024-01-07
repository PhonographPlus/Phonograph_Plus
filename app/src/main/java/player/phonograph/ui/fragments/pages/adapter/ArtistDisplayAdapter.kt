/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.ui.fragments.pages.adapter

import coil.size.ViewSizeResolver
import player.phonograph.R
import player.phonograph.actions.ClickActionProviders
import player.phonograph.coil.loadImage
import player.phonograph.coil.target.PaletteTargetBuilder
import player.phonograph.model.Artist
import player.phonograph.model.sort.SortRef
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import player.phonograph.ui.adapter.DisplayAdapter
import player.phonograph.ui.adapter.DisplayConfig
import player.phonograph.util.text.makeSectionName
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup

class ArtistDisplayAdapter(
    activity: AppCompatActivity,
    config: DisplayConfig,
) : DisplayAdapter<Artist>(activity, config) {

    override fun getSectionNameImp(position: Int): String {
        val artist = dataset[position]
        val sortMode = Setting(activity).Composites[Keys.artistSortMode].data
        val sectionName: String =
            when (sortMode.sortRef) {
                SortRef.ARTIST_NAME -> makeSectionName(artist.name)
                SortRef.ALBUM_COUNT -> artist.albumCount.toString()
                SortRef.SONG_COUNT  -> artist.songCount.toString()
                else                -> ""
            }
        return makeSectionName(sectionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayViewHolder<Artist> {
        return ArtistViewHolder(inflatedView(parent, viewType))
    }

    class ArtistViewHolder(itemView: View) : DisplayViewHolder<Artist>(itemView) {
        init {
            setImageTransitionName(itemView.context.getString(R.string.transition_artist_image))
        }

        override fun getRelativeOrdinalText(item: Artist): String = item.songCount.toString()

        override val defaultIcon: Drawable?
            get() = AppCompatResources.getDrawable(itemView.context, R.drawable.default_artist_image)

        override fun setImage(position: Int, dataset: List<Artist>, usePalette: Boolean) {
            super.setImage(position, dataset, usePalette)
            val context = itemView.context
            image?.let { view ->
                loadImage(itemView.context) {
                    data(dataset[position])
                    size(ViewSizeResolver(view))
                    target(
                        PaletteTargetBuilder(context)
                            .onStart {
                                view.setImageResource(R.drawable.default_album_art)
                                setPaletteColors(context.getColor(R.color.defaultFooterColor))
                            }
                            .withConditionalYield { attached }
                            .onResourceReady { result, palette ->
                                view.setImageDrawable(result)
                                if (usePalette) setPaletteColors(palette)
                            }
                            .build()
                    )
                }
            }
        }

        override val clickActionProvider: ClickActionProviders.ClickActionProvider<Artist>
            get() = ClickActionProviders.ArtistClickActionProvider()
    }
}
