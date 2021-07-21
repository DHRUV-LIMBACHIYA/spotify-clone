package com.plcoding.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.adapters.SongAdapter
import com.plcoding.spotifyclone.databinding.FragmentHomeBinding
import com.plcoding.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifyclone.other.Resource
import com.plcoding.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 20-07-2021.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var songAdapter: SongAdapter
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        subscribeToLiveData()

        songAdapter.setOnItemClickListener { song ->
            mViewModel.playOrToggle(song)
        }
    }

    private fun setUpRecyclerView(){
        binding.rvAllSongs.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeToLiveData(){

        mViewModel.mediaItems.observe(viewLifecycleOwner,{ result ->
            when(result){
                is Resource.Loading -> binding.allSongsProgressBar.isVisible = true
                is Resource.Success -> {
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                    binding.allSongsProgressBar.isVisible = false
                }
                is Resource.Error -> Unit
            }
        })
    }
}