package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private val root =
    Unit::class.java
        .getResource("/")
        ?.toURI()
        ?.path

/**
 * Cache of dynamically generated mappers keyed by the domain class.
 * Prevents repeated code generation and loading.
 */
private val mappers = mutableMapOf<Pair<Class<*>, Class<*>>, Mapper<*, *>>()

/**
 * Loads a dynamic mapper instance for the given domain classes using their Java `Class` representations.
 * If not already cached, the mapper class is generated, loaded, and instantiated.
 * Keeping the mapper cache keyed by Java `Class` objects instead of Kotlin `KClass` is preferred,
 * since dynamic mappers may internally require auxiliary mappers and invoke this method with
 * the property types' corresponding `Class` references.
 * Otherwise, obtaining the Java `Class` from a `KClass` would introduce the overhead of
 * calling `kotlin/jvm/JvmClassMappingKt.getJavaClass`.
 */
fun <T : Any, R : Any> loadDynamicMapper(
    srcType: Class<T>,
    destType: Class<R>,
) = mappers.getOrPut(srcType to destType) {
    buildMapperClassfile(srcType.kotlin, destType.kotlin)
        .createInstance() as Mapper<*, *>
} as Mapper<T, R>

/**
 * Loads or creates a dynamic mapper instance for the given domain class.
 * Delegates to the Java version of `loadDynamicMapper`.
 */
fun <T : Any, R : Any> loadDynamicMapper(
    srcType: KClass<T>,
    destType: KClass<R>,
) = loadDynamicMapper(srcType.java, destType.java)

/**
 * Generates the class file for a mapper based on the structure of the given domain classes.
 * Uses code generation techniques (e.g., Class-File API) to build the repository implementation at runtime.
 *
 * @param src the Kotlin class of the source domain type.
 * @param dest the Kotlin class of the destination domain type.
 * @return the runtime-generated class implementing the repository logic.
 */
private fun <T : Any, R : Any> buildMapperClassfile(
    src: KClass<T>,
    dest: KClass<R>,
): KClass<out Any> {
    TODO()
}
