/*
 *  Copyright (c) 2022~2024 chr_56
 */

package player.phonograph.ui.modules.explorer

import player.phonograph.model.file.FileEntity
import player.phonograph.model.file.Location
import player.phonograph.repo.mediastore.loaders.FileEntityLoader
import android.content.Context
import kotlinx.coroutines.CoroutineScope

class FilesChooserViewModel : AbsFileViewModel() {

    override suspend fun listFiles(context: Context, location: Location, scope: CoroutineScope?): Set<FileEntity> =
        FileEntityLoader.listFilesLegacy(location, scope)
}
