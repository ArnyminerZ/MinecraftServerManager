package utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Runs the code inside [block] asynchronously in the [Dispatchers.IO] thread.
 * @author Arnau Mora
 * @since 20220713
 * @param block The block of code to run.
 */
fun doAsync(block: suspend CoroutineScope.() -> Unit) {
    CoroutineScope(Dispatchers.IO).launch { block() }
}

/**
 * Runs the code inside [block] in the UI thread.
 * @author Arnau Mora
 * @since 20220713
 * @param block The block of code to run.
 */
suspend fun uiContext(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Main, block)
}
