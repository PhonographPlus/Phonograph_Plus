/*
 * Copyright (c) 2022 chr_56
 */

package player.phonograph.coil.audiofile

import coil.map.Mapper
import coil.request.Options
import player.phonograph.coil.model.SongImage
import player.phonograph.model.file.FileEntity
import player.phonograph.repo.loader.Songs
import kotlinx.coroutines.runBlocking
import java.io.File

class FileEntityMapper : Mapper<FileEntity.File, SongImage> {
    override fun map(data: FileEntity.File, options: Options): SongImage? {
        val available =
            runCatching { File(data.location.absolutePath).exists() }.getOrElse { false } // if file is  available
        val song = runBlocking { Songs.id(options.context, data.id) } ?: return null
        return if (available) SongImage.from(song) else null
    }
}
