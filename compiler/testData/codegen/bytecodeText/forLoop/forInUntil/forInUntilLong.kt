// IGNORE_BACKEND: JVM_IR
fun test(a: Long, b: Long): Long {
    var sum = 0L
    for (i in a until b) {
        sum = sum * 10L + i
    }
    return sum
}

// JVM non-IR uses while.
// JVM IR uses if + do-while. In addition, for "until" progressions, there is a check that the range is not empty: upper bound != MIN_VALUE.

// 0 iterator
// 0 getStart
// 0 getEnd
// 0 getFirst
// 0 getLast
// 0 getStep
// 1 LCMP
// 1 IFGE
// 1 IF