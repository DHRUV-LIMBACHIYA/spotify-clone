package com.plcoding.spotifyclone.exoplayer

import android.support.v4.media.session.PlaybackStateCompat

/**
 * Created by Dhruv Limbachiya on 19-07-2021.
 */

inline val PlaybackStateCompat.isPrepare
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPlayEnabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L  ||  // If session support or enabled Play action command.
        (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L && state == PlaybackStateCompat.STATE_PAUSED) // Check Play/Play actions are not disabled. actions are not disabled and state is in Pause state.
