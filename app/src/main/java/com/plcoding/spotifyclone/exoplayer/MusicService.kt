package com.plcoding.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifyclone.exoplayer.callbacks.MusicNotificationListener
import com.plcoding.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.plcoding.spotifyclone.exoplayer.callbacks.MusicPlayerPreparer
import com.plcoding.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifyclone.other.Constants.NETWORK_ERROR
import com.plcoding.spotifyclone.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 14-07-2021.
 */

const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private var currentSong: MediaMetadataCompat? = null

    private val job = Job()
    private val serviceScope =
        CoroutineScope(Dispatchers.Main + job) // Custom behaviour Coroutine Scope

    lateinit var mediaSessionCompat: MediaSessionCompat

    private lateinit var mediaSessionConnector: MediaSessionConnector // Mediator between MediaSessionCompat & ExoPlayer.

    private lateinit var musicNotificationManager: MusicNotificationManager

    var isForegroundService = false

    private lateinit var musicPlayerListener: MusicPlayerEventListener

    private var isPlayerInitialized =
        false // Tracker variable for tracking initialization of player

    companion object {
        var currentSongDuration: Long = 0L // Ready from anywhere
            private set // write from only this file
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseMusicSource.fetchMediaSongs() // Fetch the songs from the firebase.
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSessionCompat = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent) // launch intent to show UI for the session.
            isActive = true // Session is currently active and ready to receive commands.
        }

        sessionToken = mediaSessionCompat.sessionToken // Set sessionToken for MediaBrowserService

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSessionCompat.sessionToken,
            MusicNotificationListener(this)
        ) {
            currentSongDuration = exoPlayer.duration
        }

        val musicPlayerPreparer = MusicPlayerPreparer(firebaseMusicSource) { mediaMetadataCompat ->
            currentSong = mediaMetadataCompat
            preparePlayer(currentSong, firebaseMusicSource.songs, true)
        }


        mediaSessionConnector =
            MediaSessionConnector(mediaSessionCompat) // Create MediaConnector instance using MediaSessionCompat object.
        mediaSessionConnector.setPlaybackPreparer(musicPlayerPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator()) // Handles queue navigation actions(SKIP or Previous), and updates the media session queue
        mediaSessionConnector.setPlayer(exoPlayer) // Set ExoPlayer. Now MediaConnector connects a MediaSessionCompat to a Player.

        musicPlayerListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerListener)
        musicNotificationManager.showNotification(exoPlayer) // Start showing MediaStyle notification.
    }

    inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSessionCompat) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }


    private fun preparePlayer(
        itemToPlay: MediaMetadataCompat?,
        songs: List<MediaMetadataCompat>,
        playNow: Boolean
    ) {
        // If current song is null then set index = 0 (play first song) else set the appropriate index
        val currentSongIndex = if (currentSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.setMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancel the serviceScope including its job and all its children.
        exoPlayer.removeListener(musicPlayerListener) // Remove the listeners
        exoPlayer.release() // Release exo player resources.
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                    // Send media item as result if all the songs are initialized else send null.
                    if (isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItem())
                        // Prepare the player for the first song but do not play it.
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                firebaseMusicSource.songs[0],
                                firebaseMusicSource.songs,
                                false
                            )
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSessionCompat.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }

                if (!resultSent) {
                    result.detach() // Detach this message from the current thread and allow the sendResult call to happen later.
                }
            }
        }
    }
}
