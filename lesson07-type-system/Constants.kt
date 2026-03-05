// Value calculated at compile-time
// Class file stores the result 81920
const val BITS_OF_10KB = 8 * 10 * 1024;   

// Value calculated at runtime
// IMMUTABLE but not CONSTANT
val BITS_OF_5KB = calc(8, 10, 5);   

fun calc(nrBits: Int, words: Int, slots: Int) : Int {
    return nrBits * words * slots;
}