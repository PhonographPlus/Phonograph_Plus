/*
 *  Copyright (c) 2022~2023 chr_56
 */

package util.phonograph.tagsources

interface QueryParameter {
    fun check(): Boolean
    fun toAction(): Action
}

