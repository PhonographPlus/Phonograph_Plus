/*
 *  Copyright (c) 2022~2023 chr_56 & Abou Zeid (kabouzeid) (original author)
 */

package player.phonograph.misc

import player.phonograph.R
import player.phonograph.util.MEDIASTORE_VOLUME_EXTERNAL
import player.phonograph.util.mediaStoreUriSong
import player.phonograph.util.theme.tintButtons
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings

object RingtoneManager {

    fun setRingtone(context: Context, songId: Long): Boolean {
        if (systemSettingCanWrite(context)) {
            showDialog(context)
        } else {
            setRingtoneImpl(context, songId)
        }
        return true
    }

    private fun setRingtoneImpl(context: Context, songId: Long) {
        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_ALARM,
            mediaStoreUriSong(MEDIASTORE_VOLUME_EXTERNAL, songId)
        )
    }

    private fun systemSettingCanWrite(context: Context): Boolean = !Settings.System.canWrite(context)

    private fun showDialog(context: Context): AlertDialog = AlertDialog.Builder(context)
        .setTitle(R.string.dialog_ringtone_title)
        .setMessage(R.string.dialog_ringtone_message)
        .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        .setPositiveButton(android.R.string.ok) { _, _ ->
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                }
            )
        }
        .create().tintButtons()
}
