package fr.o80.rx.simple

import io.reactivex.Observable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

@DisplayName("Tests around the SimpleRxCache system")
internal class SimpleRxCacheUnitTest {

    @Test
    fun `Should keep the first value in cache and restore it for second subscriber`() {
        // Given
        val source = Observable
            .fromCallable { System.currentTimeMillis() }
            .simpleCache()

        // When
        val firstResult = source.blockingFirst()
        Thread.sleep(50L)
        val secondResult = source.blockingFirst()

        // Then
        assertEquals(firstResult, secondResult, "Simple cache should keep same value")
    }

    @Test
    fun `Should refresh the cached value AFTER the given time`() {
        // Given
        val source = Observable
            .fromCallable { System.currentTimeMillis() }
            .simpleCache(200L, TimeUnit.MILLISECONDS)

        // When
        val firstResult = source.blockingFirst()
        Thread.sleep(500L)
        val secondResult = source.blockingFirst()

        // Then
        assertNotEquals(firstResult, secondResult, "Cache should refresh after the given time")
    }

    @Test
    fun `Should NOT refresh the cached value BEFORE the given time`() {
        // Given
        val source = Observable
            .fromCallable { System.currentTimeMillis() }
            .simpleCache(500L, TimeUnit.MILLISECONDS)

        // When
        val firstResult = source.blockingFirst()
        Thread.sleep(10L)
        val secondResult = source.blockingFirst()

        // Then
        assertEquals(firstResult, secondResult, "Cache should NOT refresh BEFORE the given time")
    }

    @Test
    fun `Should refresh cache after someone forced the refresh`() {
        // Given
        val source = Observable.fromCallable { System.currentTimeMillis() }
        val simpleRxCache = SimpleRxCache(source, 1_000_000L, TimeUnit.MILLISECONDS)
        val cached = Observable.create(simpleRxCache)

        // When
        val first = cached.blockingFirst()
        simpleRxCache.forceRefresh()
        Thread.sleep(50L)
        val second = cached.blockingFirst()

        // Then
        assertNotEquals(first, second, "Cache should refresh after someone forced the refresh")
    }
}
