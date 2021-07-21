package com.plcoding.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.plcoding.spotifyclone.R
import com.plcoding.spotifyclone.data.model.Song
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 20-07-2021.
 */
class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item) {

    override val differ = AsyncListDiffer(this,diffUtilCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {

            tvPrimary.text = song.song_name

            tvSecondary.text = song.song_subtitle

            glide.load(song.song_thumbnail)
                .into(ivItemImage)

            setOnClickListener {
                itemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}