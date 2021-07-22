package com.plcoding.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.other.Constants.CHANNEL_ID
import com.plcoding.spotifyclone.other.Constants.NOTIFICATION_ID

/**
 * Created by Dhruv Limbachiya on 15-07-2021.
 */
class MusicNotificationManager(
    private val context: Context,
    mediaSessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener, // A listener for changes to the notification. [onNotificationCancelled or onNotificationPosted]
    private val newSongCallback: () -> Unit
) {

    private var musicNotificationManager: PlayerNotificationManager // Start,updates and cancel media style notification reflecting the player state.


    init {
        // Media Controller - Allows an app to interact with an ongoing media session. Media buttons and other commands can be sent to the session.
        // A callback may be registered to receive updates from the session, such as metadata and play state changes.
        val mediaController = MediaControllerCompat(context, mediaSessionToken)

        // Build a notification
        musicNotificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            CHANNEL_ID,
            CustomMediaDescriptorAdapter(mediaController),
        ).apply {
            setChannelNameResourceId(R.string.notification_channel_name)
            setSmallIconResourceId(R.drawable.ic_music)
            setNotificationListener(notificationListener)
            setChannelDescriptionResourceId(R.string.channel_description)
        }.build()

        musicNotificationManager.setMediaSessionToken(mediaSessionToken)
    }

    // starts or show the notification unless the player is in Player.STATE_IDLE
    fun showNotification(player: Player) {
        musicNotificationManager.setPlayer(player)
    }

    /**
     * Sub class of MediaDescriptor Adapter.
     * This adapter class is responsible for providing current playing media data to the Notification.
     */
    inner class CustomMediaDescriptorAdapter(private val mediaController: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context)
                .asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        callback.onBitmap(resource) // onBitmap() - called when the bitmap is available
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })

            return null
        }

    }
}