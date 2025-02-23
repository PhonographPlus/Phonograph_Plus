/*
 *  Copyright (c) 2022~2025 chr_56
 */

package player.phonograph.model.metadata

import player.phonograph.R

interface MusicMetadata : Metadata {}

object EmptyMusicMetadata : MusicMetadata {

    override fun get(key: Metadata.Key): Metadata.Field? = null

    override fun contains(key: Metadata.Key): Boolean = false

    override val fields: List<Metadata.Entry> get() = listOf()
}

//region Keys

sealed interface MusicMetadataKey : Metadata.Key

/**
 * definitions from JAudioTagger
 */
@Suppress("SpellCheckingInspection")
enum class ConventionalMusicMetadataKey(override val res: Int = 0) : MusicMetadataKey {
    ACOUSTID_FINGERPRINT,
    ACOUSTID_ID,
    ALBUM(R.string.album),
    ALBUM_ARTIST(R.string.album_artist),
    ALBUM_ARTIST_SORT,
    ALBUM_ARTISTS,
    ALBUM_ARTISTS_SORT,
    ALBUM_SORT,
    AMAZON_ID,
    ARRANGER,
    ARRANGER_SORT,
    ARTIST(R.string.artist),
    ARTISTS,
    ARTISTS_SORT,
    ARTIST_SORT,
    BARCODE,
    BPM,
    CATALOG_NO,
    CLASSICAL_CATALOG,
    CLASSICAL_NICKNAME,
    CHOIR,
    CHOIR_SORT,
    COMMENT(R.string.comment),
    COMPOSER(R.string.composer),
    COMPOSER_SORT,
    CONDUCTOR,
    CONDUCTOR_SORT,
    COUNTRY,
    COVER_ART,
    CUSTOM1,
    CUSTOM2,
    CUSTOM3,
    CUSTOM4,
    CUSTOM5,
    DISC_NO(R.string.disk_number),
    DISC_SUBTITLE,
    DISC_TOTAL(R.string.disk_number_total),
    DJMIXER,
    ENCODER,
    ENGINEER,
    ENSEMBLE,
    ENSEMBLE_SORT,
    FBPM,
    GENRE(R.string.genre),
    GROUPING,
    INVOLVED_PERSON,
    ISRC,
    IS_CLASSICAL,
    IS_SOUNDTRACK,
    IS_COMPILATION,
    ITUNES_GROUPING,
    KEY,
    LANGUAGE,
    LYRICIST(R.string.lyricist),
    LYRICS(R.string.lyrics),
    MEDIA,
    MIXER,
    MOOD,
    MOOD_ACOUSTIC,
    MOOD_AGGRESSIVE,
    MOOD_AROUSAL,
    MOOD_DANCEABILITY,
    MOOD_ELECTRONIC,
    MOOD_HAPPY,
    MOOD_INSTRUMENTAL,
    MOOD_PARTY,
    MOOD_RELAXED,
    MOOD_SAD,
    MOOD_VALENCE,
    MOVEMENT,
    MOVEMENT_NO,
    MOVEMENT_TOTAL,
    MUSICBRAINZ_ARTISTID,
    MUSICBRAINZ_DISC_ID,
    MUSICBRAINZ_ORIGINAL_RELEASE_ID,
    MUSICBRAINZ_RELEASEARTISTID,
    MUSICBRAINZ_RELEASEID,
    MUSICBRAINZ_RELEASE_COUNTRY,
    MUSICBRAINZ_RELEASE_GROUP_ID,
    MUSICBRAINZ_RELEASE_STATUS,
    MUSICBRAINZ_RELEASE_TRACK_ID,
    MUSICBRAINZ_RELEASE_TYPE,
    MUSICBRAINZ_TRACK_ID,
    MUSICBRAINZ_WORK,
    MUSICBRAINZ_WORK_ID,
    MUSICBRAINZ_WORK_COMPOSITION,
    MUSICBRAINZ_WORK_COMPOSITION_ID,
    MUSICBRAINZ_WORK_PART_LEVEL1,
    MUSICBRAINZ_WORK_PART_LEVEL1_ID,
    MUSICBRAINZ_WORK_PART_LEVEL1_TYPE,
    MUSICBRAINZ_WORK_PART_LEVEL2,
    MUSICBRAINZ_WORK_PART_LEVEL2_ID,
    MUSICBRAINZ_WORK_PART_LEVEL2_TYPE,
    MUSICBRAINZ_WORK_PART_LEVEL3,
    MUSICBRAINZ_WORK_PART_LEVEL3_ID,
    MUSICBRAINZ_WORK_PART_LEVEL3_TYPE,
    MUSICBRAINZ_WORK_PART_LEVEL4,
    MUSICBRAINZ_WORK_PART_LEVEL4_ID,
    MUSICBRAINZ_WORK_PART_LEVEL4_TYPE,
    MUSICBRAINZ_WORK_PART_LEVEL5,
    MUSICBRAINZ_WORK_PART_LEVEL5_ID,
    MUSICBRAINZ_WORK_PART_LEVEL5_TYPE,
    MUSICBRAINZ_WORK_PART_LEVEL6,
    MUSICBRAINZ_WORK_PART_LEVEL6_ID,
    MUSICBRAINZ_WORK_PART_LEVEL6_TYPE,
    MUSICIP_ID,
    OCCASION,
    OPUS,
    ORCHESTRA,
    ORCHESTRA_SORT,
    ORIGINAL_ALBUM,
    ORIGINAL_ARTIST,
    ORIGINAL_LYRICIST,
    ORIGINAL_YEAR,
    PART,
    PART_NUMBER,
    PART_TYPE,
    PERFORMER,
    PERFORMER_NAME,
    PERFORMER_NAME_SORT,
    PERIOD,
    PRODUCER,
    QUALITY,
    RANKING,
    RATING(R.string.rating),
    RECORD_LABEL,
    REMIXER,
    SCRIPT,
    SINGLE_DISC_TRACK_NO,
    SUBTITLE,
    TAGS,
    TEMPO,
    TIMBRE,
    TITLE(R.string.title),
    TITLE_SORT,
    TITLE_MOVEMENT,
    TONALITY,
    TRACK(R.string.track),
    TRACK_TOTAL(R.string.track_total),
    URL_DISCOGS_ARTIST_SITE,
    URL_DISCOGS_RELEASE_SITE,
    URL_LYRICS_SITE,
    URL_OFFICIAL_ARTIST_SITE,
    URL_OFFICIAL_RELEASE_SITE,
    URL_WIKIPEDIA_ARTIST_SITE,
    URL_WIKIPEDIA_RELEASE_SITE,
    WORK,
    WORK_TYPE,
    YEAR(R.string.year);

    companion object {
        val WELL_KNOWN: List<ConventionalMusicMetadataKey>
            get() = listOf(
                TITLE,
                ARTIST,
                ALBUM,
                ALBUM_ARTIST,
                COMPOSER,
                LYRICIST,
                YEAR,
                GENRE,
                DISC_NO,
                DISC_TOTAL,
                TRACK,
                TRACK_TOTAL,
                RATING,
                COMMENT,
            )
    }
}

interface TagMetadataKey : MusicMetadataKey

//endregion
