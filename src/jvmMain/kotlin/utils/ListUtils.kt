package utils

fun <T, C : MutableCollection<T>> C.toggle(item: T): C {
    if (this.contains(item))
        this.remove(item)
    else
        this.add(item)
    return this
}

fun <T, M : MutableMap<T, Boolean>> M.toggle(key: T): M {
    if (this.containsKey(key))
        this[key] = !getValue(key)
    return this
}

/**
 * Adds the item only if the collection doesn't contain it yet.
 * @author Arnau Mora
 * @since 20220713
 * @param item The item to add.
 */
fun <T, C : MutableCollection<T>> C.introduce(item: T): C =
    apply {
        if (!contains(item))
            add(item)
    }

fun <T, K, M: MutableMap<K, List<T>>> M.add(key: K, value: T): M = apply {
    if (!containsKey(key))
        this[key] = listOf(value)
    else
        this[key] = this
            .getValue(key)
            .toMutableList()
            .apply { add(value) }
}

fun <T, L: MutableList<T>> L.append(item: T): L = apply { add(item) }

operator fun <A> List<(A) -> Unit>.invoke(i: A) = forEach { it(i) }
