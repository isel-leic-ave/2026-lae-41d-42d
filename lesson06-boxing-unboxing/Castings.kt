open class A

class B : A()

fun foo(a: A) {
    val o: Any = a
    val b = a as B // bytecode checkcast
}

fun main() {
    println("Test an object B")
    foo(B())
    readLine()
    println("Test an object A")
    foo(A())
}