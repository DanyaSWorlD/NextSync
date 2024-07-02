package com.next.sync.core.di

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataBus @Inject constructor() {
    private val store = mutableMapOf<String, Any?>()
    private val exitDelegates = mutableMapOf<String, (Any?) -> Unit>()
    private val longLiveListeners = mutableMapOf<String, HashSet<(Any?) -> Unit>>()

    fun emit(key: String, value: Any?) {
        store[key] = value

        notifyDelegates(key, value)
        notifyLongListeners(key, value)

        consume(key)
    }

    private fun notifyDelegates(key: String, value: Any?) {
        val delegatesToProcess = exitDelegates.filter { it.key == key }
        if (delegatesToProcess.isEmpty()) return

        delegatesToProcess.forEach {
            it.value(value)
            exitDelegates.remove(it.key)
        }
    }

    private fun notifyLongListeners(key: String, value: Any?) {
        if (!longLiveListeners.containsKey(key))
            return

        val listeners = longLiveListeners[key]
        listeners?.forEach {
            it(value)
        }
    }

    fun consume(key: String): Any? {
        val value = store[key]
        store.remove(key)
        return value
    }

    inline fun <reified T> consumeTyped(key: String): T? {
        return cast<T>(consume(key))
    }

    fun consume(key: String, callback: (Any?) -> Unit) {

        if (store.containsKey(key)) {
            callback(consume(key))
            return
        }

        exitDelegates[key] = { callback(it) }
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

    fun register(key: String, callback: (Any?) -> Unit) {
        if (!longLiveListeners.containsKey(key))
            longLiveListeners[key] = hashSetOf()

        longLiveListeners[key]!!.add(callback)
    }

    fun unregister(key: String, callback: (Any?) -> Unit) {
        if (!longLiveListeners.containsKey(key)) return

        if (longLiveListeners[key]!!.contains(callback))
            longLiveListeners[key]!!.remove(callback)

        if (longLiveListeners[key]!!.isEmpty())
            longLiveListeners.remove(key)
    }
}

object DataBusKey {
    const val LocalPathPick = "LocalPathPick"
    const val RemotePathPick = "RemotePathPick"
    const val TaskId = "TaskId"
    const val ProgressFlowReset = "ProgressFlowReset"
}