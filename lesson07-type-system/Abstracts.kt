abstract class A {
    abstract fun foo()
}

interface I {
    fun foo()
}

fun main() {
    var ca: A = object : A() {
        override fun foo() { }
    }
    ca = object : A() {
        override fun foo() { println("second") }
    }
    ca = object : A() {
        override fun foo() { println("third") }
    }
    val ci: I = object : I {
        override fun foo() { }
    }
}