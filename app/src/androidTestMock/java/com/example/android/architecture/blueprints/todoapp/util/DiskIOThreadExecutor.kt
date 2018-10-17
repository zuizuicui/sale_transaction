package com.example.android.architecture.blueprints.todoapp.util

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Executor that runs a task on a new background thread.
 * This implementation is used by the Android instrumentation tests.
 */
class DiskIOThreadExecutor : Executor {

    private val mDiskIO: Executor

    init {
        mDiskIO = Executors.newSingleThreadExecutor()
    }

    override fun execute(command: Runnable) {
        // increment the idling resources before executing the long running command
        EspressoIdlingResource.increment()
        mDiskIO.execute(command)
        // decrement the idling resources once executing the command has been finished
        EspressoIdlingResource.decrement()
    }
}
