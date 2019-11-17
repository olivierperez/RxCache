package fr.o80.rx.simple

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * TODO KDoc
 */
fun <T> Observable<T>.simpleCache(): Observable<T> {
    return Observable.create(SimpleRxCache(this))
}

/**
 * TODO KDoc
 */
fun <T> Observable<T>.simpleCache(duration: Long, timeUnit: TimeUnit): Observable<T> {
    return Observable.create(SimpleRxCache(this, duration, timeUnit))
}
