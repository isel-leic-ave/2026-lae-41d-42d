package pt.isel

import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

class Utils {
    fun foo(
        nr: Int,
        label: String? = "isel",
        msg: String,
    ) {
        println("$nr $label $msg")
    }
}

fun main() {
    val fFoo: KFunction<*> = Utils::class
        .memberFunctions
        .first { it.name == "foo" }

    fFoo.call(Utils(), 7, null, "isel") // <=> foo(7, null, "isel")
    // f.call(Utils(), 7, "isel") // <=> foo(7, isel) // Callable expects 4 arguments, but 3 were provided.

    val paramThis = fFoo.instanceParameter
    checkNotNull(paramThis)
    val pNr = fFoo.parameters.first { it.name == "nr" }
    val pMsg = fFoo.parameters.first { it.name == "msg" }
    fFoo.callBy(mapOf(
        pMsg to "blue",
        paramThis to Utils(),
        pNr to 7,
    ))

}
