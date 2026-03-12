package pt.isel

import java.net.URI
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter.Kind.INSTANCE
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.memberFunctions

fun checkMembers(obj: Any) {
    obj::class
        .members
        .forEach { member: KCallable<*> ->
            if (member is KFunction) {
                println("Func " + member.name)
            } else {
                println("Prop " + member.name)
            }
        }
}

fun checkAndCallMethods(obj: Any) {
    obj::class
        .memberFunctions
        .filter {
            it.parameters.size == 1 &&
                it.parameters[0].kind == INSTANCE &&
                it.visibility == PUBLIC
        }.forEach { f ->
            println(f.name + ":" + f.returnType + "() ----> " + f.call(obj))
        }
}

fun main() {
    checkAndCallMethods(URI("https://github.com"))
    // checkAndCallMethods(LocalDate.now())
}
