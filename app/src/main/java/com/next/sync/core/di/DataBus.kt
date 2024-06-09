package com.next.sync.core.di

import javax.inject.Inject
import javax.inject.Singleton

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

    fun consume(key: String, callback: (Any?) -> Unit) {

        if (store.containsKey(key)) {
            callback(consume(key))
            return
        }

        exitDelegates.add(Pair(key) {
            callback(it)
        })
    }

    inline fun <reified T> tryCast(instance: Any?, block: T.() -> Unit) {
        if (instance is T) {
            block(instance)
        }
    }

    inline fun <reified T> cast(instance: Any?): T? {
        if (instance is T)
            return instance
        return null
    }
}

object DataBusKey {
    const val LocalPathPick = "LocalPathPick"
    const val RemotePathPick = "RemotePathPick"
    const val TaskId = "TaskId"
}