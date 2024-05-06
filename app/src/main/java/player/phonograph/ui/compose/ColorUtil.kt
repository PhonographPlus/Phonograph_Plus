/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.ui.compose

import util.theme.color.primaryTextColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import android.content.Context
import android.graphics.Color.RGBToHSV
import kotlin.math.abs
import kotlin.math.roundToInt


private fun Color.hsvShift(by: Float): Color {
    val hsv = floatArrayOf(0f, 0f, 0f)
    RGBToHSV((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt(), hsv)
    return Color.hsv(hsv[0], hsv[1], hsv[2] * by)
}

fun Color.darker(): Color = hsvShift(0.8f)

fun Color.lighter(): Color = hsvShift(1.25f)



fun isColorRelevant(a: Color, b: Color): Boolean {
    return (abs(a.luminance() - b.luminance()) <= 0.0625f) or (
            (abs(a.red - b.red) <= 0.08) and (abs(a.green - b.green) <= 0.08) and (abs(a.blue - b.blue) <= 0.08)
            )
}

inline fun makeSureContrastWith(backgroundColor: Color, block: () -> Color): Color {
    val goingDarker = backgroundColor.isColorLight()
    var newColor = block()
    while (isColorRelevant(newColor, backgroundColor)) {
        newColor = if (goingDarker) newColor.darker() else newColor.lighter()
    }
    return newColor
}

fun Color.isColorLight(): Boolean = luminance() >= 0.5f

fun Color.getReverseColor(): Color {
    val r = colorSpace.getMaxValue(1) - red
    val g = colorSpace.getMaxValue(2) - green
    val b = colorSpace.getMaxValue(3) - blue
    return Color(r, g, b, alpha, colorSpace)
}

fun textColorOn(context: Context, color: Color): Color =
    Color(
        context.primaryTextColor(color.toArgb())
    )
