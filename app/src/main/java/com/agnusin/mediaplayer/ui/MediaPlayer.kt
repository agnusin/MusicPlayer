package com.agnusin.mediaplayer.ui

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.agnusin.mediaplayer.domain.model.TrackId
import com.agnusin.mediaplayer.mediaplayer.service.MediaService

class MediaPlayer(
    private val context: Context,
    private val onError: () -> Unit
): LifecycleObserver, MediaBrowserCompat.ConnectionCallback() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaController: MediaControllerCompat? = null

    private val _mediaMetadata = MutableLiveData<MediaMetadataCompat?>()

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()

    private val mediaControllerCallback = object: MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _mediaMetadata.postValue(metadata)
        }

        override fun onSessionDestroyed() {
            _playbackState.postValue(null)
        }
    }

    val mediaMetadata: LiveData<MediaMetadataCompat?> = _mediaMetadata

    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MediaService::class.java),
            this,
            null
        )
        mediaBrowser.connect()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        mediaController?.unregisterCallback(mediaControllerCallback)
        mediaController = null

        if (mediaBrowser.isConnected) {
            mediaBrowser.disconnect()
        }

        _playbackState.postValue(null)
    }

    fun play(trackId: TrackId) {
        mediaController?.transportControls?.playFromMediaId(trackId, null)
    }

    fun pause() {
        mediaController?.transportControls?.pause()
    }

    override fun onConnected() {

        mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
            .apply {
                registerCallback(mediaControllerCallback)
            }

        // Sync existing MediaSession state to the UI.
        mediaControllerCallback.onMetadataChanged(mediaController!!.metadata)
        mediaControllerCallback.onPlaybackStateChanged(mediaController!!.playbackState)
    }

    override fun onConnectionSuspended() {
        onError()
    }

    override fun onConnectionFailed() {
        onError()
    }
}