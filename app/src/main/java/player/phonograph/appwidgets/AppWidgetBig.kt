package player.phonograph.appwidgets


import coil.target.Target
import player.phonograph.R
import player.phonograph.model.Song
import player.phonograph.util.ui.getScreenSize
import androidx.core.graphics.drawable.toBitmapOrNull
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews


class AppWidgetBig : BaseAppWidget() {

    override val layoutId: Int get() = R.layout.app_widget_big

    override val name: String = NAME

    override val darkBackground: Boolean get() = true

    override fun updateText(context: Context, view: RemoteViews, song: Song) {
        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
            view.setViewVisibility(R.id.media_titles, View.INVISIBLE)
        } else {
            view.setViewVisibility(R.id.media_titles, View.VISIBLE)
            view.setTextViewText(R.id.title, song.title)
            view.setTextViewText(R.id.text, getSongArtistAndAlbum(song))
        }
    }

    override fun startUpdateCover(
        context: Context,
        appWidgetIds: IntArray?,
        view: RemoteViews,
        song: Song,
        isPlaying: Boolean,
    ) {
        // Load the album cover async and push the update on completion
        val p = context.getScreenSize()
        val widgetImageSize = p.x.coerceAtMost(p.y)
        loadImage(
            context = context.applicationContext,
            song = song,
            widgetImageSize = widgetImageSize,
            target =
            object : Target {
                val mainHandler: Handler = Handler(Looper.getMainLooper())
                override fun onStart(placeholder: Drawable?) {
                    mainHandler.post { onUpdate(null) }
                }

                override fun onError(error: Drawable?) {
                    mainHandler.post { onUpdate(null) }
                }

                override fun onSuccess(result: Drawable) {
                    mainHandler.post { onUpdate(result.toBitmapOrNull()) }
                }

                private fun onUpdate(bitmap: Bitmap?) {
                    cachedCover = bitmap
                    if (bitmap == null) {
                        view.setImageViewResource(R.id.image, R.drawable.default_album_art)
                    } else {
                        updateImage(context, view, bitmap)
                    }
                    pushUpdate(context.applicationContext, appWidgetIds, view)
                }
            }
        )

    }

    override fun updateImage(context: Context, view: RemoteViews, bitmap: Bitmap?) {
        view.setImageViewBitmap(R.id.image, bitmap)
    }

    override val clickableAreas: IntArray = intArrayOf(R.id.clickable_area)

    companion object {
        const val NAME = "app_widget_big"
        private var mInstance: AppWidgetBig? = null

        @JvmStatic
        @get:Synchronized
        val instance: AppWidgetBig
            get() {
                if (mInstance == null) {
                    mInstance = AppWidgetBig()
                }
                return mInstance!!
            }
    }
}
