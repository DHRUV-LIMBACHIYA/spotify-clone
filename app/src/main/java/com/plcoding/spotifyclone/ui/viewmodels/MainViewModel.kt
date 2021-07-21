package com.plcoding.spotifyclone.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.plcoding.spotifyclone.data.model.Song
import com.plcoding.spotifyclone.exoplayer.MusicServiceConnection
import com.plcoding.spotifyclone.exoplayer.isPlayEnabled
import com.plcoding.spotifyclone.exoplayer.isPlaying
import com.plcoding.spotifyclone.exoplayer.isPrepare
import com.plcoding.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifyclone.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 19-07-2021.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    // Live data for holding media items to display in the recyclerview.
    private var _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentPlayingSong = musicServiceConnection.currentPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        // Status Loading...
        _mediaItems.value = Resource.Loading(null)

        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    // Map the children of type MediaItem to Song type.
                    val items = children.map { mediaItem ->
                        Song(
                            mediaItem.mediaId ?: "",
                            mediaItem.description.title.toString(),
                            mediaItem.description.subtitle.toString(),
                            mediaItem.description.mediaUri.toString(),
                            mediaItem.description.iconUri.toString()
                        )
                    }
                    // Status data loaded successfully.
                    _mediaItems.value = Resource.Success(items)
                }
            })
    }

    // skip to next song.
    fun skipToNext() {
        musicServiceConnection.mediaController.transportControls.skipToNext()
    }

    // skip to the previous song.
    fun skipToPrevious() {
        musicServiceConnection.mediaController.transportControls.skipToPrevious()
    }

    // seek to the particular position
    fun seekTo(pos: Long) {
        musicServiceConnection.mediaController.transportControls.seekTo(pos)
    }

    fun playOrToggle(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepare ?: false

        // Current song.
        if (isPrepared && (mediaItem.song_id == currentPlayingSong.value?.getString(
                MediaMetadataCompat.METADATA_KEY_MEDIA_ID
            ))
        ) {
            playbackState.value?.let { state ->
                when {
                    state.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    state.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            // New song
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.song_id, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {}) // UnSubscribe to Parent Id.
    }

}