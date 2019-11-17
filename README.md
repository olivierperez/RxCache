# RxCache

My RxJava classes for caching data.

## SimpleRxCache

```kotlin
val source = Observable.fromCallable { System.currentTimeMillis() }
val cached = source.simpleCache(5L, TimeUnit.MINUTES)

cached
    .subscribeBy(
        onNext = { println("Next: $it") },
        onError = { System.err.println("!!!!! Error !!!!! ${it.message}") }
    )
```
```kotlin
val source = Observable.fromCallable { System.currentTimeMillis() }
val simpleRxCache = SimpleRxCache(source, 5L, TimeUnit.MINUTES)
val cached = Observable.create(simpleRxCache)

cached
    .subscribeBy(
        onNext = { println("Next: $it") },
        onError = { System.err.println("!!!!! Error !!!!! ${it.message}") }
    )

// Later
simpleRxCache.forceRefresh()
```

* [SimpleRxCache](/src/main/kotlin/fr/o80/rx/simple/SimpleRxCache.kt)
* [Rx operator](/src/main/kotlin/fr/o80/rx/simple/simpleCache.ext.kt)
* [Unit Test as example](/src/test/kotlin/fr/o80/rx/simple/SimpleRxCacheUnitTest.kt)

## MultiLayer

```kotlin
val remoteSource = RemoteSource()
val inMemoryCache = InMemoryCache()

remoteSource.retrieveData()
    .cachedBy(
        read = inMemoryCache::read,
        write = inMemoryCache::write,
        refreshAfter = 200L,
        invalidateAfter = 1000L
    )
    .subscribeBy(
        onSuccess = { println("Success: $it") },
        onError = { System.err.println("!!!!! Error !!!!! ${it.message}") }
    )
```

* [LayeredCache](/src/main/kotlin/fr/o80/rx/layered/LayeredCache.kt)
* [Rx operator](/src/main/kotlin/fr/o80/rx/layered/cachedBy.ext.kt)
* [Unit Test as example](/src/test/kotlin/fr/o80/rx/layered/LayeredCacheUnitTest.kt)

## Common

* [Rx extension - Single](/src/main/kotlin/fr/o80/rx/ext/single.ext.kt)
