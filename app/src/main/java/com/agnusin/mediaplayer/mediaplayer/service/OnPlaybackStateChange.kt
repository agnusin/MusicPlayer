package com.agnusin.mediaplayer.mediaplayer.service

import android.support.v4.media.session.PlaybackStateCompat

interface OnPlaybackStateChange {

    fun onChange(state: PlaybackStateCompat)
}