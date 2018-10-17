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

package com.example.android.architecture.blueprints.todoapp.data.source.local

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.util.AppExecutors

import javax.inject.Inject
import javax.inject.Singleton

import com.google.common.base.Preconditions.checkNotNull


/**
 * Concrete implementation of a data source as a db.
 */
@Singleton
class TasksLocalDataSource @Inject
constructor(private val mAppExecutors: AppExecutors, private val mTasksDao: TasksDao) : TasksDataSource {

    /**
     * Note: [LoadTasksCallback.onDataNotAvailable] is fired if the database doesn't exist
     * or the table is empty.
     */
    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        val runnable = Runnable {
            val tasks = mTasksDao.tasks
            mAppExecutors.mainThread().execute {
                if (tasks.isEmpty()) {
                    // This will be called if the table is new or just empty.
                    callback.onDataNotAvailable()
                } else {
                    callback.onTasksLoaded(tasks)
                }
            }
        }

        mAppExecutors.diskIO().execute(runnable)
    }

    /**
     * Note: [GetTaskCallback.onDataNotAvailable] is fired if the [Task] isn't
     * found.
     */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val runnable = Runnable {
            val task = mTasksDao.getTaskById(taskId)

            mAppExecutors.mainThread().execute {
                if (task != null) {
                    callback.onTaskLoaded(task)
                } else {
                    callback.onDataNotAvailable()
                }
            }
        }

        mAppExecutors.diskIO().execute(runnable)
    }

    override fun saveTask(task: Task) {
        checkNotNull(task)
        val saveRunnable = Runnable { mTasksDao.insertTask(task) }
        mAppExecutors.diskIO().execute(saveRunnable)
    }

    override fun completeTask(task: Task) {
        val completeRunnable = Runnable { mTasksDao.updateCompleted(task.id, true) }

        mAppExecutors.diskIO().execute(completeRunnable)
    }

    override fun completeTask(taskId: String) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun activateTask(task: Task) {
        val activateRunnable = Runnable { mTasksDao.updateCompleted(task.id, false) }
        mAppExecutors.diskIO().execute(activateRunnable)
    }

    override fun activateTask(taskId: String) {
        // Not required for the local data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun clearCompletedTasks() {
        val clearTasksRunnable = Runnable { mTasksDao.deleteCompletedTasks() }

        mAppExecutors.diskIO().execute(clearTasksRunnable)
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        val deleteRunnable = Runnable { mTasksDao.deleteTasks() }

        mAppExecutors.diskIO().execute(deleteRunnable)
    }

    override fun deleteTask(taskId: String) {
        val deleteRunnable = Runnable { mTasksDao.deleteTaskById(taskId) }

        mAppExecutors.diskIO().execute(deleteRunnable)
    }
}
