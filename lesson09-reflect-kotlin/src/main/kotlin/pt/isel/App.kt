package pt.isel

import java.net.URI
import java.time.LocalDate
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.KVisibility.*
import kotlin.reflect.full.declaredMemberFunctions

fun checkMembers(obj: Any) {
    println("### " + obj::class.simpleName)
    obj::class
        .members
        .forEach { member ->
            if (member is KProperty<*>) {
                println("Prop " + member.name)
            } else {
                println("Func " + member.name)
            }
        }
}

fun checkAndCallMethods(obj: Any) {
    println("### " + obj::class.simpleName)
    obj::class
        .declaredMemberFunctions
        .filter { func ->
            func.parameters.size == 1 &&
                func.parameters[0].kind == KParameter.Kind.INSTANCE &&
                func.visibility == PUBLIC
        }.forEach { func ->
            println("Func ${func.name}(): ${func.returnType} ======> ${func.call(obj)}")
        }
}

fun main() {
    checkAndCallMethods(URI("https://github.com"))
    checkAndCallMethods(LocalDate.now())
}
