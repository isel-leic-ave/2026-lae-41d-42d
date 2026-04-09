package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

data class PropInfo(
    val srcProp: KProperty<*>,
    val destParam: KParameter,
    val mapPropValue: (Any?) -> Any?,
)

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
    val params: List<PropInfo> =
        srcProps.map { srcProp ->
            val destParam = ctor.parameters.first { match(srcProp, it) }
            PropInfo(srcProp, destParam, buildParseProp(srcProp, destParam))
        }

    fun mapFrom(src: T): R {
        // 3. Collect the arguments to pass to the constructor
        val args: Map<KParameter, Any?> =
            params.associate { (srcProp, destParam, parseProp) ->
                val propValue = srcProp.call(src)
                destParam to parseProp(propValue)
            }

        // Call the constructor via Reflect that instantiates the object
        // and call the constructor
        return ctor.callBy(args)
    }
}

@Suppress("UNCHECKED_CAST")
fun buildParseProp(
    srcProp: KProperty<*>,
    destParam: KParameter,
): (Any?) -> Any? {
    val propType = srcProp.returnType
    val paramType = destParam.type
    if (propType == paramType) {
        return { propValue -> propValue }
    }
    val propKlass = propType.classifier as KClass<Any>
    val paramKlass = paramType.classifier as KClass<Any>
    if (propKlass != List::class && paramKlass != List::class) {
        val mapper = MapperOpt(propKlass, paramKlass)
        return { propValue -> propValue?.let { mapper.mapFrom(propValue) } }
    }
    val propElemKlass = propType.arguments[0].type!!.classifier as KClass<Any>
    val paramElemKlass = paramType.arguments[0].type!!.classifier as KClass<Any>
    val mapper = MapperOpt(propElemKlass, paramElemKlass)
    return { propValue ->
        (propValue as List<*>).map {
            it?.let { mapper.mapFrom(it) }
        }
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
