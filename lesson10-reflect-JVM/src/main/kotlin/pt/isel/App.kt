package pt.isel

import java.lang.reflect.Modifier
import kotlin.reflect.full.memberProperties

fun checkMembers(obj: Any) {
    val objKotlinClass = obj::class
    val objJavaClass = objKotlinClass.java // <=> obj.javaClass

    obj::class.java
        .methods // all public methods including inherited
        .forEach { println("Method " + it.name) }

    obj::class.java
        .fields // all public fields including inherited
        .forEach { println("Field " + it.name) }
}

fun checkAndCallMethods(obj: Any) {
    obj::class.java
        .declaredMethods // not include inherited
        .filter {
            it.parameters.size == 0 &&
                it.modifiers and Modifier.PUBLIC != 0
        }.forEach { f ->
            println(f.name + ":" + f.returnType + "() ----> " + f.invoke(obj))
        }
}

open class Person(
    val name: String,
    val age: Int,
)

class Teacher(name: String, age: Int) : Person(name, age)
/**
 * NO RELATION between Student and Person
 */
class Student(
    val name: String,
    val age: Int,
)

fun main() {
    // checkAndCallMethods(URI("https://github.com"))
    // checkAndCallMethods(LocalDate.now())

    val propName = Person::class
        .memberProperties
        .first { it.name == "name" }

    println(propName.call(Person("Ze", 56)))
    println(propName.call(Person("Maria", 56)))
    println(propName.call(Teacher("Joao", 56)))
    println(propName.call(Student("Manel", 56)))
}

fun foo(p: Person) {}