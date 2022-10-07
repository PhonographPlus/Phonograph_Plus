package player.phonograph.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import coil.size.ViewSizeResolver
import mt.util.color.resolveColor
import player.phonograph.R
import player.phonograph.adapter.base.MediaEntryViewHolder
import player.phonograph.coil.loadImage
import player.phonograph.model.Album
import player.phonograph.model.Artist
import player.phonograph.model.Song
import player.phonograph.model.infoString
import player.phonograph.service.MusicPlayerRemote
import player.phonograph.util.NavigationUtil.goToAlbum
import player.phonograph.util.NavigationUtil.goToArtist
import player.phonograph.util.menu.MenuClickListener

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class SearchAdapter(
    private val activity: AppCompatActivity,
    dataSet: List<Any>,
) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    var dataSet: List<Any> = dataSet
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int =
        when (dataSet[position]) {
            is Album -> ALBUM
            is Artist -> ARTIST
            is Song -> SONG
            else -> HEADER
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == HEADER) ViewHolder(LayoutInflater.from(activity).inflate(R.layout.sub_header, parent, false), viewType)
        else ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false), viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            SONG -> {
                val song = dataSet[position] as Song
                holder.title?.text = song.title
                holder.text?.text = song.infoString()
                loadImage(activity) {
                    size(ViewSizeResolver(holder.image!!))
                    data(song)
                    target(
                        onStart = { holder.image!!.setImageResource(R.drawable.default_album_art) },
                        onSuccess = { holder.image!!.setImageDrawable(it) }
                    )
                }
            }
            ALBUM -> {
                val album = dataSet[position] as Album
                holder.title?.text = album.title
                holder.text?.text = album.infoString(activity)
                loadImage(activity) {
                    data(album.safeGetFirstSong())
                    target(
                        onStart = { holder.image!!.setImageResource(R.drawable.default_album_art) },
                        onSuccess = { holder.image!!.setImageDrawable(it) }
                    )
                }
            }
            ARTIST -> {
                val artist = dataSet[position] as Artist
                holder.title?.text = artist.name
                holder.text?.text = artist.infoString(activity)
                loadImage(activity) {
                    data(artist)
                    target(
                        onStart = { holder.image!!.setImageResource(R.drawable.default_artist_image) },
                        onSuccess = { holder.image!!.setImageDrawable(it) }
                    )
                }
            }
            else -> holder.title?.text = dataSet[position].toString()
        }
    }

    override fun getItemCount(): Int = dataSet.size

    inner class ViewHolder(itemView: View, itemViewType: Int) : MediaEntryViewHolder(itemView) {

        init {
            itemView.setOnLongClickListener(null)
            if (itemViewType != HEADER) {
                itemView.setBackgroundColor(resolveColor(activity, R.attr.cardBackgroundColor))
                itemView.elevation = activity.resources.getDimensionPixelSize(R.dimen.card_elevation).toFloat()
                shortSeparator?.visibility = View.GONE
            }

            menu?.apply {
                if (itemViewType == SONG) {
                    visibility = View.VISIBLE

                    setOnClickListener(object : MenuClickListener(activity, null) {
                        override val song: Song get() = dataSet[bindingAdapterPosition] as Song
                    })
                } else {
                    visibility = View.GONE
                }
            }
            when (itemViewType) {
                ALBUM -> setImageTransitionName(activity.getString(R.string.transition_album_art))
                ARTIST -> setImageTransitionName(activity.getString(R.string.transition_artist_image))
                // else -> itemView.findViewById<View>(R.id.image_container)?.visibility = View.GONE
            }
        }
        override fun onClick(v: View) {

            val item = dataSet[bindingAdapterPosition]
            when (itemViewType) {
                ALBUM -> goToAlbum(
                    activity, (item as Album).id,
                    Pair.create(
                        image, activity.resources.getString(R.string.transition_album_art)
                    )
                )
                ARTIST -> goToArtist(
                    activity, (item as Artist).id,
                    Pair.create(
                        image, activity.resources.getString(R.string.transition_artist_image)
                    )
                )
                SONG -> {
                    MusicPlayerRemote.playNow(item as Song)
                }
            }
        }
    }

    companion object {
        private const val HEADER = 0
        private const val ALBUM = 1
        private const val ARTIST = 2
        private const val SONG = 3
    }
}
