/*
 * Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.mechanism.migrate

import player.phonograph.coil.CustomArtistImageStore
import player.phonograph.settings.Keys
import player.phonograph.settings.PrerequisiteSetting
import player.phonograph.settings.PrimitiveKey
import player.phonograph.settings.Setting
import player.phonograph.util.debug
import player.phonograph.util.file.moveFile
import player.phonograph.util.reportError
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FilenameFilter

fun migrate(context: Context, from: Int, to: Int) {

    when (from) {
        in 1 until 1000    -> { // v1.0.0
            throw IllegalStateException("You are upgrading from a very old version (version $from)! Please Wipe app data!")
        }

        in 1000 until 1040 -> { // v1.4.0
            reportError(
                IllegalStateException(), TAG,
                "You are upgrading from a very old version (version $from)! Try to wipe app data!"
            )
        }
    }

    if (from != to) {
        Log.i(TAG, "Start Migration: $from -> $to")

        MigrateOperator(context, from, to).apply {
            migrate(AutoDownloadMetadataMigration())
            migrate(LegacyLastAddedCutoffIntervalMigration())
            migrate(CustomArtistImageStoreMigration())
            migrate(ThemeStoreMigration())
            migrate(GeneralThemeMigration())
            migrate(LegacyDetailDialogMigration())
            migrate(PlaylistFilesOperationBehaviourMigration())
        }

        Log.i(TAG, "End Migration")

        PrerequisiteSetting.instance(context).previousVersion = to
    } else {
        debug {
            Log.i(TAG, "No Need to Migrate")
        }
    }
}

/**
 * Migration Rule
 */
private abstract class Migration(
    val introduced: Int,
    val deprecated: Int = Int.MAX_VALUE,
) {

    /**
     * actual codes that operate migrations
     */
    abstract fun doMigrate(context: Context)

    /**
     * check condition of migrate
     */
    fun check(from: Int, to: Int): Boolean {
        return from <= to && from != -1 && introduced in from + 1..to
    }

    fun tryMigrate(context: Context, from: Int, to: Int) {
        if (check(from, to)) {
            doMigrate(context)
            Log.i(TAG, "Migrating: ${javaClass.simpleName}")
        }
    }
}


private class MigrateOperator(
    private val context: Context,
    private val from: Int,
    private val to: Int,
) {
    fun migrate(migration: Migration) =
        migration.tryMigrate(context, from, to)
}

private class AutoDownloadMetadataMigration : Migration(introduced = 1011) {
    override fun doMigrate(context: Context) {
        removePreference(context, keyName = DeprecatedPreference.AutoDownloadMetadata.AUTO_DOWNLOAD_IMAGES_POLICY)
    }
}

private class LegacyLastAddedCutoffIntervalMigration : Migration(introduced = 1011) {
    override fun doMigrate(context: Context) {
        removePreference(context, keyName = DeprecatedPreference.LegacyLastAddedCutoffInterval.LEGACY_LAST_ADDED_CUTOFF)
    }
}

/**
 * Custom Artist images have been moved to external storage from internal storage
 */
