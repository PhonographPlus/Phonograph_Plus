/*
 *  Copyright (c) 2022~2025 chr_56
 */

package player.phonograph.model.coil

import androidx.annotation.StringDef

const val IMAGE_SOURCE_MEDIA_STORE = "MediaStore"
const val IMAGE_SOURCE_MEDIA_METADATA_RETRIEVER = "MediaMetadataRetriever"
const val IMAGE_SOURCE_J_AUDIO_TAGGER = "JAudioTagger"
const val IMAGE_SOURCE_EXTERNAL_FILE = "ExternalFile"

@StringDef(
    value = [
        IMAGE_SOURCE_MEDIA_STORE,
        IMAGE_SOURCE_MEDIA_METADATA_RETRIEVER,
        IMAGE_SOURCE_J_AUDIO_TAGGER,
        IMAGE_SOURCE_EXTERNAL_FILE,
    ],
)
@Retention(AnnotationRetention.SOURCE)
annotation class ImageSourceType


enum class ImageSource(@get:ImageSourceType @field:ImageSourceType val key: String) {
    MediaStore(IMAGE_SOURCE_MEDIA_STORE),
    MediaMetadataRetriever(IMAGE_SOURCE_MEDIA_METADATA_RETRIEVER),
    JAudioTagger(IMAGE_SOURCE_J_AUDIO_TAGGER),
    ExternalFile(IMAGE_SOURCE_EXTERNAL_FILE),
    ;

    companion object {
        fun fromKey(@ImageSourceType key: String): ImageSource = when (key) {
            IMAGE_SOURCE_MEDIA_STORE              -> MediaStore
            IMAGE_SOURCE_MEDIA_METADATA_RETRIEVER -> MediaMetadataRetriever
            IMAGE_SOURCE_J_AUDIO_TAGGER           -> JAudioTagger
            IMAGE_SOURCE_EXTERNAL_FILE            -> ExternalFile
            else                                  -> throw IllegalArgumentException("Unknown ImageSource: $key")
        }
    }
}