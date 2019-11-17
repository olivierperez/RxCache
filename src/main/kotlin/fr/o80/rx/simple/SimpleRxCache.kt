package fr.o80.rx.simple

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TODO KDoc
 */
class SimpleRxCache<T>(
    private val source: Observable<T>,
    private val timeToLive: Long,
    private val timeUnit: TimeUnit
) : ObservableOnSubscribe<T> {

    /**
     * TODO KDoc
     */
    constructor(source: Observable<T>) : this(source, -1, TimeUnit.MILLISECONDS)

    init {
        refresh()
    }

    private lateinit var cached: Observable<T>
    private var refreshAfter: Long = -1
    private val needToRefresh = AtomicBoolean(false)

    override fun subscribe(emitter: ObservableEmitter<T>) {
        if (needToRefresh.get() || timeToLive >= 0 && System.currentTimeMillis() > refreshAfter) {
            needToRefresh.set(false)
            refresh()
        }

        cached.subscribe(object : Observer<T> {
            override fun onComplete() {
                emitter.onComplete()
            }

            override fun onSubscribe(d: Disposable) {
                emitter.setDisposable(d)
            }

            override fun onNext(t: T) {
                emitter.onNext(t)
            }

            override fun onError(e: Throwable) {
                emitter.onError(e)
            }

        })
    }

    private fun refresh() {
        cached = source.cache()
        refreshAfter = if (timeToLive < 0) -1 else System.currentTimeMillis() + timeUnit.toMillis(timeToLive)
    }

    fun forceRefresh() {
        needToRefresh.set(true)
    }
}
