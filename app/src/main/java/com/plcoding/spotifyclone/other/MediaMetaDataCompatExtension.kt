package com.plcoding.spotifyclone.other

import android.support.v4.media.MediaMetadataCompat
import com.plcoding.spotifyclone.data.model.Song

/**
 * Created by Dhruv Limbachiya on 22-07-2021.
 */

// Extension function for converting MediaMetadataCompat to Custom Song object.
fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            description.mediaId ?: "",
            description.title.toString(),
            description.subtitle.toString(),
            description.mediaUri.toString(),
            description.iconUri.toString()
        )
    }
}
