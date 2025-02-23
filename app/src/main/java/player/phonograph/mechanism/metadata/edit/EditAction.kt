/*
 *  Copyright (c) 2022~2025 chr_56
 */

package player.phonograph.mechanism.metadata.edit

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagNotFoundException
import java.io.File

sealed interface EditAction {

    val key: FieldKey

    val description: String

    fun valid(audioFile: AudioFile): ValidResult

    data class Delete(override val key: FieldKey) : EditAction {
        override val description: String get() = "Delete $key"
        override fun valid(audioFile: AudioFile): ValidResult {
            val tag = audioFile.tag ?: return ValidResult.NoSuchKey
            val target = try {
                tag.getFirst(key)
            } catch (e: TagNotFoundException) {
                null
            }
            return when (target) {
                null -> ValidResult.NoSuchKey
                else -> ValidResult.Valid
            }
        }
    }

    object ImageDelete : EditAction {
        override val key: FieldKey = FieldKey.COVER_ART
        override val description: String get() = "Delete Cover Art"
        override fun valid(audioFile: AudioFile): ValidResult {
            val size = audioFile.tag?.artworkList?.size ?: 0
            return if (size > 0) ValidResult.Valid else ValidResult.NoSuchKey
        }
    }

    data class Update(override val key: FieldKey, val newValue: String) : EditAction {
        override val description: String get() = "Update $key to $newValue"
        override fun valid(audioFile: AudioFile): ValidResult {
            val tag = audioFile.tag ?: return ValidResult.NoSuchKey
            val target = try {
                tag.getFirst(key)
            } catch (e: TagNotFoundException) {
                null
            }
            return when (target) {
                null     -> ValidResult.NoSuchKey
                newValue -> ValidResult.NoChange
                else     -> ValidResult.Valid
            }
        }
    }

    class ImageReplace(val file: File) : EditAction {
        override val key: FieldKey = FieldKey.COVER_ART
        override val description: String get() = "Replace Cover Art to ${file.path}"
        override fun valid(audioFile: AudioFile): ValidResult {
            return ValidResult.Valid
        }
    }


    sealed class ValidResult {
        abstract val message: String

        object Valid : ValidResult() {
            override val message: String get() = "Valid"
        }

        object NoChange : ValidResult() {
            override val message: String get() = "No changes are made"
        }

        object NoSuchKey : ValidResult() {
            override val message: String get() = "Key not found"
        }

        object AlreadyExisted : ValidResult() {
            override val message: String get() = "Already existed"
        }

        object ReadOnly : ValidResult() {
            override val message: String get() = "Read only file"
        }
    }

    companion object {
        private const val TAG = "EditAction"

        /**
         * remove duplicated
         */
        fun merge(original: List<EditAction>): MutableList<EditAction> {
            val result = original.toMutableList()
            result.sortBy { it.key }
            var completed = false
            outer@ while (!completed) {
                var index = 0
                inner@ while (index + 1 <= result.lastIndex) {
                    val u = result[index]
                    val b = result[index + 1]
                    if (u.key == b.key) { // override check
                        result.removeAt(index)
                        continue@outer
                    }
                    index++
                }
                if (index + 1 >= result.lastIndex) completed = true
            }
            return result
        }
    }

}