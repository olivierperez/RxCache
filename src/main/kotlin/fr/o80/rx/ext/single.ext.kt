package fr.o80.rx.ext

import io.reactivex.Completable
import io.reactivex.Single

/**
 * Continue a Single with a Completable without breaking the flow.
 * Because it doesn't exists in RxJava2.
 */
fun <T> Single<T>.andThen(completableSource: (T) -> Completable): Single<T> {
    return this.flatMap { completableSource(it).toSingleDefault(it) }
}
