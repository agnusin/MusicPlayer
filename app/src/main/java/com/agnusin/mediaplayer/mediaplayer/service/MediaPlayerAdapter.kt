package com.agnusin.mediaplayer.mediaplayer.service

import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import com.agnusin.mediaplayer.domain.model.TrackId
import com.agnusin.mediaplayer.mediaplayer.datasource.DataSourceFactory

class MediaPlayerAdapter(
    context: Context,
    private val listener: OnPlaybackStateChange
) : PlayerAdapter(context) {

    private var mediaPlayer: MediaPlayer? = null

    private var state: Int? = null

    private val dataSourceFactory = DataSourceFactory(context)

    private var currentTrackId: TrackId? = null

    // Work-around for a MediaPlayer bug related to the behavior of MediaPlayer.seekTo()
    // while not playing.
    private var seekWhileNotPlaying = -1

    private fun init() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            setState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    override fun playFromTrackId(trackId: TrackId) {
        val trackChanged = currentTrackId != trackId
        /*if (mCurrentMediaPlayedToCompletion) {
            // Last audio file was played to completion, the resourceId hasn't changed, but the
            // player was released, so force a reload of the media file for playback.
            mediaChanged = true
            mCurrentMediaPlayedToCompletion = false
        }*/
        if (!trackChanged) {
            if (!isPlaying()) {
                play()
            }
            return
        } else {
            release()
        }

        currentTrackId = trackId

        init()
        getTrackById(trackId)?.let { track ->
            val source = dataSourceFactory.createDataSource(track)
            mediaPlayer?.setDataSource(
                source.fileDescriptor,
                source.startOffset,
                source.length
            )
            mediaPlayer?.prepare() // TODO catch errors
            play()
        }
    }

    override fun isPlaying(): Boolean =
        mediaPlayer?.isPlaying ?: false

    override fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    override fun onPlay() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            setState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    override fun onPause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            setState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    override fun onStop() {
        setState(PlaybackStateCompat.STATE_STOPPED)
        release()
    }

    override fun seekTo(position: Long) {
        if (mediaPlayer?.isPlaying == true) {
            seekWhileNotPlaying = position.toInt()
        }
        mediaPlayer?.seekTo(position.toInt())

        // Set the state (to the current state) because the position changed and should
        // be reported to clients.
        setState(state!!)
    }

    private fun setState(@PlaybackStateCompat.State state: Int) {
        this.state = state

        // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
        val position = getCurrentPosition()
        val stateBuilder = PlaybackStateCompat.Builder()
        .setActions(getAvailableActions())
        .setState(
            state,
            position,
            1.0f,
            SystemClock.elapsedRealtime()
        )
        listener.onChange(stateBuilder.build())
    }

    private fun getCurrentPosition() =
        if (seekWhileNotPlaying >= 0) {
            val position = seekWhileNotPlaying.toLong()

            if (state == PlaybackStateCompat.STATE_PLAYING) {
                seekWhileNotPlaying = -1
            }
            position
        } else {
            mediaPlayer?.currentPosition?.toLong() ?: 0
        }

    private fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @PlaybackStateCompat.Actions
    private fun getAvailableActions(): Long =
        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
        when (state) {
            PlaybackStateCompat.STATE_STOPPED -> PlaybackStateCompat.ACTION_PLAY
                .or(PlaybackStateCompat.ACTION_PAUSE)
            PlaybackStateCompat.STATE_PLAYING -> PlaybackStateCompat.ACTION_STOP
                .or(PlaybackStateCompat.ACTION_PAUSE)
                .or(PlaybackStateCompat.ACTION_SEEK_TO)
            PlaybackStateCompat.STATE_PAUSED -> PlaybackStateCompat.ACTION_PLAY
                .or(PlaybackStateCompat.ACTION_STOP)
            else -> PlaybackStateCompat.ACTION_PLAY
                .or(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .or(PlaybackStateCompat.ACTION_STOP)
                .or(PlaybackStateCompat.ACTION_PAUSE)
        }

}