package com.plcoding.spotifyclone.exoplayer

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifyclone.exoplayer.callbacks.MusicNotificationListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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


    private val job = Job()

    private val serviceScope = CoroutineScope(Dispatchers.Main + job) // Custom behaviour Coroutine Scope

    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector // Mediator between MediaSessionCompat & ExoPlayer.
    private lateinit var musicNotificationManager: MusicNotificationManager

    var isForeground = false

    override fun onCreate() {
        super.onCreate()
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)
        }

        mediaSessionCompat = MediaSessionCompat(this,SERVICE_TAG).apply {
            setSessionActivity(activityIntent) // launch intent to show UI for the session.
            isActive = true // Session is currently active and ready to receive commands.
        }

        sessionToken = mediaSessionCompat.sessionToken // Set sessionToken for MediaBrowserService

        musicNotificationManager = MusicNotificationManager(this,mediaSessionCompat.sessionToken,MusicNotificationListener(this)){

        }

        mediaSessionConnector = MediaSessionConnector(mediaSessionCompat) // Create MediaConnector instance using MediaSessionCompat object.

        mediaSessionConnector.setPlayer(exoPlayer) // Set ExoPlayer. Now MediaConnector connects a MediaSessionCompat to a Player.
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancel the serviceScope including its job and all its children.
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}