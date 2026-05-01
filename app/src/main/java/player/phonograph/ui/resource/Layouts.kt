/*
 *  Copyright (c) 2022~2026 chr_56
 */

package player.phonograph.ui.resource

import player.phonograph.R
import player.phonograph.model.ui.ItemLayoutStyle
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_GRID
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_GRID_CARD_HORIZONTAL
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_LIST
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_LIST_3L
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_LIST_3L_EXTENDED
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_LIST_3L_NO_IMAGE
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_LIST_EXTENDED
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_LIST_NO_IMAGE
import player.phonograph.model.ui.ItemLayoutStyle.Companion.TYPE_LIST_SINGLE_ROW
import player.phonograph.model.ui.ItemLayoutStyle.Companion.ViewHolderType
import androidx.annotation.LayoutRes

object Layouts {

    @LayoutRes
    fun itemLayoutStyle(@ViewHolderType style: ItemLayoutStyle): Int = when (style.ordinal) {
        TYPE_LIST                 -> R.layout.item_list
        TYPE_LIST_EXTENDED        -> R.layout.item_list_extended
        TYPE_LIST_SINGLE_ROW      -> R.layout.item_list_single_row
        TYPE_LIST_NO_IMAGE        -> R.layout.item_list_no_image
        TYPE_LIST_3L              -> R.layout.item_list_3l
        TYPE_LIST_3L_EXTENDED     -> R.layout.item_list_3l_extended
        TYPE_LIST_3L_NO_IMAGE     -> R.layout.item_list_3l_no_image
        TYPE_GRID                 -> R.layout.item_grid
        TYPE_GRID_CARD_HORIZONTAL -> R.layout.item_grid_card_horizontal
        else                      -> R.layout.item_list //default
    }

}