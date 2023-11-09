/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.repo.database

import org.koin.core.context.GlobalContext
import player.phonograph.mechanism.event.MediaStoreTracker
import player.phonograph.util.debug
import player.phonograph.util.file.safeGetCanonicalPath
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Environment.DIRECTORY_ALARMS
import android.os.Environment.DIRECTORY_NOTIFICATIONS
import android.os.Environment.DIRECTORY_RINGTONES
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import java.io.File

class PathFilterStore(context: Context) :
        SQLiteOpenHelper(context, DatabaseConstants.PATH_FILTER, null, VERSION) {

    init {
        val databaseFile = context.getDatabasePath(DatabaseConstants.PATH_FILTER)
        if (!databaseFile.exists()) {
            debug {
                Log.v(DatabaseConstants.PATH_FILTER, "PathFilter database doesn't not existed, initializing...")
            }
            // blacklisted by default
            addPathImpl(getExternalStoragePublicDirectory(DIRECTORY_ALARMS), TABLE_BLACKLIST)
            addPathImpl(getExternalStoragePublicDirectory(DIRECTORY_NOTIFICATIONS), TABLE_BLACKLIST)
            addPathImpl(getExternalStoragePublicDirectory(DIRECTORY_RINGTONES), TABLE_BLACKLIST)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_BLACKLIST ($PATH TEXT NOT NULL);"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS $TABLE_WHITELIST ($PATH TEXT NOT NULL);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    val blacklistPaths: List<String>
        get() = fetch(TABLE_BLACKLIST)

    val whitelistPaths: List<String>
        get() = fetch(TABLE_WHITELIST)

    private fun fetch(table: String): List<String> {
        val paths: MutableList<String> = ArrayList()
        readableDatabase.query(
            table, arrayOf(PATH), null, null, null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    paths.add(cursor.getString(0))
                } while (cursor.moveToNext())
            }
        }
        return paths
    }


    fun addBlacklistPath(file: File) {
        addPathImpl(file, TABLE_BLACKLIST)
        notifyMediaStoreChanged()
    }

    fun addWhitelistPath(file: File) {
        addPathImpl(file, TABLE_WHITELIST)
        notifyMediaStoreChanged()
    }

    fun addBlacklistPath(path: String) {
        addPathImpl(path, TABLE_BLACKLIST)
        notifyMediaStoreChanged()
    }

    fun addWhitelistPath(path: String) {
        addPathImpl(path, TABLE_WHITELIST)
        notifyMediaStoreChanged()
    }


    fun addBlacklistPath(paths: List<String>) {
        paths.forEach { addPathImpl(it, TABLE_BLACKLIST) }
        notifyMediaStoreChanged()
    }

    fun addWhitelistPath(paths: List<String>) {
        paths.forEach { addPathImpl(it, TABLE_WHITELIST) }
        notifyMediaStoreChanged()
    }


    fun removeBlacklistPath(file: File) {
        removePathImpl(file, TABLE_BLACKLIST)
        notifyMediaStoreChanged()
    }

    fun removeWhitelistPath(file: File) {
        removePathImpl(file, TABLE_WHITELIST)
        notifyMediaStoreChanged()
    }

    fun removeBlacklistPath(path: String) {
        removePathImpl(path, TABLE_BLACKLIST)
        notifyMediaStoreChanged()
    }

    fun removeWhitelistPath(path: String) {
        removePathImpl(path, TABLE_WHITELIST)
        notifyMediaStoreChanged()
    }

    fun removeBlacklistPath(paths: List<String>) {
        paths.forEach { removePathImpl(it, TABLE_BLACKLIST) }
        notifyMediaStoreChanged()
    }

    fun removeWhitelistPath(paths: List<String>) {
        paths.forEach { removePathImpl(it, TABLE_WHITELIST) }
        notifyMediaStoreChanged()
    }

    fun clearBlacklist() {
        clearTable(TABLE_BLACKLIST)
        notifyMediaStoreChanged()
    }

    fun clearWhitelist() {
        clearTable(TABLE_WHITELIST)
        notifyMediaStoreChanged()
    }


    private fun addPathImpl(file: File, table: String) {
        if (contains(file, table)) return
        val path = safeGetCanonicalPath(file)
        addPathImpl(path, table)
    }

    private fun addPathImpl(path: String, table: String) {
        if (contains(path, table)) return
        with(writableDatabase) {
            beginTransaction()
            try {
                // add the entry
                insert(
                    table, null,
                    ContentValues(1).apply {
                        put(PATH, path)
                    }
                )
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
        }
    }

    private fun removePathImpl(file: File, table: String) =
        removePathImpl(safeGetCanonicalPath(file), table)

    private fun removePathImpl(path: String, table: String) {
        writableDatabase.delete(
            table, "$PATH = ?", arrayOf(path)
        )
    }

    internal fun contains(file: File, table: String): Boolean =
        contains(safeGetCanonicalPath(file), table)

    internal fun contains(path: String, table: String): Boolean {
        return readableDatabase.query(
            table, arrayOf(PATH),
            "$PATH = ?", arrayOf(path),
            null, null, null, null
        )?.use {
            it.moveToFirst()
        } ?: false
    }


    private fun clearTable(table: String) {
        writableDatabase.delete(table, null, null)
    }


    companion object {
        private const val VERSION = 2

        const val TABLE_BLACKLIST = "blacklist"
        const val TABLE_WHITELIST = "whitelist"
        const val PATH = "path"

        fun get() = GlobalContext.get().get<PathFilterStore>()

        private fun notifyMediaStoreChanged() = GlobalContext.get().get<MediaStoreTracker>().notifyAllListeners()
    }
}
