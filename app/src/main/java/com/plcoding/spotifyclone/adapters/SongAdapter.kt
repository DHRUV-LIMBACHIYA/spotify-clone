package com.plcoding.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.plcoding.spotifyclone.data.model.Song
import com.plcoding.spotifyclone.databinding.ListItemBinding
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 20-07-2021.
 */
class SongAdapter @Inject constructor(
   private val glide: RequestManager
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {


    private val diffUtilCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.song_id == newItem.song_id

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
            oldItem.hashCode() == newItem.hashCode()
    }

    private val differ = AsyncListDiffer(
        this,
        diffUtilCallback
    ) // Helper for computing the difference between two lists via DiffUtil on a background thread.

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private var itemClickListener: ((Song) -> Unit)? = null

    fun setOnItemClickListener(listener: (Song) -> Unit){
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.binding.apply {
            tvPrimary.text = song.song_name
            tvSecondary.text = song.song_subtitle
            glide.load(song.song_thumbnail)
                .into(ivItemImage)

           root.setOnClickListener {
               itemClickListener?.let { click ->
                   click(song)
               }
           }
        }
    }

    override fun getItemCount(): Int = songs.size

    class SongViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)
}