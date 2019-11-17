package fr.o80.rx.ext

import fr.o80.rx.layered.Cache
import fr.o80.rx.layered.LayeredCache
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins

/**
 * Continue a Single with a Completable without breaking the flow.
 * Because it doesn't exists in RxJava2.
 */
fun <T> Single<T>.andThen(completableSource: (T) -> Completable): Single<T> {
    return this.flatMap { completableSource(it).toSingleDefault(it) }
}

/**
 * Operator to describe a layer of cache.
 * It could be chained to another .cachedBy
 */
fun <T> Single<T>.cachedBy(
    read: () -> Single<Cache<T>>,
    write: (T) -> Completable,
    refreshAfter: Long,
    invalidateAfter: Long
): Single<T> {
    return RxJavaPlugins.onAssembly(LayeredCache(this, read, write, refreshAfter, invalidateAfter))
}
