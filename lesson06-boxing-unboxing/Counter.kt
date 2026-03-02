data class Counter(val dayIndex: Int) {
    operator fun plus(increment: Int): Counter {
        return Counter(dayIndex + increment)
    }
}

fun f() : Int { return 9}

fun main() {
    // val res = 11 + f() // bytecode add
    val res = Counter(11) + 7
}