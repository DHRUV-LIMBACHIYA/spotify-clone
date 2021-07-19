package com.plcoding.spotifyclone.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.plcoding.spotifyclone.other.Constants.NETWORK_ERROR
import com.plcoding.spotifyclone.other.Event
import com.plcoding.spotifyclone.other.Resource

/**
 * Created by Dhruv Limbachiya on 19-07-2021.
 */
class MusicServiceConnection(
    private val context: Context
) {

    // Live Data vars
    private var _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private var _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private var _currentPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentPlayingSong: LiveData<MediaMetadataCompat?> = _currentPlayingSong

    private var _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState


    lateinit var mediaController: MediaControllerCompat

    val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    val mediaBrowserCompat = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        //Connects to the media browse service. Internally, it binds to the service.
        //The connection callback specified in the constructor will be invoked when the connection completes or fails
        connect()
    }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowserCompat.subscribe(parentId, callback)
    }

    fun unSubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowserCompat.unsubscribe(parentId, callback)
    }

    // Class is responsible for handling the connection related events.
    inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController =
                MediaControllerCompat(context, mediaBrowserCompat.sessionToken).apply {
                    registerCallback(MediaControllerCallback())
                }
            _isConnected.postValue(Event(Resource.Success(true)));
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(
                Event(Resource.Error(null, "Connection is suspended."))
            )
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(
                    Resource.Error(
                        null,
                        "Connection Failed!"
                    )
                )
            )
        }
    }

    // Class responsible for handling MediaController callback
    inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            when (event) {
                NETWORK_ERROR -> {
                    _networkError.postValue(
                        Event(
                            Resource.Error(
                                false,
                                "Couldn't connect to the service.Please check your internet connection"
                            )
                        )
                    )
                }
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

}