package com.agnusin.mediaplayer.mediaplayer.service

import android.content.ContentResolver
import android.support.v4.media.MediaMetadataCompat
import com.agnusin.mediaplayer.BuildConfig
import com.agnusin.mediaplayer.domain.model.Track
import java.util.concurrent.TimeUnit

class MetadataFactory {

    fun createMetadata(track: Track): MediaMetadataCompat =
        MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, track.id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                TimeUnit.MILLISECONDS.convert(track.durationInSec.toLong(), TimeUnit.SECONDS))
            .apply {
                when (track) {
                    is Track.AssetTrack -> {
                        val uri = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                                BuildConfig.APPLICATION_ID + "/drawable/" + track.fileName
                        putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, uri)
                    }
                }
            }
            .build()
}