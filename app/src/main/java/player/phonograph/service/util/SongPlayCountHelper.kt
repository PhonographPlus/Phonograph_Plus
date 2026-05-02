/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.service.util

import player.phonograph.model.Song
import player.phonograph.repo.database.domain.DynamicTracks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author Abou Zeid (kabouzeid)
 */
class SongPlayCountHelper {

    private val stopWatch: StopWatch = StopWatch()
    var songMonitored: Song? = null
        set(song) {
            synchronized(this) {
                stopWatch.reset()
                field = song
            }
        }

    fun notifyPlayStateChanged(isPlaying: Boolean) {
        synchronized(this) {
            if (isPlaying) {
                stopWatch.start()
            } else {
                stopWatch.pause()
            }
        }
    }

    fun checkForBumpingPlayCount(coroutineScope: CoroutineScope) {
        synchronized(this) {
            val song = songMonitored
            if (song != null && stopWatch.elapsedTime > song.duration * 0.5) {
                coroutineScope.launch(Dispatchers.IO) {
                    DynamicTracks.TopTracks.bump(song.id)
                }
            }
        }
    }

    /**
     * Simple thread safe stop watch.
     *
     * @author Karim Abou Zeid (kabouzeid)
     */
    class StopWatch {
        /**
         * The time the stop watch was last started.
         */
        private var startTime: Long = 0

        /**
         * The time elapsed before the current [.startTime].
         */
        private var previousElapsedTime: Long = 0

        /**
         * Whether the stop watch is currently running or not.
         */
        private var isRunning = false

        /**
         * Starts or continues the stop watch.
         *
         * @see .pause
         * @see .reset
         */
        fun start() {
            synchronized(this) {
                startTime = System.currentTimeMillis()
                isRunning = true
            }
        }

        /**
         * Pauses the stop watch. It can be continued later from [.start].
         *
         * @see .start
         * @see .reset
         */
        fun pause() {
            synchronized(this) {
                previousElapsedTime += System.currentTimeMillis() - startTime
                isRunning = false
            }
        }

        /**
         * Stops and resets the stop watch to zero milliseconds.
         *
         * @see .start
         * @see .pause
         */
        fun reset() {
            synchronized(this) {
                startTime = 0
                previousElapsedTime = 0
                isRunning = false
            }
        }

        /**
         * @return the total elapsed time in milliseconds
         */
        val elapsedTime: Long
            get() {
                synchronized(this) {
                    var currentElapsedTime: Long = 0
                    if (isRunning) {
                        currentElapsedTime = System.currentTimeMillis() - startTime
                    }
                    return previousElapsedTime + currentElapsedTime
                }
            }

        override fun toString(): String = "$elapsedTime millis"
    }
}