package pt.isel

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@TagTeam(name = "Sea")
class Student(
    @TagTeam(name = "Sea") val name: String,
    @TagColor(color = "White") val age: Int,
    // <=> By default: @param:TagColor(color = "White") val age: Int,
    // @property:TagColor(color = "White") val age: Int,
)

fun main() {
    Student::class
        .annotations
        .forEach { println(it) }

    Student::class
        .findAnnotation<TagTeam>()
        .also { annot -> println(annot?.name) }

    // Look for annotation TagColor in property age
    Student::class
        .memberProperties
        .first { it.name == "age" }
        .findAnnotation<TagColor>()
        .also { annot -> println(annot?.color) }
}
