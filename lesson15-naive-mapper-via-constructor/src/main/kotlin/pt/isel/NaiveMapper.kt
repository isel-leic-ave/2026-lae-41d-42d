package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

fun Any.mapTo(dest: KClass<*>): Any {
    // 0. get the properties of the source type
    val srcProps = this::class.memberProperties

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

    // 3. Collect the arguments to pass to the constructor
    val args: Map<KParameter, Any?> = params.entries.associate { (srcProp, destParam) ->
        destParam to convert(this, srcProp, destParam)
    }

    // Call the constructor via Reflect that instantiates the object
    // and call the constructor
    return ctor.callBy(args)
}

fun convert(src: Any, srcProp: KProperty<*>, destParam: KParameter) : Any? {
    val srcValue = srcProp.call(src)
    return if(srcProp.returnType == destParam.type) {
        srcValue
    } else {
        val destKlass = destParam.type.classifier as KClass<*>
        srcValue?.mapTo(destKlass)
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
