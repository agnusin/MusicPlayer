package com.agnusin.mediaplayer

import android.app.Application
import com.agnusin.mediaplayer.data.AssetsMediaRepository

class MediaPlayerApp: Application() {

    val mediaRepository = AssetsMediaRepository()

}