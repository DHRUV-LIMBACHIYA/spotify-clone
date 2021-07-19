package com.plcoding.spotifyclone.di.modules

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.plcoding.spotifyclone.data.remote.SongDatabase
import com.plcoding.spotifyclone.exoplayer.FirebaseMusicSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

/**
 * Created by Dhruv Limbachiya on 14-07-2021.
 */

// Module for providing dependencies to Service
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideSongDatabase() = SongDatabase()

    @ServiceScoped
    @Provides
    fun provideFirebaseMusicSource() = FirebaseMusicSource(SongDatabase())

    @ServiceScoped
    @Provides
    fun provideAudioAttribute() = AudioAttributes.Builder().apply {
        setContentType(C.CONTENT_TYPE_MUSIC)
        setUsage(C.USAGE_MEDIA)
        build()
    }

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ) = SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes,true)
        setHandleAudioBecomingNoisy(true) // Player pause automatically when audio is rerouted from device to speaker/handsfree.
    }

    @ServiceScoped
    @Provides
    fun provideDataSource(
        @ApplicationContext context: Context
    ) = DefaultDataSourceFactory(context,Util.getUserAgent(context,"Spotify Clone"))
}