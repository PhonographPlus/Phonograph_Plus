/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.ui.dialogs

import player.phonograph.ui.adapter.SortableListAdapter
import player.phonograph.mechanism.backup.ALL_BACKUP_CONFIG
import player.phonograph.mechanism.backup.BackupItem
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class BackupChooserAdapter(
    private val config: List<BackupItem>,
    private val all: List<BackupItem> = ALL_BACKUP_CONFIG,
) : SortableListAdapter<BackupItem>() {

    override fun fetchDataset(): SortableList<BackupItem> {
        val disabled = all.toMutableList().also { it.removeAll(config) }
        return SortableList(
            config.map { SortableList.Item(it, true) } + disabled.map { SortableList.Item(it, false) }
        )
    }

    override fun onCreateContentView(parent: ViewGroup, viewType: Int): View {
        return TextView(parent.context).apply {
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
            textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        }
    }

    override fun onBindContentView(contentView: View, position: Int) {
        require(contentView is TextView) { "Receive ${contentView.javaClass.name}" }
        contentView.text = dataset.items[position].content.displayName(contentView.resources)
    }

    val currentConfig: List<BackupItem>
        get() = dataset.items.filter { it.checked }.map { it.content }

}