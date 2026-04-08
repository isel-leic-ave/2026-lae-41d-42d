package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class MapperOpt<T : Any, R : Any> private constructor(
    src: KClass<T>,
    dest: KClass<R>,
) {
    companion object {
        private val mappers = mutableMapOf<Pair<KClass<*>, KClass<*>>, MapperOpt<*, *>>()

        @Suppress("UNCHECKED_CAST")
        operator fun <T : Any, R : Any> invoke(
            srcType: KClass<T>,
            destType: KClass<R>,
        ): MapperOpt<T, R> =
            mappers.getOrPut(srcType to destType) {
                MapperOpt(srcType, destType)
            } as MapperOpt<T, R>
    }

    // 0. get the properties of the source type
    val srcProps = src.memberProperties

    // 1. Select a constructor with a matching parameter
    // for each property from the source type
    val ctor =
        dest
            .constructors
            .first { ctor ->
                srcProps
                    .all { srcProp ->
                        ctor.parameters.any { destParam ->
                            match(srcProp, destParam)
                        }
                    }
            }

    // 2. For each property associate the corresponding Parameter
    val params: Map<KProperty<*>, KParameter> =
        srcProps.associateWith { srcProp ->
            ctor.parameters.first { match(srcProp, it) }
        }

    fun mapFrom(src: T): R {
        // 3. Collect the arguments to pass to the constructor
        val args: Map<KParameter, Any?> =
            params.entries.associate { (srcProp, destParam) ->
                destParam to convert(src, srcProp, destParam)
            }

        // Call the constructor via Reflect that instantiates the object
        // and call the constructor
        return ctor.callBy(args)
    }
}

fun convert(
    src: Any,
    srcProp: KProperty<*>,
    destParam: KParameter,
): Any? {
    val srcValue = srcProp.call(src)
    return if (srcProp.returnType == destParam.type) {
        srcValue
    } else if (srcValue is Iterable<*> && destParam.type.isSubtypeOf(typeOf<Iterable<*>>())) {
        val srcKlass =
            srcProp.returnType.arguments
                .first()
                .type
                ?.classifier as KClass<Any>
        val destKlass =
            destParam.type.arguments
                .first()
                .type
                ?.classifier as KClass<Any>
        val mapper = MapperOpt(srcKlass, destKlass)
        srcValue.map { item -> item?.let { mapper.mapFrom(item) } }
    } else {
        val srcKlass = srcProp.returnType.classifier as KClass<Any>
        val destKlass = destParam.type.classifier as KClass<Any>
        srcValue?.let { MapperOpt(srcKlass, destKlass).mapFrom(srcValue) }
    }
}

fun match(
    srcProp: KProperty<*>,
    destParam: KParameter,
): Boolean {
    val match = srcProp.findAnnotation<Match>()
    val srcName = match?.name ?: srcProp.name
    return srcName == destParam.name
}
