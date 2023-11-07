/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.actions.menu

import com.github.chr56.android.menu_dsl.attach
import com.github.chr56.android.menu_dsl.menuItem
import player.phonograph.R
import player.phonograph.actions.actionAddToPlaylist
import player.phonograph.actions.actionDelete
import player.phonograph.actions.actionGotoDetail
import player.phonograph.actions.actionShare
import player.phonograph.actions.fragmentActivity
import player.phonograph.mechanism.PathFilter
import player.phonograph.mechanism.setting.FileConfig
import player.phonograph.misc.MediaScanner
import player.phonograph.model.Song
import player.phonograph.model.file.FileEntity
import player.phonograph.repo.loader.Songs
import player.phonograph.service.MusicPlayerRemote
import player.phonograph.ui.modules.tag.TagBrowserActivity
import player.phonograph.util.lifecycleScopeOrNewOne
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

fun fileEntityPopupMenu(
    context: Context,
    menu: Menu,
    file: FileEntity,
) = context.run {
    attach(menu) {
        menuItem(title = getString(R.string.action_play)) { // id = R.id.action_play
            showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
            onClick {
                action(context, file, MusicPlayerRemote::playNow)
            }
        }
        menuItem(title = getString(R.string.action_play_next)) { // id = R.id.action_play_next
            showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
            onClick {
                action(context, file, MusicPlayerRemote::playNext)
            }
        }
        menuItem(title = getString(R.string.action_add_to_playing_queue)) { // id = R.id.action_add_to_current_playing
            showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
            onClick {
                action(context, file, MusicPlayerRemote::enqueue)
            }
        }
        menuItem(title = getString(R.string.action_add_to_playlist)) { // id = R.id.action_add_to_playlist
            showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
            onClick {
                action(context, file) { it.actionAddToPlaylist(context) } //todo
            }
        }
        when (file) {
            is FileEntity.File   -> {
                menuItem(title = getString(R.string.action_details)) { // id = R.id.action_details
                    showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                    onClick {
                        fragmentActivity(context) { Songs.searchByFileEntity(context, file).actionGotoDetail(it) }
                        true
                    }
                }
                menuItem(title = getString(R.string.action_share)) { // id = R.id.action_share
                    showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                    onClick { Songs.searchByFileEntity(context, file).actionShare(context) }
                }
                menuItem(title = getString(R.string.action_tag_editor)) { //id = R.id.action_tag_editor
                    showAsActionFlag = MenuItem.SHOW_AS_ACTION_IF_ROOM
                    onClick {
                        TagBrowserActivity.launch(context, file.location.absolutePath)
                        true
                    }
                }
            }

            is FileEntity.Folder -> {
                menuItem(title = getString(R.string.action_scan)) {
                    showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                    onClick {
                        scan(context, file)
                        true
                    }
                }
                menuItem(title = getString(R.string.action_set_as_start_directory)) {
                    showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                    onClick {
                        setStartDirectory(context, file)
                    }
                }
                menuItem(title = getString(R.string.action_add_to_black_list)) { // id = R.id.action_add_to_black_list
                    showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
                    onClick {
                        PathFilter.addToBlacklist(context, File(file.location.absolutePath))
                        true
                    }
                }

            }
        }
        menuItem(title = getString(R.string.action_delete_from_device)) { // id = R.id.action_delete_from_device
            showAsActionFlag = MenuItem.SHOW_AS_ACTION_NEVER
            onClick {
                action(context, file) { it.actionDelete(context) } //todo
            }
        }
    }
}

private inline fun action(
    context: Context,
    fileItem: FileEntity,
    block: (List<Song>) -> Boolean,
): Boolean =
    block(
        when (fileItem) {
            is FileEntity.File   -> listOf(Songs.searchByFileEntity(context, fileItem))
            is FileEntity.Folder -> Songs.searchByPath(context, fileItem.location.sqlPattern, false)
        }
    )

private fun scan(context: Context, dir: FileEntity.Folder) {
    context.lifecycleScopeOrNewOne().launch(Dispatchers.IO) {
        val files = File(dir.location.absolutePath).listFiles() ?: return@launch
        val paths: Array<String> = Array(files.size) { files[it].path }

        MediaScanner(context).scan(paths)
    }
}

private fun setStartDirectory(context: Context, dir: FileEntity.Folder): Boolean {
    val path = dir.location.absolutePath
    FileConfig.startDirectory = File(path)
    Toast.makeText(
        context,
        String.format(context.getString(R.string.new_start_directory), path),
        Toast.LENGTH_SHORT
    ).show()
    return true
}