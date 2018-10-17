/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.di.AppComponent

import java.util.ArrayList
import java.util.LinkedHashMap

import javax.inject.Inject
import javax.inject.Singleton

import com.google.common.base.Preconditions.checkNotNull

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 *
 *
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 *
 *
 * By marking the constructor with `@Inject` and the class with `@Singleton`, Dagger
 * injects the dependencies required to create an instance of the TasksRespository (if it fails, it
 * emits a compiler error). It uses [TasksRepositoryModule] to do so, and the constructed
 * instance is available in [AppComponent].
 *
 *
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
@Singleton
class TasksRepository
/**
 * By marking the constructor with `@Inject`, Dagger will try to inject the dependencies
 * required to create an instance of the TasksRepository. Because [TasksDataSource] is an
 * interface, we must provide to Dagger a way to build those arguments, this is done in
 * [TasksRepositoryModule].
 * <P>
 * When two arguments or more have the same type, we must provide to Dagger a way to
 * differentiate them. This is done using a qualifier.
</P> *
 *
 * Dagger strictly enforces that arguments not marked with `@Nullable` are not injected
 * with `@Nullable` values.
 */
@Inject
internal constructor(@param:Remote private val mTasksRemoteDataSource: TasksDataSource,
                     @param:Local private val mTasksLocalDataSource: TasksDataSource) : TasksDataSource {

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    internal var mCachedTasks: MutableMap<String, Task>? = null

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    internal var mCacheIsDirty = false

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     *
     *
     * Note: [LoadTasksCallback.onDataNotAvailable] is fired if all data sources fail to
     * get the data.
     */
    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        checkNotNull(callback)

        // Respond immediately with cache if available and not dirty
        if (mCachedTasks != null && !mCacheIsDirty) {
            callback.onTasksLoaded(ArrayList(mCachedTasks!!.values))
            return
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback)
        } else {
            // Query the local storage if available. If not, query the network.
            mTasksLocalDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
                override fun onTasksLoaded(tasks: List<Task>) {
                    refreshCache(tasks)
                    callback.onTasksLoaded(ArrayList(mCachedTasks!!.values))
                }

                override fun onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback)
                }
            })
        }
    }

    override fun saveTask(task: Task) {
        checkNotNull(task)
        mTasksRemoteDataSource.saveTask(task)
        mTasksLocalDataSource.saveTask(task)

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = LinkedHashMap()
        }
        mCachedTasks!![task.id] = task
    }

    override fun completeTask(task: Task) {
        checkNotNull(task)
        mTasksRemoteDataSource.completeTask(task)
        mTasksLocalDataSource.completeTask(task)

        val completedTask = Task(task.title, task.description, task.id, true)

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = LinkedHashMap()
        }
        mCachedTasks!![task.id] = completedTask
    }

    override fun completeTask(taskId: String) {
        checkNotNull(taskId)
        completeTask(getTaskWithId(taskId)!!)
    }

    override fun activateTask(task: Task) {
        checkNotNull(task)
        mTasksRemoteDataSource.activateTask(task)
        mTasksLocalDataSource.activateTask(task)

        val activeTask = Task(task.title, task.description, task.id)

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = LinkedHashMap()
        }
        mCachedTasks!![task.id] = activeTask
    }

    override fun activateTask(taskId: String) {
        checkNotNull(taskId)
        activateTask(getTaskWithId(taskId)!!)
    }

    override fun clearCompletedTasks() {
        mTasksRemoteDataSource.clearCompletedTasks()
        mTasksLocalDataSource.clearCompletedTasks()

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTasks == null) {
            mCachedTasks = LinkedHashMap()
        }
        val it = mCachedTasks!!.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.isCompleted) {
                it.remove()
            }
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     *
     *
     * Note: [GetTaskCallback.onDataNotAvailable] is fired if both data sources fail to
     * get the data.
     */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        checkNotNull(taskId)
        checkNotNull(callback)

        val cachedTask = getTaskWithId(taskId)

        // Respond immediately with cache if available
        if (cachedTask != null) {
            callback.onTaskLoaded(cachedTask)
            return
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        mTasksLocalDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
            override fun onTaskLoaded(task: Task) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedTasks == null) {
                    mCachedTasks = LinkedHashMap()
                }
                mCachedTasks!![task.id] = task
                callback.onTaskLoaded(task)
            }

            override fun onDataNotAvailable() {
                mTasksRemoteDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
                    override fun onTaskLoaded(task: Task) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedTasks == null) {
                            mCachedTasks = LinkedHashMap()
                        }
                        mCachedTasks!![task.id] = task
                        callback.onTaskLoaded(task)
                    }

                    override fun onDataNotAvailable() {
                        callback.onDataNotAvailable()
                    }
                })
            }
        })
    }

    override fun refreshTasks() {
        mCacheIsDirty = true
    }

    override fun deleteAllTasks() {
        mTasksRemoteDataSource.deleteAllTasks()
        mTasksLocalDataSource.deleteAllTasks()

        if (mCachedTasks == null) {
            mCachedTasks = LinkedHashMap()
        }
        mCachedTasks!!.clear()
    }

    override fun deleteTask(taskId: String) {
        mTasksRemoteDataSource.deleteTask(checkNotNull(taskId))
        mTasksLocalDataSource.deleteTask(checkNotNull(taskId))

        mCachedTasks!!.remove(taskId)
    }

    private fun getTasksFromRemoteDataSource(callback: TasksDataSource.LoadTasksCallback) {
        mTasksRemoteDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                refreshCache(tasks)
                refreshLocalDataSource(tasks)
                callback.onTasksLoaded(ArrayList(mCachedTasks!!.values))
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    private fun refreshCache(tasks: List<Task>) {
        if (mCachedTasks == null) {
            mCachedTasks = LinkedHashMap()
        }
        mCachedTasks!!.clear()
        for (task in tasks) {
            mCachedTasks!![task.id] = task
        }
        mCacheIsDirty = false
    }

    private fun refreshLocalDataSource(tasks: List<Task>) {
        mTasksLocalDataSource.deleteAllTasks()
        for (task in tasks) {
            mTasksLocalDataSource.saveTask(task)
        }
    }

    private fun getTaskWithId(id: String): Task? {
        checkNotNull(id)
        return if (mCachedTasks == null || mCachedTasks!!.isEmpty()) {
            null
        } else {
            mCachedTasks!![id]
        }
    }
}
