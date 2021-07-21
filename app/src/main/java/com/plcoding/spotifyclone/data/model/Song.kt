package com.plcoding.spotifyclone.data.model

/**
 * Created by Dhruv Limbachiya on 14-07-2021.
 */

/**
 * Hold the song data.
 */
data class Song(
    val song_id: String = "",
    val song_name: String = "",
    val song_subtitle: String = "",
    val song_url: String = "",
    val song_thumbnail: String = "",
)
