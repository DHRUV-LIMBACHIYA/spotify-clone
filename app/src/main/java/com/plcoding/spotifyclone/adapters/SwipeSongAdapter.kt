package com.plcoding.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.plcoding.spotifyclone.R
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.list_item.view.*
import kotlinx.android.synthetic.main.list_item.view.tvPrimary
import kotlinx.android.synthetic.main.swipe_item.view.*
import javax.inject.Inject

/**
 * Created by Dhruv Limbachiya on 21-07-2021.
 */
class SwipeSongAdapter @Inject constructor(
   private val glide: RequestManager
) : BaseSongAdapter(R.layout.swipe_item) {

    override val differ = AsyncListDiffer(this, diffUtilCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {

            tvPrimary.text = song.song_name

            glide.load(song.song_thumbnail).into(ivCurSongImage)

            setOnClickListener {
                itemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}