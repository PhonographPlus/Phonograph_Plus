/*
 *  Copyright (c) 2022~2025 chr_56
 */

package player.phonograph.model.metadata

data class FileProperties(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val dateAdded: Long,
    val dateModified: Long,
) : Metadata {

    override fun get(key: Metadata.Key): Metadata.Field? =
        when (key) {
            Key.Name         -> Metadata.TextualField(fileName)
            Key.Path         -> Metadata.TextualField(fileName)
            Key.Size         -> Metadata.NumericField(fileSize, NOTATION_DATA_SIZE)
            Key.DateAdded    -> Metadata.NumericField(dateAdded, NOTATION_TIMESTAMP)
            Key.DateModified -> Metadata.NumericField(dateModified, NOTATION_TIMESTAMP)
            else             -> null
        }

    override fun contains(key: Metadata.Key): Boolean = key is Key

    override val fields: List<Metadata.Entry>
        get() = listOf(
            Metadata.PlainEntry(Key.Name, Metadata.TextualField(fileName)),
            Metadata.PlainEntry(Key.Path, Metadata.TextualField(fileName)),
            Metadata.PlainEntry(Key.Size, Metadata.NumericField(fileSize, NOTATION_TIMESTAMP)),
            Metadata.PlainEntry(Key.DateAdded, Metadata.NumericField(dateAdded, NOTATION_TIMESTAMP)),
            Metadata.PlainEntry(Key.DateModified, Metadata.NumericField(dateModified, NOTATION_TIMESTAMP)),
        )

    enum class Key : Metadata.Key {
        Name,
        Path,
        Size,
        DateAdded,
        DateModified,
        ;
    }
}