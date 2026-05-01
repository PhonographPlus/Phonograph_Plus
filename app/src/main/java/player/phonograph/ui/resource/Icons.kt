/*
 *  Copyright (c) 2022~2026 chr_56
 */

package player.phonograph.ui.resource

import player.phonograph.R
import player.phonograph.model.notification.NotificationAction
import player.phonograph.model.pages.HomePage
import player.phonograph.model.pages.PAGE_ALBUM
import player.phonograph.model.pages.PAGE_ARTIST
import player.phonograph.model.pages.PAGE_FILES
import player.phonograph.model.pages.PAGE_FOLDER
import player.phonograph.model.pages.PAGE_GENRE
import player.phonograph.model.pages.PAGE_PLAYLIST
import player.phonograph.model.pages.PAGE_SONG
import player.phonograph.model.service.MusicServiceStatus
import player.phonograph.model.service.RepeatMode
import player.phonograph.model.service.ShuffleMode
import androidx.annotation.DrawableRes

object Icons {
    @DrawableRes
    fun page(@HomePage page: String?): Int = when (page) {
        PAGE_SONG     -> R.drawable.ic_music_note_white_24dp
        PAGE_ALBUM    -> R.drawable.ic_album_white_24dp
        PAGE_ARTIST   -> R.drawable.ic_person_white_24dp
        PAGE_PLAYLIST -> R.drawable.ic_queue_music_white_24dp
        PAGE_GENRE    -> R.drawable.ic_bookmark_music_white_24dp
        PAGE_FOLDER   -> R.drawable.ic_folder_white_24dp
        PAGE_FILES    -> R.drawable.ic_folder_white_24dp
        else          -> R.drawable.ic_library_music_white_24dp
    }

    @DrawableRes
    fun notificationAction(notification: NotificationAction, status: MusicServiceStatus) =
        when (notification) {
            NotificationAction.PlayPause   -> if (status.isPlaying) R.drawable.ic_pause_white_24dp else R.drawable.ic_play_arrow_white_24dp
            NotificationAction.Prev        -> R.drawable.ic_skip_previous_white_24dp
            NotificationAction.Next        -> R.drawable.ic_skip_next_white_24dp
            NotificationAction.Repeat      -> when (status.repeatMode) {
                RepeatMode.NONE               -> R.drawable.ic_repeat_off_white_24dp
                RepeatMode.REPEAT_QUEUE       -> R.drawable.ic_repeat_white_24dp
                RepeatMode.REPEAT_SINGLE_SONG -> R.drawable.ic_repeat_one_white_24dp
            }

            NotificationAction.Shuffle     -> when (status.shuffleMode) {
                ShuffleMode.NONE    -> R.drawable.ic_shuffle_disabled_white_24dp
                ShuffleMode.SHUFFLE -> R.drawable.ic_shuffle_white_24dp
            }

            NotificationAction.FastForward -> R.drawable.ic_fast_forward_white_24dp
            NotificationAction.FastRewind  -> R.drawable.ic_fast_rewind_white_24dp
            NotificationAction.Fav         -> R.drawable.ic_favorite_border_white_24dp
            NotificationAction.Close       -> R.drawable.ic_close_white_24dp
            NotificationAction.Invalid     -> R.drawable.ic_notification
        }
}
