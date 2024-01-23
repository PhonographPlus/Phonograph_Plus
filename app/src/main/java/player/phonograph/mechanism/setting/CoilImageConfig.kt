/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.mechanism.setting

import player.phonograph.App
import player.phonograph.model.config.ImageSourceConfig
import player.phonograph.settings.Keys
import player.phonograph.settings.Setting
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

object CoilImageConfig {

    var currentImageSourceConfig: ImageSourceConfig
        get() {
            val rawString = Setting(App.instance)[Keys.imageSourceConfigJsonString].data
            val config: ImageSourceConfig = try {
                readFromJson(rawString)
            } catch (e: SerializationException) {
                Log.e(TAG, "Glitch ImageSourceConfig: $rawString", e)
                resetImageSourceToDefault()
                // return default
                ImageSourceConfig.DEFAULT
            }
            return if (checkInvalid(config)) {
                config
            } else {
                resetImageSourceToDefault()
                ImageSourceConfig.DEFAULT
            }
        }
        set(value) {
            val json = writeToJson(value) as JsonObject
            Setting(App.instance)[Keys.imageSourceConfigJsonString].data = json.toString()
        }

    fun resetImageSourceToDefault() {
        currentImageSourceConfig = ImageSourceConfig.DEFAULT
    }

    private fun writeToJson(config: ImageSourceConfig): JsonElement {
        val parser = Json {
            encodeDefaults = true
        }
        return parser.encodeToJsonElement(config)
    }

    private fun readFromJson(string: String): ImageSourceConfig {
        val parser = Json {
            ignoreUnknownKeys = true
        }
        return parser.decodeFromString(string)
    }

    private fun readFromJson(jsonElement: JsonElement): ImageSourceConfig {
        val parser = Json {
            ignoreUnknownKeys = true
        }
        return parser.decodeFromJsonElement(jsonElement)
    }

    /**
     * @return true if correct
     */
    private fun checkInvalid(config: ImageSourceConfig): Boolean {
        if (config.sources.isEmpty()) {
            Log.e(TAG, "Illegal ImageSourceConfig: $config")
            return false
        }
        return true
    }

    private const val TAG = "ImageSource"
}