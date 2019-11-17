package fr.o80.rx.layered

import fr.o80.rx.simple.InMemoryCache
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@DisplayName("Tests around the Layered Cache system")
internal class LayeredCacheUnitTest {

    @Test
    fun `Should keep the first value in cache and restore it for second subscriber`() {
        // Given
        val atomicAge = AtomicLong(0)
        val source = Single.fromCallable { System.currentTimeMillis() }
        val cached = LayeredCache(
            source = source,
            read = { Single.just(Cache(atomicAge.get(), 5L)) },
            write = { Completable.complete() },
            invalidateAfter = 500L,
            refreshAfter = 400L
        )

        // When
        val firstResult = cached.blockingGet()
        atomicAge.set(60L)
        val secondResult = cached.blockingGet()

        // Then
        assertEquals(firstResult, secondResult, "Cache should keep same value")
    }

    @Test
    fun `Should refresh the cache after -refreshAfter- milliseconds`() {
        // Given
        val refreshCounter = AtomicInteger(0)
        val refreshAfter = 400L
        val inMemoryCache = InMemoryCache()

        val source = Single.fromCallable {
            refreshCounter.incrementAndGet()
            System.currentTimeMillis()
        }
        val cached = LayeredCache(
            source = source,
            read = inMemoryCache::read,
            write = inMemoryCache::write,
            invalidateAfter = 500L,
            refreshAfter = refreshAfter
        )

        // When
        val firstResult = cached.blockingGet()
        Thread.sleep(refreshAfter + 50L)
        val secondResult = cached.blockingGet()

        // Then
        assertNotEquals(firstResult, secondResult, "Cache should refresh")
        assertEquals(2, refreshCounter.get(), "Source should be called twice")
    }

    @Test
    fun `Should NOT fail while refreshing the cache after -refreshAfter- milliseconds`() {
        // Given
        val refreshCounter = AtomicInteger(0)
        val refreshAfter = 200L
        val inMemoryCache = InMemoryCache()

        val source = Single.fromCallable {
            val count = refreshCounter.incrementAndGet()
            if (count == 2) throw RuntimeException("Source failed")
            System.currentTimeMillis()
        }

        val cached = LayeredCache(
            source = source,
            read = inMemoryCache::read,
            write = inMemoryCache::write,
            invalidateAfter = 1_000L,
            refreshAfter = refreshAfter
        )

        // When
        val firstResult = cached.blockingGet()
        Thread.sleep(250L)
        val secondResult = cached.blockingGet()

        // Then
        assertEquals(firstResult, secondResult, "Cache should restore the previous value, because it failed to refresh")
        assertEquals(2, refreshCounter.get(), "Source should be called twice")
    }

    @Test
    fun `Should FAIL while refreshing the cache after -invalidateAfter- milliseconds`() {
        // Given
        val refreshCounter = AtomicInteger(0)
        val refreshAfter = 50L
        val invalidateAfter = 200L
        val inMemoryCache = InMemoryCache()

        val source = Single.fromCallable {
            val count = refreshCounter.incrementAndGet()
            if (count == 2) throw RuntimeException("Source failed")
            System.currentTimeMillis()
        }

        val cached = LayeredCache(
            source = source,
            read = inMemoryCache::read,
            write = inMemoryCache::write,
            invalidateAfter = invalidateAfter,
            refreshAfter = refreshAfter
        )

        // When
        cached.blockingGet()
        Thread.sleep(250L)
        val secondSubscriber = cached.test()

        // Then
        assertEquals(2, refreshCounter.get(), "Source should be called twice")
        secondSubscriber.assertError { t ->
            t is RuntimeException && t.message == "Source failed"
        }
    }

}