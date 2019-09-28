package com.agnusin.mediaplayer.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class MediaTimeBar: AppCompatSeekBar {

    constructor(context: Context) : super(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    interface OnChangeListener {

        fun onChangePosition(position: Int)
    }
    private var timerDisposable: Disposable? = null

    var listener: OnChangeListener? = null

    fun init(fromSec: Int, toSec: Int) {
        max = toSec
        progress = fromSec
    }

    fun start() {
        val count = max - progress
        timerDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .take(count.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext{
                progress = (progress + 1)
                listener?.onChangePosition(progress)
            }
            .subscribe()
    }

    fun stop() {
        timerDisposable?.dispose()
    }

    fun reset() {
        progress = 0
    }
}