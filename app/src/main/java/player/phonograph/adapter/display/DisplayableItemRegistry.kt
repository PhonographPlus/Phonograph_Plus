/*
 * Copyright (c) 2022 chr_56
 */

@file:JvmName("DisplayableItemRegistry")

package player.phonograph.adapter.display

import android.app.Activity
import android.content.Context
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.core.util.Pair
import player.phonograph.R
import player.phonograph.actions.applyToPopupMenu
import player.phonograph.model.Album
import player.phonograph.model.Artist
import player.phonograph.model.Displayable
import player.phonograph.model.Genre
import player.phonograph.model.Song
import player.phonograph.service.MusicPlayerRemote.playNow
import player.phonograph.service.MusicPlayerRemote.playQueue
import player.phonograph.settings.Setting
import player.phonograph.util.NavigationUtil

/**
 * involve item click
 * @param list      (optional) a list that this Displayable is among
 * @param activity  (optional) for SceneTransitionAnimation
 * @param imageView (optional) item's imagine for SceneTransitionAnimation
 * @return true if action have been processed
 */
fun Displayable.tapClick(list: List<Displayable>?, activity: Activity?, imageView: ImageView?): Boolean {
    return when (this) {
        is Song -> {
            val contextQueue = list?.filterIsInstance<Song>()
            if (contextQueue != null) {
                if (Setting.instance.keepPlayingQueueIntact) {
                    playNow(this)
                } else {
                    playQueue(contextQueue, contextQueue.indexOf(this), true, null)
                }
            }
            true
        }
        is Album -> {
            if (activity != null) {
                if (imageView != null) {
                    NavigationUtil.goToAlbum(
                        activity,
                        this.id,
                        Pair(
                            imageView,
                            imageView.resources.getString(R.string.transition_album_art)
                        )
                    )
                } else {
                    NavigationUtil.goToAlbum(
                        activity,
                        this.id
                    )
                }
                true
            } else {
                false
            }
        }
        is Artist -> {
            if (activity != null) {
                if (imageView != null) {
                    NavigationUtil.goToArtist(
                        activity,
                        this.id,
                        Pair(
                            imageView,
                            imageView.resources.getString(R.string.transition_artist_image)
                        )
                    )
                } else {
                    NavigationUtil.goToArtist(
                        activity,
                        this.id
                    )
                }
                true
            } else {
                false
            }
        }
        is Genre -> {
            if (activity != null) {
                NavigationUtil.goToGenre(activity, this)
                true
            } else {
                false
            }
        }
        else -> false
    }
}

fun Displayable.hasMenu(): Boolean = this is Song

/**
 * setup three-dot menu for [Song]
 */
fun Displayable.initMenu(
    context: Context,
    menu: Menu,
    enableCollapse: Boolean = true,
    showPlay: Boolean = false,
    index: Int = Int.MIN_VALUE,
    transitionView: View? = null,
) =
    if (this is Song) {
        applyToPopupMenu(context, menu, this, enableCollapse, showPlay, index, transitionView)
    } else {
        menu.clear()
    }

/**
 * for fast-scroll recycler-view's bar hint
 */
fun Displayable?.defaultSortOrderReference(): String? =
    when (this) {
        is Song -> this.title
        is Album -> this.title
        is Artist -> this.name
        is Genre -> this.name
        else -> null
    }
