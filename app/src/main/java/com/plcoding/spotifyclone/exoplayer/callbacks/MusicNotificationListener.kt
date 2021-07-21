package com.plcoding.spotifyclone.exoplayer.callbacks

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.plcoding.spotifyclone.exoplayer.MusicService
import com.plcoding.spotifyclone.other.Constants.NOTIFICATION_ID

/**
 * Created by Dhruv Limbachiya on 15-07-2021.
 */

// Custom class for handling changes to the notification
class MusicNotificationListener(private val musicService: MusicService) :
    PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        musicService.apply {
            stopForeground(true) //Remove this service from foreground state, allowing it to be killed if more memory is needed.
            isForegroundService = false
            stopSelf() // stop the service
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        musicService.apply {
            if (ongoing && !isForegroundService) {
                // Start the foreground service
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
            }
            startForeground(NOTIFICATION_ID, notification) // Make the started service run in foreground by providing a notification.
            isForegroundService = true
        }
    }
}