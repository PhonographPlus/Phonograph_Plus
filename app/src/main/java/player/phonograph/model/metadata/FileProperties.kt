/*
 *  Copyright (c) 2022~2025 chr_56
 */

package player.phonograph.model.metadata

import player.phonograph.R

data class FileProperties(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
) : Metadata {

    override fun get(key: Metadata.Key): Metadata.Field? =
        when (key) {
            is Keys.Name -> Metadata.PlainStringField(fileName)
            is Keys.Path -> Metadata.PlainStringField(fileName)
            is Keys.Size -> Metadata.PlainNumberField(fileSize)
            else         -> null
        }

    override fun contains(key: Metadata.Key): Boolean = key is FilePropertiesKey

    override val fields: List<Metadata.Entry>
        get() = listOf(
            Metadata.PlainEntry(Keys.Name, Metadata.PlainStringField(fileName)),
            Metadata.PlainEntry(Keys.Path, Metadata.PlainStringField(fileName)),
            Metadata.PlainEntry(Keys.Size, Metadata.PlainNumberField(fileSize)),
        )

    sealed interface FilePropertiesKey : Metadata.Key

    object Keys {

        data object Name : FilePropertiesKey {
            override val res: Int = R.string.label_file_name
        }

        data object Path : FilePropertiesKey {
            override val res: Int = R.string.label_file_path
        }

        data object Size : FilePropertiesKey {
            override val res: Int = R.string.label_file_size
        }
    }
}