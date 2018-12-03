package fr.o80.week2

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@DslMarker
annotation class Injection

@Injection
inline fun module(block: InjectModule.() -> Unit) = InjectModule().apply(block)

class InjectModule {

    val generators: MutableMap<String, Generator<*>> = mutableMapOf()

    inline fun <reified T : Any> inject(): ReadOnlyProperty<Any, T> = object : ReadOnlyProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T = get()
    }

    inline fun <reified T : Any> get(): T {
        val className = T::class.qualifiedName
                ?: throw IllegalArgumentException("Cannot inject a non-real class")

        val provider = generators[className]
                ?: throw IllegalArgumentException("$className is not declared as injectable")

        val value = provider.get() as? T
                ?: throw IllegalArgumentException("$className doesn't provide the right type")

        return value
    }

    inline fun <reified T : Any> factory(noinline generator: () -> T) {
        val className = T::class.qualifiedName
                ?: throw IllegalArgumentException("A non-real class cannot be injected")
        generators[className] = Prototype(generator)
    }

    inline fun <reified T : Any> singleton(noinline generator: () -> T) {
        val className = T::class.qualifiedName
                ?: throw IllegalArgumentException("A non-real class cannot be injected")
        generators[className] = Singleton(generator)
    }

}

abstract class Generator<T : Any> {
    abstract fun get(): T
}

class Prototype<T : Any>(private val block: () -> T) : Generator<T>() {
    override fun get() = block()

    fun invoke(): T = block()
}

class Singleton<T : Any>(private val block: () -> T) : Generator<T>() {
    private var cached: T? = null

    override fun get(): T {
        val value = cached
        return when (value) {
            null -> block().also { cached = it }
            else -> value
        }
    }
}