/*
 * Copyright (c) 2022 chr_56 & Abou Zeid (kabouzeid) (original author)
 */

package lib.phonograph.activity

import player.phonograph.settings.ThemeSetting.accentColor
import player.phonograph.settings.ThemeSetting.navigationBarColor
import player.phonograph.settings.ThemeSetting.primaryColor
import player.phonograph.R
import player.phonograph.mechanism.setting.StyleConfig
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import player.phonograph.util.theme.nightMode
import util.theme.activity.adjustStatusbarText
import util.theme.activity.setNavigationBarColor
import util.theme.activity.setStatusbarColor
import util.theme.activity.setTaskDescriptionColor
import util.theme.color.darkenColor
import util.theme.color.primaryTextColor
import util.theme.color.secondaryTextColor
import androidx.lifecycle.lifecycleScope
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.animation.PathInterpolator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * An abstract class providing material activity (no toolbar)
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class ThemeActivity : MultiLanguageActivity() {
    private var createTime: Long = -1

    protected var primaryColor: Int = 0
    protected var accentColor: Int = 0
    protected var textColorPrimary: Int = 0
    protected var textColorSecondary: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        observeColors()

        super.onCreate(savedInstanceState)
        createTime = System.currentTimeMillis()

        // theme
        setTheme(StyleConfig.generalThemeStyle(this))

        // immersive status bar
        if (useCustomStatusBar) setFullScreenAndIncludeStatusBar()

        // color
        if (autoSetStatusBarColor) setStatusbarColor(primaryColor(this))
        if (autoSetNavigationBarColor) setNavigationBarColor(navigationBarColor(this))
        if (autoSetTaskDescriptionColor) setTaskDescriptionColor(primaryColor(this))
    }

    /** Must call before super */
    protected var useCustomStatusBar: Boolean = true
        set(value) {
            field = value
            if (value) setFullScreenAndIncludeStatusBar()
        }

    /** Must call before super */
    protected var autoSetStatusBarColor: Boolean = true

    /** Must call before super */
    protected var autoSetNavigationBarColor: Boolean = true

    /** Must call before super */
    protected var autoSetTaskDescriptionColor: Boolean = true

    private fun observeColors() {
        primaryColor = primaryColor(this)
        accentColor = accentColor(this)
        lifecycleScope.launch(Dispatchers.IO) {
            Setting(this@ThemeActivity)[Keys.selectedPrimaryColor].flow.collect {
                primaryColor = it
                requireRecreate = true
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            Setting(this@ThemeActivity)[Keys.selectedAccentColor].flow.collect {
                accentColor = it
                requireRecreate = true
            }
        }
        textColorPrimary = primaryTextColor(nightMode)
        textColorSecondary = secondaryTextColor(nightMode)
    }


    override fun onResume() {
        super.onResume()
        if (requireRecreate) {
            postRecreate()
            requireRecreate = false
        }
    }

    private var requireRecreate: Boolean = false
    protected fun postRecreate() {
        // hack to prevent java.lang.RuntimeException: Performing pause of activity that is not resumed
        // makes sure recreate() is called right after and not in onResume()
        Handler(Looper.getMainLooper()).post { recreate() }
    }

    protected fun updateAllColors(color: Int) {
        setStatusbarColor(color)
        setNavigationBarColor(color)
        setTaskDescriptionColor(color)
    }

    //
    // User Interface
    //
    private fun setFullScreenAndIncludeStatusBar() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            (SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    protected fun restoreNotFullsScreen() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility -=
            (SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    //
    // Status Bar
    //
    /**
     * This will set the color of the view with the id "status_bar" on Lollipop.
     * On Lollipop if no such view is found it will set the statusbar color using the native method.
     *
     * @param color the new statusbar color (will be shifted down on Lollipop and above)
     */
    open fun setStatusbarColor(color: Int) {
        val darkColor = darkenColor(color)
        setStatusbarColor(darkColor, R.id.status_bar)
        adjustStatusbarText(darkColor)
    }

    //
    // SnackBar holder
    //
    protected open val snackBarContainer: View get() = window.decorView

    //
    // Animation
    //
    private var colorChangeAnimator: ValueAnimator? = null
    protected fun animateThemeColorChange(oldColor: Int, newColor: Int) {
        animateThemeColorChange(oldColor, newColor) { animation: ValueAnimator ->
            setStatusbarColor(animation.animatedValue as Int)
            if (Setting(this)[Keys.coloredNavigationBar].data) setNavigationBarColor(animation.animatedValue as Int)
        }
    }

    protected fun animateThemeColorChange(
        oldColor: Int, newColor: Int, action: (ValueAnimator) -> Unit,
    ) { // todo: make sure lifecycle
        colorChangeAnimator?.cancel()
        colorChangeAnimator = ValueAnimator
            .ofArgb(oldColor, newColor)
            .setDuration(600L)
        colorChangeAnimator?.also { animator ->
            animator.interpolator = PathInterpolator(0.4f, 0f, 1f, 1f)
            animator.addUpdateListener(action)
            animator.start()
        }
    }

    protected fun cancelThemeColorChange() {
        colorChangeAnimator?.cancel()
        colorChangeAnimator = null
    }
}
