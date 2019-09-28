package com.agnusin.mediaplayer.mediaplayer.datasource

import java.io.FileDescriptor

data class DataSource(
    val fileDescriptor: FileDescriptor,
    val startOffset: Long,
    val length: Long
)