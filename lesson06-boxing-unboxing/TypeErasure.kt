class Student(val nr: Int, val name: String)

fun main() {
    val l: MutableList<Student> = mutableListOf(Student(183764, "Maria"))
    // l.add("ola") // Compile-time error
    val objs = l as MutableList<Any>
    objs.add("ola")
    println(objs[1])
    println(l[1].name)
}