private class CustomArtistImageStoreMigration : Migration(introduced = 1053) {
    override fun doMigrate(context: Context) {
        val newLocation = CustomArtistImageStore.directory(context) ?: return // no external storage
        val oldLocation = CustomArtistImageStore.directoryFallback(context)
        if (!oldLocation.exists()) oldLocation.mkdirs()
        try {
            val files: Array<File> = oldLocation.listFiles(imageNameFilter) ?: return // empty
            for (file in files) {
                moveFile(file, File(newLocation, file.name))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val imageNameFilter = FilenameFilter { _, name -> name.endsWith("jpeg") }
}


private class ThemeStoreMigration : Migration(introduced = 1064) {
    @SuppressLint("ApplySharedPref")
    override fun doMigrate(context: Context) {
        @Suppress("LocalVariableName")
        val Old = DeprecatedPreference.ThemeColorKeys
        val pref = context.getSharedPreferences(
            Old.THEME_CONFIG_PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        CoroutineScope(Dispatchers.IO).launch {
            if (pref.getBoolean(Old.KEY_IS_CONFIGURED, false)) {
                with(context) {
                    migrateIntPreferenceToDataStore(pref, Old.KEY_PRIMARY_COLOR, Keys.selectedPrimaryColor)
                    migrateIntPreferenceToDataStore(pref, Old.KEY_ACCENT_COLOR, Keys.selectedAccentColor)
                    migrateIntPreferenceToDataStore(pref, Old.KEY_MONET_PRIMARY_COLOR, Keys.monetPalettePrimaryColor)
                    migrateIntPreferenceToDataStore(pref, Old.KEY_MONET_ACCENT_COLOR, Keys.monetPaletteAccentColor)
                    migrateBooleanPreferenceToDataStore(pref, Old.KEY_COLORED_NAVIGATION_BAR, Keys.coloredNavigationBar)
                    migrateBooleanPreferenceToDataStore(pref, Old.KEY_ENABLE_MONET, Keys.enableMonet)
                }
            }
            pref.edit().clear().commit()
            delay(100)
            deleteSharedPreferences(context, Old.THEME_CONFIG_PREFERENCE_NAME)
        }
    }
}

private class GeneralThemeMigration : Migration(introduced = 1064) {
    @SuppressLint("ApplySharedPref")
    override fun doMigrate(context: Context) {
        @Suppress("LocalVariableName")
        val Old = DeprecatedPreference.StyleConfigKeys
        val pref = context.getSharedPreferences(
            Old.PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        CoroutineScope(Dispatchers.IO).launch {
            context.migrateStringPreferenceToDataStore(pref, Old.KEY_THEME, Keys.theme)
            pref.edit().clear().commit()
            delay(100)
            deleteSharedPreferences(context, Old.PREFERENCE_NAME)
            // change "auto"
            delay(100)
            Setting.settingsDatastore(context).edit { preferences ->
                val theme = preferences[Keys.theme.preferenceKey]
                if (theme == "auto") {
                    preferences[Keys.theme.preferenceKey] = Keys.theme.defaultValue()
                }
            }
        }
    }
}

private class LegacyDetailDialogMigration : Migration(introduced = 1081) {
    override fun doMigrate(context: Context) {
        removePreference(context, DeprecatedPreference.LegacyDetailDialog.USE_LEGACY_DETAIL_DIALOG)
    }
}

private class PlaylistFilesOperationBehaviourMigration : Migration(introduced = 1085) {
    override fun doMigrate(context: Context) {
        removePreference(
            context,
            DeprecatedPreference.PlaylistFilesOperationBehaviour.PLAYLIST_FILES_OPERATION_BEHAVIOUR
        )
    }
}

fun deleteSharedPreferences(context: Context, fileName: String) {
    val sharedPreferencesFile = File(context.applicationInfo.dataDir + "/shared_prefs/" + fileName + ".xml")
    if (sharedPreferencesFile.exists()) {
        sharedPreferencesFile.delete()
    }
}

private suspend fun Context.migrateIntPreferenceToDataStore(
    oldPreference: SharedPreferences,
    oldKeyName: String,
    newKeyName: PrimitiveKey<Int>,
) {
    try {
        val value = oldPreference.getInt(oldKeyName, -1)
        if (value != -1) {
            Setting.settingsDatastore(this).edit {
                it[newKeyName.preferenceKey] = value
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to migrate int preference $oldKeyName", e)
    }
}

private suspend fun Context.migrateBooleanPreferenceToDataStore(
    oldPreference: SharedPreferences,
    oldKeyName: String,
    newKeyName: PrimitiveKey<Boolean>,
) {
    try {
        val value = oldPreference.getBoolean(oldKeyName, false)
        Setting.settingsDatastore(this).edit { it[newKeyName.preferenceKey] = value }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to migrate bool preference $oldKeyName", e)
    }
}

private suspend fun Context.migrateStringPreferenceToDataStore(
    oldPreference: SharedPreferences,
    oldKeyName: String,
    newKeyName: PrimitiveKey<String>,
) {
    try {
        val value = oldPreference.getString(oldKeyName, null)
        if (value != null) {
            Setting.settingsDatastore(this).edit {
                it[newKeyName.preferenceKey] = value
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to migrate int preference $oldKeyName", e)
    }
}

private fun moveIntPreference(
    oldPreference: SharedPreferences,
    oldKeyName: String,
    newPreference: SharedPreferences,
    newKeyName: String,
) {
    try {
        val value = oldPreference.getInt(oldKeyName, 0)
        newPreference.edit().putInt(newKeyName, value).apply()
        oldPreference.edit().remove(oldKeyName).apply()
        Log.i(TAG, "Success: $oldKeyName -> $newKeyName")
    } catch (e: Exception) {
        Log.i(TAG, "Fail: $oldKeyName -> $newKeyName")
    }
}


private fun removePreference(context: Context, keyName: String) {
    var type: Int = -1
    var exception: Exception? = null
    try {
        CoroutineScope(SupervisorJob()).launch {
            Setting.settingsDatastore(context).edit {
                val booleanKey = booleanPreferencesKey(keyName)
                val stringKey = stringPreferencesKey(keyName)
                val intKey = intPreferencesKey(keyName)
                val longKey = intPreferencesKey(keyName)
                val keys: List<Preferences.Key<*>> = listOf(booleanKey, stringKey, intKey, longKey)
                for (key in keys) {
                    if (it.contains(key)) {
                        it.remove(key)
                        break
                    }
                }
            }
        }
    } catch (e: Exception) {
        exception = e
        type = DATASTORE
    }
    try {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        pref.edit().remove(keyName).apply()
    } catch (e: Exception) {
        exception = e
        type = PREFERENCE
    }
    if (exception != null) {
        reportError(
            exception,
            TAG,
            "Failed to remove legacy preference item `$keyName` via ${if (type == DATASTORE) "datastore" else "preference"}"
        )
    }
}

private const val PREFERENCE = 1
private const val DATASTORE = 2

private const val TAG = "VersionMigrate"

object DeprecatedPreference {
    // "removed since version code 101"
    const val LIBRARY_CATEGORIES = "library_categories"

    // "removed since version code 210"
    object SortOrder {
        const val ARTIST_SORT_ORDER = "artist_sort_order"
        const val ARTIST_SONG_SORT_ORDER = "artist_song_sort_order"
        const val ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order"
        const val ALBUM_SORT_ORDER = "album_sort_order"
        const val ALBUM_SONG_SORT_ORDER = "album_song_sort_order"
        const val SONG_SORT_ORDER = "song_sort_order"
        const val GENRE_SORT_ORDER = "genre_sort_order"
    }

    // "removed since version code 262"
    object MusicChooserPreference {
        const val LAST_MUSIC_CHOOSER = "last_music_chooser"
    }

    // "removed since version code 402"
    object LegacyClickPreference {
        const val REMEMBER_SHUFFLE = "remember_shuffle"
        const val KEEP_PLAYING_QUEUE_INTACT = "keep_playing_queue_intact"
    }

    // "move to a separate preference since 460"
    object QueueCfg {
        const val PREF_POSITION = "POSITION"
        const val PREF_SHUFFLE_MODE = "SHUFFLE_MODE"
        const val PREF_REPEAT_MODE = "REPEAT_MODE"
        const val PREF_POSITION_IN_TRACK = "POSITION_IN_TRACK"
    }

    // "remove lockscreen cover since 522"
    object LockScreenCover {
        const val ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen"
        const val BLURRED_ALBUM_ART = "blurred_album_art"
    }


    // "removed Auto Download Metadata from last.fm since version code 1011"
    object AutoDownloadMetadata {
        const val AUTO_DOWNLOAD_IMAGES_POLICY = "auto_download_images_policy"
        const val DOWNLOAD_IMAGES_POLICY_ALWAYS = "always"
        const val DOWNLOAD_IMAGES_POLICY_ONLY_WIFI = "only_wifi"
        const val DOWNLOAD_IMAGES_POLICY_NEVER = "never"
    }

    // "replaced with the flexible one since version code 1011"
    object LegacyLastAddedCutoffInterval {
        const val LEGACY_LAST_ADDED_CUTOFF = "last_added_interval"
        const val INTERVAL_TODAY = "today"
        const val INTERVAL_PAST_SEVEN_DAYS = "past_seven_days"
        const val INTERVAL_PAST_FOURTEEN_DAYS = "past_fourteen_days"
        const val INTERVAL_PAST_ONE_MONTH = "past_one_month"
        const val INTERVAL_PAST_THREE_MONTHS = "past_three_months"
        const val INTERVAL_THIS_WEEK = "this_week"
        const val INTERVAL_THIS_MONTH = "this_month"
        const val INTERVAL_THIS_YEAR = "this_year"
    }

    // "migrate to datastore since version code 1064"
    object ThemeColorKeys {
        const val THEME_CONFIG_PREFERENCE_NAME = "theme_color_cfg"
        const val KEY_IS_CONFIGURED = "is_configured"
        const val KEY_VERSION = "is_configured_version"
        const val KEY_LAST_EDIT_TIME = "values_changed"
        const val KEY_PRIMARY_COLOR = "primary_color"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_COLORED_STATUSBAR = "apply_primarydark_statusbar"
        const val KEY_COLORED_NAVIGATION_BAR = "apply_primary_navbar"
        const val KEY_ENABLE_MONET = "enable_monet"
        const val KEY_MONET_PRIMARY_COLOR = "monet_primary_color"
        const val KEY_MONET_ACCENT_COLOR = "monet_accent_color"
    }

    // "migrate to datastore since version code 1064"
    object StyleConfigKeys {
        const val PREFERENCE_NAME = "style_config"
        const val KEY_THEME = "theme"
    }

    // "remove fallback since 1081"
    object LegacyDetailDialog {
        const val USE_LEGACY_DETAIL_DIALOG = "use_legacy_detail_dialog"
    }

    // "removed since 1085"
    object PlaylistFilesOperationBehaviour {
        const val PLAYLIST_FILES_OPERATION_BEHAVIOUR = "playlist_files_operation_behaviour"
    }
}