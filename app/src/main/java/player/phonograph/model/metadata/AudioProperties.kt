/*
 *  Copyright (c) 2022~2025 chr_56
 */

package player.phonograph.model.metadata

data class AudioProperties(
    val audioFormat: String,
    val trackLength: Long,
    val bitRate: Long,
    val samplingRate: Long,
) : Metadata {

    override fun get(key: Metadata.Key): Metadata.Field? =
        when (key) {
            Key.AudioFormat  -> Metadata.TextualField(audioFormat)
            Key.TrackLength  -> Metadata.NumericField(trackLength, NOTATION_DURATION)
            Key.BitRate      -> Metadata.NumericField(bitRate, NOTATION_BIT_RATE)
            Key.SamplingRate -> Metadata.NumericField(samplingRate, NOTATION_SAMPLING)
            else             -> null
        }

    override fun contains(key: Metadata.Key): Boolean = key is Key

    override val fields: List<Metadata.Entry>
        get() = listOf(
            Metadata.PlainEntry(Key.AudioFormat, Metadata.TextualField(audioFormat)),
            Metadata.PlainEntry(Key.TrackLength, Metadata.NumericField(trackLength, NOTATION_DURATION)),
            Metadata.PlainEntry(Key.BitRate, Metadata.NumericField(bitRate, NOTATION_BIT_RATE)),
            Metadata.PlainEntry(Key.SamplingRate, Metadata.NumericField(samplingRate, NOTATION_SAMPLING)),
        )

    enum class Key : Metadata.Key {
        AudioFormat,
        TrackLength,
        BitRate,
        SamplingRate,
        ;
    }
}