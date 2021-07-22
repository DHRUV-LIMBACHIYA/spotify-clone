package com.plcoding.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.data.model.Song
import com.plcoding.spotifyclone.exoplayer.isPlaying
import com.plcoding.spotifyclone.other.Resource
import com.plcoding.spotifyclone.other.toSong
import com.plcoding.spotifyclone.ui.viewmodels.MainViewModel
import com.plcoding.spotifyclone.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 22-07-2021.
 */

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private val mMainViewModel: MainViewModel by activityViewModels() // MainViewModel bounded with activity lifecycle.

    private val mSongViewModel: SongViewModel by viewModels() // SongViewModel bounded with current fragment lifecycle.

    private var currentPlayingSong: Song? = null // Hold the current playing song object.

    private var playbackState: PlaybackStateCompat? = null // Hold the player state.

    /**
     * shouldUpdateSeekbar = true means song position changed by the player.Update the seekbar. (Automatically)
     * shouldUpdateSeekbar = false means user changed the player position by dragging the seekbar. (Manually - By user)
     */
    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToLiveData()

        ivSkip.setOnClickListener {
            mMainViewModel.skipToNext()
        }

        ivSkipPrevious.setOnClickListener {
            mMainViewModel.skipToPrevious()
        }

        ivPlayPauseDetail.setOnClickListener {
            currentPlayingSong?.let {
                mMainViewModel.playOrToggle(it,true)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    SongViewModel.convertTimeStampToString(requireContext(),progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mMainViewModel.seekTo(seekBar.progress.toLong()) // Seek player to the given SeekBar's current progress.
                    shouldUpdateSeekbar = true
                }
            }

        })
    }

    /**
     * Update the Title TextView and Thumbnail ImageView.
     */
    private fun updateSongTitleAndImage(song: Song){
        val title = "${song.song_name} - ${song.song_subtitle}"
        tvSongName.text = title
        glide.load(song.song_thumbnail).into(ivSongImage)
        currentPlayingSong = song
    }

    private fun subscribeToLiveData(){
        mMainViewModel.mediaItems.observe(viewLifecycleOwner){ songResource ->
            when(songResource){
                is Resource.Success -> {
                    songResource.data?.let { songs ->
                        /**
                         * If user enter in [SongFragment] without playing any song(currentPlayingSong == null)
                         * then update the title and image by fetching first song data.
                         */
                        if(currentPlayingSong == null && songs.isNotEmpty()){
                            currentPlayingSong = songs[0]
                            updateSongTitleAndImage(songs[0])
                        }
                    }
                }
                else -> Unit
            }
        }

        mMainViewModel.currentPlayingSong.observe(viewLifecycleOwner){
            if(it == null) return@observe

            currentPlayingSong = it.toSong() // Convert the MediaMetaCompat to custom Song object.
            updateSongTitleAndImage(currentPlayingSong!!) // Update the current playing song.
        }

        mMainViewModel.playbackState.observe(viewLifecycleOwner){ state ->
            playbackState = state // update the current state.

            // Update the Play/Pause icon.
            ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )

            seekBar.progress = (state?.position?.toInt() ?: 0) // Last changed position of playback.
        }

        mSongViewModel.currentPlayingSongPosition.observe(viewLifecycleOwner){ ms ->
            ms?.let {
                if(shouldUpdateSeekbar){
                    seekBar.progress = ms.toInt() // Set the current player position to the Seekbar progress.
                    tvCurTime.text = SongViewModel.convertTimeStampToString(requireContext(),ms) // Set the current progress to the tvCurTime TextView
                }
            }
        }

        mSongViewModel.currentPlayingSongDuration.observe(viewLifecycleOwner){ ms ->
            ms?.let {
                seekBar.max = ms.toInt() // set the max = song duration(ms).
                tvSongDuration.text = SongViewModel.convertTimeStampToString(requireContext(),ms) // Update total duration textview.
            }
        }
    }
}