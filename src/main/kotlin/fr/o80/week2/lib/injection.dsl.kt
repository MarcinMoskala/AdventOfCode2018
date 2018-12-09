package fr.o80.week2.lib

@DslMarker
annotation class InjectionDsl

@InjectionDsl
inline fun module(block: InjectModule.() -> Unit) = InjectModule().apply(block)

@InjectionDsl
inline fun <reified T : Any> InjectModule.factory(noinline generator: () -> T) {
    val className = getClassNameForInjection()
    generators[className] = Prototype(generator)
}

@InjectionDsl
inline fun <reified T : Any> InjectModule.singleton(noinline generator: () -> T) {
    val className = getClassNameForInjection()
    generators[className] = Singleton(className, generator)
}

@InjectionDsl
inline fun <reified T : Any> InjectModule.scoped(noinline generator: () -> T) {
    val className = getClassNameForInjection()
    generators[className] = Scoped(generator)
}

@InjectionDsl
fun InjectModule.dependsOn(vararg modules: InjectModule) {
    dependsOn.addAll(modules)
}
 
inline fun <reified T> getClassNameForInjection() = T::class.qualifiedName
        ?: throw IllegalArgumentException("A non-real class cannot be injected"))
