@file:Suppress("DeprecatedCallableAddReplaceWith")

package player.phonograph.ui.views

import player.phonograph.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Property
import android.view.animation.DecelerateInterpolator
import kotlin.math.roundToInt

class PlayPauseDrawable(context: Context) : Drawable() {

    private val leftPauseBar = Path()
    private val rightPauseBar = Path()

    private val paint =
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.WHITE
        }

    private val pauseBarWidth: Float = context.resources.getDimensionPixelSize(R.dimen.pause_bar_width).toFloat()
    private val pauseBarHeight: Float = context.resources.getDimensionPixelSize(R.dimen.pause_bar_height).toFloat()
    private val pauseBarDistance: Float = context.resources.getDimensionPixelSize(R.dimen.pause_bar_distance).toFloat()

    private var width = 0f
    private var height = 0f

    private var progress: Float = 0f
        private set(value) {
            field = value
            invalidateSelf()
        }

    private var isPlay = false
    private var isPlaySet = false

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        if (bounds.width() > 0 && bounds.height() > 0) {
            width = bounds.width().toFloat()
            height = bounds.height().toFloat()
        }
    }

    override fun draw(canvas: Canvas) {
        leftPauseBar.rewind()
        rightPauseBar.rewind()

        // The current distance between the two pause bars.
        val barDist = lerp(pauseBarDistance, 0f, progress)
        // The current width of each pause bar.
        val rawBarWidth = lerp(pauseBarWidth, pauseBarHeight / 1.75f, progress)
        // We have to round the bar width when finishing the progress to prevent the gap
        // that might occur onDraw because of a pixel is lost when casting float to int instead of rounding it.
        val barWidth = if (progress == 1f) rawBarWidth.roundToInt().toFloat() else rawBarWidth
        // The current position of the left pause bar's top left coordinate.
        val firstBarTopLeft = lerp(0f, barWidth, progress)
        // The current position of the right pause bar's top right coordinate.
        val secondBarTopRight = lerp(2f * barWidth + barDist, barWidth + barDist, progress)

        // Draw the left pause bar. The left pause bar transforms into the
        // top half of the play button triangle by animating the position of the
        // rectangle's top left coordinate and expanding its bottom width.
        leftPauseBar.moveTo(0f, 0f)
        leftPauseBar.lineTo(firstBarTopLeft, -pauseBarHeight)
        leftPauseBar.lineTo(barWidth, -pauseBarHeight)
        leftPauseBar.lineTo(barWidth, 0f)
        leftPauseBar.close()

        // Draw the right pause bar. The right pause bar transforms into the
        // bottom half of the play button triangle by animating the position of the
        // rectangle's top right coordinate and expanding its bottom width.
        rightPauseBar.moveTo(barWidth + barDist, 0f)
        rightPauseBar.lineTo(barWidth + barDist, -pauseBarHeight)
        rightPauseBar.lineTo(secondBarTopRight, -pauseBarHeight)
        rightPauseBar.lineTo(2 * barWidth + barDist, 0f)
        rightPauseBar.close()
        val saveCount = canvas.save()

        // Translate the play button a tiny bit to the right so it looks more centered.
        canvas.translate(lerp(0f, pauseBarHeight / 8f, progress), 0f)

        // (1) Pause --> Play: rotate 0 to 90 degrees clockwise.
        // (2) Play --> Pause: rotate 90 to 180 degrees clockwise.
        val rotationProgress = if (isPlay) 1f - progress else progress
        val startingRotation = if (isPlay) 90f else 0f
        canvas.rotate(
            lerp(startingRotation, startingRotation + 90f, rotationProgress),
            width / 2f,
            height / 2f
        )

        // Position the pause/play button in the center of the drawable's bounds.
        canvas.translate(
            (width / 2f - (2f * barWidth + barDist) / 2f).roundToInt().toFloat(),
            (height / 2f + pauseBarHeight / 2f).roundToInt().toFloat()
        )

        // Draw the two bars that form the animated pause/play button.
        canvas.drawPath(leftPauseBar, paint)
        canvas.drawPath(rightPauseBar, paint)
        canvas.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
        invalidateSelf()
    }

    @Deprecated("This method is no longer used in graphics optimizations")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    fun update(state: Boolean, animate: Boolean) {
        if (animate) {
            if (state xor isPlaySet) { // (state && !isPlaySet) || (!state && isPlaySet)
                playAnimation()
            }
        } else {
            isPlaySet = state
            isPlay = state
            progress = if (state) 1f else 0f
        }
    }

    private var animator: Animator? = null

    private fun playAnimation() {
        animator?.cancel()
        isPlaySet = !isPlaySet
        animator = createPausePlayAnimator().also { it.start() }
    }


    private fun createPausePlayAnimator(): Animator =
        ObjectAnimator.ofFloat(
            this,
            PROGRESS,
            if (isPlay) 1f else 0f,
            if (isPlay) 0f else 1f
        ).also { animator ->
            animator.interpolator = DecelerateInterpolator()
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isPlay = !isPlay
                }
            })
            animator.duration = PLAY_PAUSE_ANIMATION_DURATION
        }

    companion object {
        private const val PLAY_PAUSE_ANIMATION_DURATION: Long = 250

        private val PROGRESS: Property<PlayPauseDrawable, Float> =
            object : Property<PlayPauseDrawable, Float>(Float::class.java, "progress") {
                override fun get(drawable: PlayPauseDrawable): Float = drawable.progress

                override fun set(drawable: PlayPauseDrawable, value: Float) {
                    drawable.progress = value
                }
            }

        /**
         * Linear interpolate between a and b with parameter t.
         */
        private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
    }
}
