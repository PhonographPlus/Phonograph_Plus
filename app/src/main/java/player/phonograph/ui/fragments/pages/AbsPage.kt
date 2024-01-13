/*
 * Copyright (c) 2022 chr_56 & Abou Zeid (kabouzeid) (original author)
 */

package player.phonograph.ui.fragments.pages

import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import player.phonograph.ui.fragments.AbsMusicServiceFragment
import player.phonograph.ui.fragments.HomeFragment

// todo no more AbsMusicServiceFragment
abstract class AbsPage : AbsMusicServiceFragment() {

    protected val hostFragment: HomeFragment
        get() = parentFragment?.let { it as HomeFragment } ?: throw IllegalStateException("${this::class.simpleName} hasn't attach to HomeFragment")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hostFragment.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)
                    @Suppress("DEPRECATION") //todo: replace `setHasOptionsMenu` with MenuProvider
                    setHasOptionsMenu(true)
                }
            }
        )
    }

    open fun onBackPress(): Boolean = false
}
