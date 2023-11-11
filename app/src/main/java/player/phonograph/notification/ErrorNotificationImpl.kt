/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.notification

import player.phonograph.R
import player.phonograph.notification.ErrorNotification.KEY_IS_A_CRASH
import player.phonograph.notification.ErrorNotification.KEY_NOTE
import player.phonograph.notification.ErrorNotification.KEY_STACK_TRACE
import androidx.core.app.NotificationCompat
import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent

class ErrorNotificationImpl(context: Context, private val crashActivity: Class<out Activity>) : AbsNotificationImpl() {

    override val channelId: String = NOTIFICATION_CHANNEL_ID_ERROR
    override val channelName: CharSequence = context.getString(R.string.error_notification_name)
    override val importance: Int = NotificationManager.IMPORTANCE_HIGH

    fun send(note: String, throwable: Throwable? = null, title: String? = null, context: Context) {
        val action = Intent(context, crashActivity).apply {
            putExtra(KEY_NOTE, note)
            putExtra(KEY_STACK_TRACE, throwable?.stackTraceToString())
            putExtra(KEY_IS_A_CRASH, false)
        }

        val clickIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, action, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)


        execute(context) {
            val notification: Notification =
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_ERROR)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setCategory(NotificationCompat.CATEGORY_ERROR)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setContentTitle(context.getString(R.string.error_notification_name))
                    .setContentText(note)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(context.getString(R.string.error_notification_name))
                            .setSummaryText(title).bigText(note)
                    )
                    .setContentIntent(clickIntent).setAutoCancel(true)
                    .build()

            count++
            notificationManager.notify(count, notification)
        }

    }


    companion object {
        private var count = 0
        private const val NOTIFICATION_CHANNEL_ID_ERROR = "error_notification"
    }

}
