package fr.o80.rx.layered

/**
 * TODO KDoc
 */
class Cache<T>(
    val age: Long,
    val value: T
) {
    fun isOlderThan(comparisonAge: Long): Boolean {
        return age > comparisonAge
    }
}
