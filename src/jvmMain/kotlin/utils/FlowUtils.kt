package utils

/**
 * Runs [block] only if [this] is null.
 * @author Arnau Mora
 * @since 20220713
 * @param block The block of code to run if [this] is null.
 */
fun <T : Any?> T.isNull(block: () -> Unit): T {
    this ?: block()
    return this
}

/**
 * Runs the block of code only if [this] is false.
 * @author Arnau Mora
 * @since 20220713
 * @param block The block of code to run.
 */
fun <R> Boolean.otherwise(block: () -> R): R? =
    this.takeIf { !it }
        .run { block() }

/**
 * Runs the contents of [block] only if [check] is true. Returns self.
 * @author Arnau Mora
 * @since 20220716
 */
fun <T, R> T.check(check: (obj: T) -> Boolean, block: (obj: T) -> R): R? =
    this.let { if (check(this)) block(this) else null }

/**
 * Runs the contents of [block] only if [check] is true. Returns self.
 * @author Arnau Mora
 * @since 20220716
 */
fun <T, R> T.check(check: (obj: T) -> Boolean, block: (obj: T) -> R, elseBlock: (obj: T) -> R): R =
    this.let { if (check(this)) block(this) else elseBlock.invoke(this) }

inline fun <T, reified C : Throwable, R> T.attempt(block: (obj: T) -> R, onFailed: (exception: C) -> R): R =
    try {
        block(this)
    } catch (e: Exception) {
        if (e is C)
            onFailed(e)
        else throw e
    }
