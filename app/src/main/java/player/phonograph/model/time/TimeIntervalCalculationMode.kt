/*
 *  Copyright (c) 2022~2023 chr_56
 */

package player.phonograph.model.time

private const val CONST_PAST = -1
private const val CONST_RECENT = 1
private const val CONST_EVERY = 0

enum class TimeIntervalCalculationMode(val value: Int) {
    PAST(CONST_PAST),
    RECENT(CONST_RECENT),
    EVERY(CONST_EVERY),
    ;

    companion object {
        fun from(value: Int) = when (value) {
            CONST_PAST   -> PAST
            CONST_RECENT -> RECENT
            CONST_EVERY  -> EVERY
            else         -> null
        }
    }
}