package com.next.sync.core.sync.tasks

interface ISyncTask {
    val next: Boolean get

    fun next() : ISyncTask

    fun Run()
}