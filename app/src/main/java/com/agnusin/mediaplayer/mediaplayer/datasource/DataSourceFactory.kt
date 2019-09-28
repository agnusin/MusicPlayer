package com.agnusin.mediaplayer.mediaplayer.datasource

import android.content.Context
import com.agnusin.mediaplayer.domain.model.Track

class DataSourceFactory(val context: Context) {

    fun createDataSource(track: Track): DataSource =
        when (track) {
            is Track.AssetTrack -> {
                val asset = context.assets.openFd(track.fileName)
                DataSource(
                    asset.fileDescriptor,
                    asset.startOffset,
                    asset.length
                )
            }
        }

}