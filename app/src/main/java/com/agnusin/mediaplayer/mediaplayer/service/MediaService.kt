package com.agnusin.mediaplayer.mediaplayer.service

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import com.agnusin.mediaplayer.MediaPlayerApp
import com.agnusin.mediaplayer.domain.model.Track

class MediaService: MediaBrowserServiceCompat(), OnPlaybackStateChange {

    private var mediaSession: MediaSessionCompat? = null
    private var mediaPlayer: MediaPlayerAdapter? = null

    private var tracksObserver: Observer<List<Track>>? = null

    override fun onCreate() {
        super.onCreate()

        mediaPlayer = MediaPlayerAdapter(this, this)
        mediaSession = MediaSessionCompat(this,
            TAG
        )
            .apply {
                setCallback(
                    MediaSessionCallback(
                        this,
                        mediaPlayer!!
                    )
                )
                setSessionToken(sessionToken)
            }

        tracksObserver = Observer { l ->
            l.forEach {
                mediaPlayer?.addTrack(it)
            }
        }
        (application as MediaPlayerApp).mediaRepository.getTracks().observeForever(tracksObserver!!)
    }

    override fun onDestroy() {
        super.onDestroy()

        tracksObserver?.let {
            (application as MediaPlayerApp).mediaRepository.getTracks().removeObserver(it)
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(arrayListOf())
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return  BrowserRoot(EMPTY_ROOT, null)
    }

    override fun onChange(state: PlaybackStateCompat) {
        mediaSession?.setPlaybackState(state)
    }

    companion object {

        private const val TAG = "[MediaService]"
        private const val EMPTY_ROOT = "@empty@"
    }
}