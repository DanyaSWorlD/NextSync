package com.next.sync.ui

import androidx.lifecycle.ViewModel

abstract class EventViewModel<TC : Any> : ViewModel() {

    internal abstract val events: Map<String, (TC) -> Unit>

    internal inline fun <reified T : TC> forEvent(
        crossinline delegate: (T) -> Unit
    ): Pair<String, (TC) -> Unit> {
        return Pair(T::class.simpleName!!) { delegate(it as T) }
    }

    internal fun onEvent(event: TC) {
        events[event.javaClass.simpleName]!!(event)
    }
}