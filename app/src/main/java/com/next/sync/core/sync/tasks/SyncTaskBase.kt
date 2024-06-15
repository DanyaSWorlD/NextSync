package com.next.sync.core.sync.tasks

abstract class SyncTaskBase : ISyncTask {
    var nextTask: SyncTaskBase? = null

    override val hasNext: Boolean by lazy { nextTask != null }

    override fun next(): ISyncTask {
        return nextTask!!
    }

    fun then(task: SyncTaskBase): SyncTaskBase {
        if (!hasNext)
            nextTask = task

        return nextTask!!
    }
}