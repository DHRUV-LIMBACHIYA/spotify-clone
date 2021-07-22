package com.plcoding.spotifyclone.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.exoplayer.MusicService
import com.plcoding.spotifyclone.exoplayer.MusicServiceConnection
import com.plcoding.spotifyclone.exoplayer.currentPosition
import com.plcoding.spotifyclone.other.Constants.UPDATE_POSITION_DELAY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

/**
 * Created by Dhruv Limbachiya on 22-07-2021.
 */

@HiltViewModel
class SongViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    // Current state.
    val playbackState = musicServiceConnection.playbackState

    private var _currentPlaySongPosition = MutableLiveData<Long>()
    val currentPlayingSongPosition: LiveData<Long> = _currentPlaySongPosition

    private var _currentPlayingSongDuration = MutableLiveData<Long>()
    val currentPlayingSongDuration: LiveData<Long> = _currentPlayingSongDuration

    init {
        updateCurrentPlayingSongPosition()
    }

    companion object {
        /**
         * Utility method to convert milliseconds to a display of minutes and seconds
         */
        fun convertTimeStampToString(context: Context,position: Long): String {
            val totalSeconds = floor(position / 1E3).toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds - (minutes * 60)
            return if(position < 0) context.getString(R.string.duration_unknown)
            else context.getString(R.string.duration_format).format(minutes,seconds)
        }
    }

    /**
     * Method responsible for updating current player position and duration asynchronously.
     */
    private fun updateCurrentPlayingSongPosition(){
        viewModelScope.launch {
            while(true){
                val currentStatePosition = playbackState.value?.currentPosition ?: 0L
                                if(currentPlayingSongPosition.value != currentStatePosition){
                    _currentPlaySongPosition.postValue(currentStatePosition) // Set current playback position.
                    _currentPlayingSongDuration.postValue(MusicService.currentSongDuration) // Set current playback duration.
                }
                delay(UPDATE_POSITION_DELAY)
            }
        }
    }
}