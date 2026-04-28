package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

private data class PropInfo(
    val srcProp: KProperty<*>,
    val destParam: KParameter,
    val mapPropValue: (Any?) -> Any?,
)

class MapperOpt<T : Any, R : Any>(
    src: KClass<T>,
    dest: KClass<R>,
) : Mapper<T, R> {
    // 0. get the properties of the source type
    private val srcProps = src.memberProperties

    // 1. Select a constructor with a matching parameter
    // for each property from the source type
    private val ctor =
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
    private val params: List<PropInfo> =
        srcProps.map { srcProp ->
            val param: KParameter = ctor.parameters.first { match(srcProp, it) }
            val converter = buildConverter(srcProp, param)
            PropInfo(srcProp, param, converter)
        }

    override fun mapFrom(from: T): R {
        // 3. Collect the arguments to pass to the constructor
        val args: Map<KParameter, Any?> =
            params.associate { (srcProp, destParam, converter) ->
                val propValue = srcProp.call(from)
                destParam to converter(propValue)
            }

        // Call the constructor via Reflect that instantiates the object
        // and call the constructor
        return ctor.callBy(args)
    }
}

private fun buildConverter(
    srcProp: KProperty<*>,
    destParam: KParameter,
): (Any?) -> Any? {
    if (srcProp.returnType == destParam.type) {
        return { srcValue -> srcValue }
    } else if (destParam.type.isSubtypeOf(typeOf<List<Any>>())) {
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
        return { srcValue -> mapper.mapFromList(srcValue as List<Any>) }
    } else {
        val srcKlass = srcProp.returnType.classifier as KClass<Any>
        val destKlass = destParam.type.classifier as KClass<Any>
        val mapper = MapperOpt(srcKlass, destKlass)
        return { srcValue -> srcValue?.let { mapper.mapFrom(it) } }
    }
}

private fun match(
    srcProp: KProperty<*>,
    destParam: KParameter,
): Boolean {
    val match = srcProp.findAnnotation<Match>()
    val srcName = match?.name ?: srcProp.name
    return srcName == destParam.name
}
