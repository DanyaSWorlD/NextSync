package com.next.sync.core.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataBus @Inject constructor() {
    val store = mutableMapOf<String, Any?>()
    var keysFlow = MutableStateFlow(Pair<Long, String>(0, ""))

    var id: Long = 0

    fun emit(key: String, value: Any?) {
        store[key] = value
        keysFlow.value = Pair(++id, key)
    }

    fun consume(key: String): Any? {
        val value = store[key]
        store.remove(key)
        return value
    }

    suspend fun consume(key: String, callback: (Any?) -> Unit) {
        val index = id
        val value = keysFlow.first { it.first != index && it.second == key }
        callback(consume(value.second))
    }

    inline fun <reified T> tryCast(instance: Any?, block: T.() -> Unit) {
        if (instance is T) {
            block(instance)
        }
    }
}

object DataBusKey {
    const val LocalPathPick = "LocalPathPick"
    const val RemotePathPick = "RemotePathPick"
}