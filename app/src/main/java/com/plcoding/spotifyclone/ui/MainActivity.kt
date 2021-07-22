package com.plcoding.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.adapters.SwipeSongAdapter
import com.plcoding.spotifyclone.data.model.Song
import com.plcoding.spotifyclone.exoplayer.isPlaying
import com.plcoding.spotifyclone.other.Resource.*
import com.plcoding.spotifyclone.other.toSong
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

        // Play/Pause
        ivPlayPause.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggle(it,true)
            }
        }

        swipeSongAdapter.setOnItemClickListener { song ->
            // Navigate to the Song Fragment.
            navHostFragment.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }

        navHostFragment.findNavController().addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id){
                R.id.songFragment -> hideBottomBar() // Hide bottom bar when we are in SongFragment.
                else -> showBottomBar() // Else show bottom bar in every case.
            }
        }
    }

    // Show the bottom bar.
    private fun showBottomBar(){
        vpSong.isVisible = true
        ivPlayPause.isVisible = true
    }

    // Hide the bottom bar.
    private fun hideBottomBar(){
        vpSong.isVisible = false
        ivPlayPause.isVisible = false
    }

    /**
     * Function for managing viewPager currentItem.
     */
    private fun switchViewPagerToCurrentSong(song: Song) {
        val newSongIndex = swipeSongAdapter.songs.indexOf(song)
        if (newSongIndex != -1) {
            currentPlayingSong = song
            vpSong.currentItem = newSongIndex // update the current item in viewpager.
        }
    }

    /**
     * Observe the live data.
     */
    private fun subscribeToLiveData() {
        mainViewModel.mediaItems.observe(this) { songResource ->
            when (songResource) {
                is Success -> {
                    songResource.data?.let { songs ->
                        swipeSongAdapter.songs = songs // Fill adapter with latest songs data.
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
        const val TAG = "MainActivity"
    }
}


