package org.javacc.fuzzer

class NameAllocator {
    private val allocated = mutableSetOf<String>()

    fun <R> allocate(name: String, action: (String) -> R) {
        var v = name
        if (!allocated.add(v)) {
            var i = 0
            do {
                i += 1
                v = name + i
            } while (!allocated.add(v))
        }
        try {
            action(v)
        } finally {
            allocated.remove(v)
        }
    }
}
