package fr.o80.rx.layered

import fr.o80.rx.ext.andThen
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposables
import io.reactivex.plugins.RxJavaPlugins

class LayeredCache<T>(
    private val source: Single<T>,
    private val read: () -> Single<Cache<T>>,
    private val write: (T) -> Completable,
    private val refreshAfter: Long,
    private val invalidateAfter: Long
) : Single<T>() {

    override fun subscribeActual(observer: SingleObserver<in T>) {
        val d = Disposables.empty()
        observer.onSubscribe(d)

        if (d.isDisposed) {
            return
        }

        read()
            .materialize()
            .flatMap { materialized ->
                if (materialized.isOnNext) {
                    val cache = materialized.value!!
                    when {
                        cache.isOlderThan(invalidateAfter) -> refresh()
                        cache.isOlderThan(refreshAfter) -> refresh().onErrorReturnItem(cache.value)
                        else -> just(cache.value)
                    }
                } else {
                    refresh()
                }
            }
            .subscribe(
                {
                    if (!d.isDisposed) observer.onSuccess(it)
                },
                {
                    if (!d.isDisposed) {
                        observer.onError(it)
                    } else {
                        RxJavaPlugins.onError(it)
                    }
                }
            )

    }

    private fun refresh(): Single<T> =
        source.andThen { write(it) }

}
