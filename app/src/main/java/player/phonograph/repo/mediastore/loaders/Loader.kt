/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.repo.mediastore.loaders

import android.content.Context

interface Loader<T> {
    suspend fun all(context: Context): List<T>
    suspend fun id(context: Context, id: Long): T?
}