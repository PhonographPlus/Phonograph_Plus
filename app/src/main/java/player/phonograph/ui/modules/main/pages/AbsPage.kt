/*
 *  Copyright (c) 2022~2024 chr_56
 */

package player.phonograph.ui.modules.main.pages

import player.phonograph.ui.modules.main.MainActivity
import player.phonograph.ui.modules.main.MainFragment
import player.phonograph.ui.modules.panel.AbsMusicServiceFragment
import player.phonograph.util.debug
import player.phonograph.util.logMetrics

// todo: remove AbsMusicServiceFragment
abstract class AbsPage : AbsMusicServiceFragment() {

    protected val mainFragment: MainFragment
        get() = (parentFragment as? MainFragment)
            ?: throw IllegalStateException("${this::class.simpleName} hasn't attach to MainFragment")

    protected val mainActivity: MainActivity get() = mainFragment.mainActivity

    override fun onResume() {
        super.onResume()
        debug { logMetrics("AbsDisplayPage.onResume()") }
    }

}
