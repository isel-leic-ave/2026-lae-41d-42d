fun foo(o: Any) {}

fun inc(nr: Int?) : Int? {
    requireNotNull(nr)
    // => Unboxing
    return nr + 1 // Boxing
}


fun main() {
    foo(7) // Boxing
    inc(7) // Boxing
}