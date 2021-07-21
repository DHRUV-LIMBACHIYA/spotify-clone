package com.plcoding.spotifyclone.exoplayer

import android.annotation.SuppressLint
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.util.Log
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifyclone.data.remote.SongDatabase
import com.plcoding.spotifyclone.exoplayer.State.*
import com.plcoding.spotifyclone.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 14-07-2021.
 */

// This class is used to provide music source from the firebase with an appropriate format.
class FirebaseMusicSource @Inject constructor(
    private val songDatabase: SongDatabase
) {

    var songs = emptyList<MediaMetadataCompat>()


    /**
     * Function for fetching songs from the Song Database and formatted into MediaMetaDataCompat.
     */
    suspend fun fetchMediaSongs() = withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main){
            state = STATE_INITIALIZING
        }

        val allSongs = songDatabase.getAllSongs()
        Log.i(MainActivity.TAG, "fetchMediaSongs:${allSongs.size} ")
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.song_subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.song_id)
                .putString(METADATA_KEY_TITLE, song.song_subtitle)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.song_subtitle)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.song_thumbnail)
                .putString(METADATA_KEY_MEDIA_URI, song.song_url)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.song_thumbnail)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.song_subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.song_subtitle)
                .build()
            }
        withContext(Dispatchers.Main){
            state = STATE_INITIALIZED // all song loaded and formatted to MediaMetaDataCompat.
        }

        Log.i(MainActivity.TAG, "fetchMediaSongs: Songs => ${songs.size} ")
    }

    /**
     * Create a media sources from media item and concatenate multiple media sources using [ConcatenatingMediaSource]'s addMediaSource() method.
     * @return - concatenatingMediaSource (list of multiple media source)
     */
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory) : ConcatenatingMediaSource{
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
                .createMediaSource(MediaItem.Builder().setUri(song.getString(METADATA_KEY_MEDIA_URI)).build())
//                .createMediaSource(MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI))) // Create a media source from media item.
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

     fun asMediaItem(): MutableList<MediaBrowserCompat.MediaItem> {
        Log.i(MainActivity.TAG, "asMediaItem: SONGS ${songs.size} ")

        val mediaItem =  songs.map { song ->
            val description = MediaDescriptionCompat.Builder()
                .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
                .setTitle(song.description.title)
                .setSubtitle(song.description.subtitle)
                .setMediaId(song.description.mediaId)
                .setIconUri(song.description.iconUri)
                .build()

            MediaBrowserCompat.MediaItem(description,FLAG_PLAYABLE) // Make all media item browsable and playable.
        }.toMutableList()

        Log.i(MainActivity.TAG, "asMediaItem: ${mediaItem.size} ")

        return mediaItem
    }


    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state = STATE_CREATED // Initial state.
        set(value) {
            // Check if all songs are initialized/loaded or failed to load
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value  // update the state.
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED) // Set listener to true if it initialized(loaded - ready to go) and false if state is STATE_ERROR.
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITIALIZED)
            true
        }
    }

}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}