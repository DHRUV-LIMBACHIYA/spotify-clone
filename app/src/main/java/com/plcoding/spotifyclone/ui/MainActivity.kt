package com.plcoding.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.adapters.SwipeSongAdapter
import com.plcoding.spotifyclone.data.model.Song
import com.plcoding.spotifyclone.exoplayer.isPlaying
import com.plcoding.spotifyclone.other.Resource.*
import com.plcoding.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item.*
import javax.inject.Inject
import kotlin.Error

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private val mainViewModel: MainViewModel by viewModels()

    private var currentPlayingSong: Song? = null

    private var currentPlaybackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToLiveData()

        vpSong.adapter = swipeSongAdapter

        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(currentPlaybackState?.isPlaying == true){
                    mainViewModel.playOrToggle(swipeSongAdapter.songs[position])
                }else {
                    currentPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggle(it,true)
            }
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newSongIndex = swipeSongAdapter.songs.indexOf(song)
        if (newSongIndex != -1) {
            currentPlayingSong = song
            vpSong.currentItem = newSongIndex // update the current item in viewpager.
        }
    }

    private fun subscribeToLiveData() {
        mainViewModel.mediaItems.observe(this) { songResource ->
            when (songResource) {
                is Success -> {
                    songResource.data?.let { songs ->
                        swipeSongAdapter.songs = songs // Fill adapter with latest songs data.
                        if (songs.isNotEmpty()) {
//                            glide.load((currentPlayingSong ?: songs[0].song_thumbnail))
//                                .into(ivCurSongImage)
                        }
                        switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
                    }
                }
                is Error -> Unit
                is Loading -> Unit
            }
        }

        mainViewModel.currentPlayingSong.observe(this) {
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
//            glide.load((currentPlayingSong?.song_thumbnail)).into(ivCurSongImage)
            switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
        }

        mainViewModel.playbackState.observe(this){ state ->
           currentPlaybackState = state // Update the latest state.

           ivPlayPause.setImageResource(
               if(currentPlaybackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
           )
        }

        mainViewModel.isConnected.observe(this) { event ->
            event.getContentIfNotHandled()?.let { resource ->
                when (resource) {
                    is Error -> {
                        Snackbar.make(
                            rootLayout,
                            resource.message ?: "An unknown error occurred!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) { event ->
            event.getContentIfNotHandled()?.let { resource ->
                when (resource) {
                    is Error -> {
                        Snackbar.make(
                            rootLayout,
                            resource.message ?: "An unknown error occurred!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    else -> Unit
                }
            }
        }

    }

    companion object {
        const val TAG = "TAGGIE"
    }
}


// Extension function for converting MediaMetadataCompat to Custom Song object.
private fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            description.mediaId ?: "",
            description.title.toString(),
            description.subtitle.toString(),
            description.mediaUri.toString(),
            description.iconUri.toString()
        )
    }


}
