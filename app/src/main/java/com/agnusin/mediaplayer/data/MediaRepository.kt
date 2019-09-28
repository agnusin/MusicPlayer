package com.agnusin.mediaplayer.data

import androidx.lifecycle.LiveData
import com.agnusin.mediaplayer.domain.model.Track

interface MediaRepository {

    fun getTracks(): LiveData<List<Track>>
}