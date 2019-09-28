package com.agnusin.mediaplayer.mediaplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.agnusin.mediaplayer.domain.model.Track
import com.agnusin.mediaplayer.domain.model.TrackId

abstract class PlayerAdapter(private val context: Context) {

    companion object {
        private const val MEDIA_VOLUME_DEFAULT = 1.0f
        private const val MEDIA_VOLUME_DUCK = 0.2f

        private val AUDIO_NOISY_INTENT_FILTER =
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }

    private val audioFocusHelper = AudioFocusHelper(context)

    private var playOnAudioFocus = false

    private var isAudioNoisyReceiverRegistered = false

    private val tracksLock = Any()

    private var tracks = ArrayList<Track>()

    private val audioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                if (isPlaying()) {
                    pause()
                }
            }
        }
    }

    abstract fun playFromTrackId(id: TrackId)

    abstract fun isPlaying(): Boolean

    abstract fun setVolume(volume: Float)

    abstract fun onPlay()

    abstract fun onPause()

    abstract fun onStop()

    abstract fun seekTo(position: Long)

    protected fun play() {
        if (audioFocusHelper.requestAudioFocus()) {
            registerAudioNoisyReceiver()
            onPlay()
        }
    }

    fun pause() {
        if (playOnAudioFocus) {
            audioFocusHelper.abandonAudioFocus()
        }
        unregisterAudioNoisyReceiver()
        onPause()
    }

    fun stop() {
        if (playOnAudioFocus) {
            audioFocusHelper.abandonAudioFocus()
        }
        onStop()
    }

    fun addTrack(track: Track) {
        synchronized(tracksLock) {
            tracks.add(track)
        }
    }

    fun getTrackById(id: TrackId): Track? =
        synchronized(tracksLock) {
            tracks.find { it.id == id }
        }

    private fun registerAudioNoisyReceiver() {
        if (!isAudioNoisyReceiverRegistered) {
            context.registerReceiver(audioNoisyReceiver,
                AUDIO_NOISY_INTENT_FILTER
            )
            isAudioNoisyReceiverRegistered = true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (isAudioNoisyReceiverRegistered) {
            context.unregisterReceiver(audioNoisyReceiver)
            isAudioNoisyReceiverRegistered = false
        }
    }

    private inner class AudioFocusHelper(context: Context) :
        AudioManager.OnAudioFocusChangeListener {

        private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        private var currentAudioFocusRequest: AudioFocusRequestCompat? = null

        private val audioFocusRequestBuilder =
            AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
                .run {
                    setAudioAttributes(
                        AudioAttributesCompat.Builder()
                            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setWillPauseWhenDucked(false)
                    setOnAudioFocusChangeListener(this@AudioFocusHelper)
                }

        fun requestAudioFocus(): Boolean {
            if (currentAudioFocusRequest != null) {
                releaseAudioFocus()
            }

            return audioFocusRequestBuilder.build().let {
                val result = AudioManagerCompat.requestAudioFocus(audioManager, it)
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    currentAudioFocusRequest = it
                    true
                } else false
            }
        }

        fun abandonAudioFocus() {
            releaseAudioFocus()
        }

        override fun onAudioFocusChange(status: Int) {
            when (status) {
                AudioManagerCompat.AUDIOFOCUS_GAIN -> {
                    if (playOnAudioFocus && !isPlaying()) {
                        play()
                    } else if (isPlaying()) {
                        setVolume(MEDIA_VOLUME_DEFAULT)
                    }
                    playOnAudioFocus = true
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    setVolume(MEDIA_VOLUME_DUCK)
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (isPlaying()) {
                        playOnAudioFocus = true
                        pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    currentAudioFocusRequest?.let {
                        AudioManagerCompat.abandonAudioFocusRequest(audioManager, it)
                    }
                    playOnAudioFocus = false
                    stop()
                }
            }
        }

        private fun releaseAudioFocus() {
            currentAudioFocusRequest?.let {
                AudioManagerCompat.abandonAudioFocusRequest(audioManager, it)
            }
            currentAudioFocusRequest = null
        }
    }
}