package com.plcoding.spotifyclone.data.model

/**
 * Created by Dhruv Limbachiya on 14-07-2021.
 */

/**
 * Hold the song data.
 */
data class Song(
    val songId: String = "",
    val songName: String = "",
    val songSubtitle: String = "",
    val songUrl: String = "",
    val songThumbnail: String = "",
)
