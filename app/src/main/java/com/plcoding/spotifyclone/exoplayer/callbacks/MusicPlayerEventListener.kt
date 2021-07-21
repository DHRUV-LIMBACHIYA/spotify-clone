package com.plcoding.spotifyclone.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.plcoding.spotifyclone.exoplayer.MusicService

/**
 * Created by Dhruv Limbachiya on 15-07-2021.
 */
class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener{

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if(playbackState == Player.STATE_READY && !playWhenReady){
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, "An unknown error occured", Toast.LENGTH_LONG).show()
    }
}