package com.agnusin.mediaplayer.ui

import android.media.AudioManager
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.agnusin.mediaplayer.MediaPlayerApp
import com.agnusin.mediaplayer.R

class MediaPlayerActivity : AppCompatActivity() {

    private val mediaPlayer by lazy { MediaPlayer(this) { onError() } }

    private val coverView by lazy { findViewById<ImageView>(R.id.cover) }

    private val playButton by lazy { findViewById<ImageButton>(R.id.playButton) }

    private val pauseButton by lazy { findViewById<ImageButton>(R.id.pauseButton) }

    private val titleView by lazy { findViewById<TextView>(R.id.trackTitle) }

    private val timeBar by lazy { findViewById<MediaTimeBar>(R.id.mediaTimeBar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with (mediaPlayer) {
            mediaMetadata.observe(this@MediaPlayerActivity, Observer { onChangeMediaMetadata(it) })
            playbackState.observe(this@MediaPlayerActivity, Observer { onChangeState(it) })
            lifecycle.addObserver(this)
        }

        (application as MediaPlayerApp).mediaRepository.getTracks().observe(this, Observer { tracks ->
            if (tracks.isNotEmpty()) {
                val track = tracks.first()

                titleView.text =track .title
                timeBar.init(0, track.durationInSec)

                playButton.setOnClickListener { mediaPlayer.play(track.id) }
                pauseButton.setOnClickListener { mediaPlayer.pause() }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun onError() {

    }

    private fun onChangeState(state: PlaybackStateCompat?) {
        if (state?.state == PlaybackState.STATE_PLAYING) {
            pauseButton.visibility = View.VISIBLE
            playButton.visibility = View.INVISIBLE

            AnimationUtils.loadAnimation(this, R.anim.cd_disk_animation).let {
                coverView.startAnimation(it)
            }

            timeBar.start()
        } else {
            pauseButton.visibility = View.INVISIBLE
            playButton.visibility = View.VISIBLE
            coverView.clearAnimation()
            timeBar.stop()
        }
    }

    private fun onChangeMediaMetadata(metadata: MediaMetadataCompat?) {
        metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)?.let {
            titleView.text = it
        }
    }
}
