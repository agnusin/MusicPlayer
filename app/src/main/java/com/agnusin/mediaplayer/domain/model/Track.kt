package com.agnusin.mediaplayer.domain.model

sealed class Track(
    open val id: TrackId,
    open val title: String,
    open val durationInSec: Int
) {

    class AssetTrack(
        override val id: TrackId,
        override val title: String,
        override val durationInSec: Int,
        val fileName: String
    ): Track(
        id,
        title,
        durationInSec
    )
}