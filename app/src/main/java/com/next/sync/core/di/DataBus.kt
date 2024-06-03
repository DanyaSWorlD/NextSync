package com.next.sync.core.di

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class DataBus @Inject constructor() {
    private val store = mutableMapOf<String, Any?>()
    private var exitDelegates = mutableListOf<Pair<String, (Any?) -> Unit>>()

    fun emit(key: String, value: Any?) {
        store[key] = value

        val delegatesToProcess = exitDelegates.filter { it.first == key }
        if (delegatesToProcess.isEmpty()) return

        delegatesToProcess.forEach {
            it.second(value)
            exitDelegates.remove(it)
        }

        consume(key)
    }

    fun consume(key: String): Any? {
        val value = store[key]
        store.remove(key)
        return value
    }

    suspend fun consume(key: String, callback: (Any?) -> Unit) {

        if (store.containsKey(key)) {
            callback(consume(key))
            return
        }

        enterWait { continuation ->
            exitDelegates.add(Pair(key) {
                continuation.resume(Unit)
                callback(it)
            })
        }
    }

    inline fun <reified T> tryCast(instance: Any?, block: T.() -> Unit) {
        if (instance is T) {
            block(instance)
        }
    }

    private suspend fun enterWait(f: (Continuation<Unit>) -> Unit) = suspendCoroutine {
        f(it)
    }
}

object DataBusKey {
    const val LocalPathPick = "LocalPathPick"
    const val RemotePathPick = "RemotePathPick"
    const val TaskId = "TaskId"
}