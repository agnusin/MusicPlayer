package com.agnusin.mediaplayer.mediaplayer.service

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat

class MediaSessionCallback(
    private val ownerSession: MediaSessionCompat,
    private val player: PlayerAdapter
): MediaSessionCompat.Callback() {

    private val metadataFactory = MetadataFactory()
    private lateinit var trackMetadata: MediaMetadataCompat
    private var isPrepare = false

    override fun onPrepare() {
        super.onPrepare()

        isPrepare = true

        ownerSession.setMetadata(trackMetadata)

        if (!ownerSession.isActive) {
            ownerSession.isActive = true
        }
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        player.getTrackById(mediaId)?.let {  track ->
            trackMetadata = metadataFactory.createMetadata(track)
        }

        if (!isPrepare) {
            onPrepare()
        }

        player.playFromTrackId(mediaId)
    }

    override fun onPause() {
        player.pause()
    }

    override fun onSeekTo(pos: Long) {
        player.seekTo(pos)
    }

    override fun onStop() {
        player.stop()
        ownerSession.isActive = false
    }
}