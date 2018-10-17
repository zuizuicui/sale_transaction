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

package com.example.android.architecture.blueprints.todoapp.data.source.remote

import android.os.Handler

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.google.common.collect.Lists
import java.util.LinkedHashMap

import javax.inject.Singleton

/**
 * Implementation of the data source that adds a latency simulating network.
 */
@Singleton
class TasksRemoteDataSource : TasksDataSource {

    /**
     * Note: [LoadTasksCallback.onDataNotAvailable] is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        // Simulate network by delaying the execution.
        val handler = Handler()
        handler.postDelayed({ callback.onTasksLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values)) }, SERVICE_LATENCY_IN_MILLIS.toLong())
    }

    /**
     * Note: [GetTaskCallback.onDataNotAvailable] is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val task = TASKS_SERVICE_DATA[taskId]

        // Simulate network by delaying the execution.
        val handler = Handler()
        handler.postDelayed({ callback.onTaskLoaded(task) }, SERVICE_LATENCY_IN_MILLIS.toLong())
    }

    override fun saveTask(task: Task) {
        TASKS_SERVICE_DATA[task.id] = task
    }

    override fun completeTask(task: Task) {
        val completedTask = Task(task.title, task.description, task.id, true)
        TASKS_SERVICE_DATA[task.id] = completedTask
    }

    override fun completeTask(taskId: String) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun activateTask(task: Task) {
        val activeTask = Task(task.title, task.description, task.id)
        TASKS_SERVICE_DATA[task.id] = activeTask
    }

    override fun activateTask(taskId: String) {
        // Not required for the remote data source because the {@link TasksRepository} handles
        // converting from a {@code taskId} to a {@link task} using its cached data.
    }

    override fun clearCompletedTasks() {
        val it = TASKS_SERVICE_DATA.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.isCompleted) {
                it.remove()
            }
        }
    }

    override fun refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    override fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }

    companion object {

        private val SERVICE_LATENCY_IN_MILLIS = 5000

        private val TASKS_SERVICE_DATA: MutableMap<String, Task>

        init {
            TASKS_SERVICE_DATA = LinkedHashMap(2)
            addTask("Build tower in Pisa", "Ground looks good, no foundation work required.")
            addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
        }

        private fun addTask(title: String, description: String) {
            val newTask = Task(title, description)
            TASKS_SERVICE_DATA[newTask.id] = newTask
        }
    }
}
