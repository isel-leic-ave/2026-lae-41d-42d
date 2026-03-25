package pt.isel

import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

class Utils {
    fun foo(
        n: Int,
        s: String? = "ola",
        m: String?,
    ) {
        println("$n $s $m")
    }
}

fun main() {
    val f: KFunction<*> = Utils::class
        .memberFunctions
        .first { it.name == "foo" }

    f.call(Utils(), 7, null, "isel") // <=> foo(7, null, isel)
    // f.call(Utils(), 7, "isel") // <=> foo(7, isel) // Callable expects 4 arguments, but 3 were provided.

    val paramThis = f.instanceParameter
    checkNotNull(paramThis)
    val paramM = f.parameters.first { it.name == "m" }
    val paramN = f.parameters.first { it.name == "n" }
    f.callBy(mapOf(
        paramM to "isel",
        paramThis to Utils(),
        paramN to 7
    ))

}
