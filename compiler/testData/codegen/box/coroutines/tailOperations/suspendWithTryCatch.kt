// IGNORE_BACKEND_FIR: JVM_IR
// WITH_RUNTIME
// WITH_COROUTINES
// COMMON_COROUTINES_TEST
import helpers.*
import COROUTINES_PACKAGE.*
import COROUTINES_PACKAGE.intrinsics.*

suspend fun foo(x: Any): Int {
    return try {
        suspendHere()
    } catch (e: Throwable) {
        13
    }
}

suspend fun suspendHere(): Int = suspendCoroutineUninterceptedOrReturn { x ->
    x.resume(56)
    COROUTINE_SUSPENDED
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var result = -1

    builder {
        result = foo("56")
    }

    if (result != 56) return "fail 1: $result"

    return "OK"
}
