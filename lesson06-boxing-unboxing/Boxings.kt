fun foo(o: Any) {}

fun bar(i: Int) {}

fun inc(nr: Int?) : Int {
    requireNotNull(nr)
    return nr + 1
}

fun main() {
    val n = 7
    val o: Any = n // Boxing
    foo(7) // Boxing
    inc(11)
}