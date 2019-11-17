package fr.o80.rx.layered

import io.reactivex.Completable
import io.reactivex.Single

class InMemoryCache {

    private var value = -1L
    private var wroteAt = -1L

    fun read(): Single<Cache<Long>> =
        Single.fromCallable {
            if (wroteAt < 0) throw IllegalStateException("Cache empty")
            Cache(System.currentTimeMillis() - wroteAt, value)
        }

    fun write(value: Long): Completable =
        Completable.fromAction {
            this.value = value
            wroteAt = System.currentTimeMillis()
        }
}
