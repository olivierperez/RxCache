package fr.o80.rx.layered

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins

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
