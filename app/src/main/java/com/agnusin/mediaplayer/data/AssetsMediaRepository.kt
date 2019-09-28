package com.agnusin.mediaplayer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.agnusin.mediaplayer.R
import com.agnusin.mediaplayer.domain.model.Track
import java.util.*

class AssetsMediaRepository: MediaRepository {

    private val _tracks = MutableLiveData<List<Track>>()

    init {
        _tracks.postValue(
            listOf(
                Track.AssetTrack(
                    UUID.randomUUID().toString(),
                    "Jazz in Paris",
                    103,
                    "jazz_in_paris.mp3"
                )
            )
        )
    }

    override fun getTracks(): LiveData<List<Track>> = _tracks

}