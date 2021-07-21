package com.plcoding.spotifyclone.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.plcoding.spotifyclone.data.model.Song
import com.plcoding.spotifyclone.other.Constants.SONGS_COLLECTION
import com.plcoding.spotifyclone.ui.MainActivity
import com.plcoding.spotifyclone.ui.MainActivity.Companion.TAG
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Created by Dhruv Limbachiya on 14-07-2021.
 */
class SongDatabase {

    private val fireStore = FirebaseFirestore.getInstance() // Get the fireStore instance.
    private val songsCollection = fireStore.collection(SONGS_COLLECTION)

    /**
     * Fetch all the songs from the fireStore.
     * @return - list of songs.
     */
    suspend fun getAllSongs(): List<Song> {
        return try {
            Log.i(TAG, "Fetching song...")
            songsCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            Log.i(TAG, "Error in fetching song")
//            Timber.tag(MainActivity.TAG).i("Error in fetching song")
            emptyList()
        }
    }
}