package com.plcoding.spotifyclone.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.plcoding.spotifyclone.data.model.Song
import com.plcoding.spotifyclone.other.Constants.SONGS_COLLECTION
import kotlinx.coroutines.tasks.await

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
            songsCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}