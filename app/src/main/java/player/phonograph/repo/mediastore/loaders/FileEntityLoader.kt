/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.repo.mediastore.loaders

import player.phonograph.mechanism.scanner.FileScanner
import player.phonograph.model.file.FileEntity
import player.phonograph.model.file.Location
import player.phonograph.repo.mediastore.internal.querySongFiles
import player.phonograph.repo.mediastore.internal.readFileEntity
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import java.io.File
import java.util.TreeSet

object FileEntityLoader {

    /**
     * list files in [location] as format of FileEntity via MediaStore
     */
    fun listFilesMediaStore(
        currentLocation: Location,
        context: Context,
        scope: CoroutineScope?,
    ): List<FileEntity> {
        val fileCursor = querySongFiles(
            context,
            "${MediaStore.MediaColumns.DATA} LIKE ?",
            arrayOf("${currentLocation.absolutePath}%"),
        ) ?: return emptyList()
        return fileCursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val list: MutableList<FileEntity> = ArrayList()
                do {
                    if (scope?.isActive == false) break
                    val item = readFileEntity(cursor, currentLocation)
                    list.put(item)
                } while (cursor.moveToNext())
                FileEntity.updateSortRef()
                list.sorted()
            } else emptyList()
        }
    }

    private fun MutableList<FileEntity>.put(item: FileEntity) {
        when (item) {
            is FileEntity.File -> {
                this.add(item)
            }
            is FileEntity.Folder -> {
                // count songs for folder
                val i = this.indexOf(item)
                if (i < 0) {
                    this.add(item.apply { songCount = 1 })
                } else {
                    (this[i] as FileEntity.Folder).songCount ++
                }
            }
        }
    }

    /**
     * list files in [location] as format of FileEntity via File API
     */
    fun listFilesLegacy(
        location: Location,
        scope: CoroutineScope?,
    ): List<FileEntity> {
        val directory = File(location.absolutePath).also { if (!it.isDirectory) return emptyList() }
        val files = directory.listFiles(FileScanner.audioFileFilter) ?: return emptyList()
        val result = ArrayList<FileEntity>()
        for (file in files) {
            val l = Location.fromAbsolutePath(file.absolutePath)
            if (scope?.isActive == false) break
            val item =
                when {
                    file.isDirectory -> {
                        FileEntity.Folder(
                            location = l,
                            name = file.name,
                            dateAdded = file.lastModified(),
                            dateModified = file.lastModified()
                        )
                    }

                    file.isFile      -> {
                        FileEntity.File(
                            location = l,
                            name = file.name,
                            size = file.length(),
                            dateAdded = file.lastModified(),
                            dateModified = file.lastModified()
                        )
                    }

                    else             -> null
                }
            item?.let { result.add(it) }
        }
        FileEntity.updateSortRef()
        return result.sorted()
    }
}