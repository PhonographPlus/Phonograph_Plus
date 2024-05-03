/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.ui.activities.base

import player.phonograph.ui.fragments.player.AbsPlayerFragment
import player.phonograph.ui.fragments.player.MiniPlayerFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class PanelViewModel(
    initialActivityColor: Int,
    initialHighlightColor: Int,
    val defaultColor: Int,
) : ViewModel() {

    // original color of this activity
    private val _activityColor: MutableStateFlow<Int> = MutableStateFlow(initialActivityColor)
    val activityColor get() = _activityColor.asStateFlow()

    fun updateActivityColor(newColor: Int) {
        viewModelScope.launch { _activityColor.emit(newColor) }
    }

    private val _highlightColor: MutableStateFlow<Int> = MutableStateFlow(initialHighlightColor)
    val highlightColor get() = _highlightColor.asStateFlow()

    private val _previousHighlightColor: MutableStateFlow<Int> = MutableStateFlow(initialHighlightColor)
    val previousHighlightColor get() = _previousHighlightColor.asStateFlow()

    fun updateHighlightColor(newColor: Int) {
        viewModelScope.launch {
            val oldColor = _highlightColor.value
            _previousHighlightColor.emit(oldColor)
            _highlightColor.emit(newColor)
        }
    }

    private var _transparentStatusbar: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val transparentStatusbar get() = _transparentStatusbar.value

    fun enableTransparentStatusbar(enable: Boolean) {
        _transparentStatusbar.update { enable }
    }

    val playerFragment: WeakReference<AbsPlayerFragment?> = WeakReference(null)
    val miniPlayerFragment: WeakReference<MiniPlayerFragment?> = WeakReference(null)


}