class Constants { 
    // Value calculated at compile-time
    // Class file stores the result 81920
    static final int BITS_OF_10KB = 8 * 10 * 1024;   

    // Value calculated at runtime
    // IMMUTABLE but not CONSTANT
    static final int BITS_OF_5KB = calc(8, 10, 5);   

    static int calc(int nrBits, int words, int slots) {
        return nrBits * words * slots;
    }
}
