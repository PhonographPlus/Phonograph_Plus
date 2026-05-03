/*
 *  Copyright (c) 2022~2026 chr_56
 */

package player.phonograph.service

import org.koin.dsl.module
import player.phonograph.service.queue.MusicPlaybackQueueStore
import player.phonograph.service.queue.QueueManager

val moduleQueue = module {
    single { MusicPlaybackQueueStore(get()) }
    single { QueueManager(get()) }
}