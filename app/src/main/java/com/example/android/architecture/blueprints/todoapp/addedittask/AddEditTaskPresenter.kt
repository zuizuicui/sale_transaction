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

package com.example.android.architecture.blueprints.todoapp.addedittask

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

import javax.inject.Inject

import dagger.Lazy

/**
 * Listens to user actions from the UI ([AddEditTaskFragment]), retrieves the data and
 * updates
 * the UI as required.
 *
 *
 * By marking the constructor with `@Inject`, Dagger injects the dependencies required to
 * create an instance of the AddEditTaskPresenter (if it fails, it emits a compiler error). It uses
 * [AddEditTaskModule] to do so.
 *
 *
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually bypassing Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
internal class AddEditTaskPresenter
/**
 * Dagger strictly enforces that arguments not marked with `@Nullable` are not injected
 * with `@Nullable` values.
 *
 * @param taskId the task ID or null if it's a new task
 * @param tasksRepository the data source
 * @param shouldLoadDataFromRepo a flag that controls whether we should load data from the
 * repository or not. It's lazy because it's determined in the
 * Activity's onCreate.
 */
@Inject
constructor(private val mTaskId: String?, tasksRepository: TasksRepository,
        // This is provided lazily because its value is determined in the Activity's onCreate. By
        // calling it in takeView(), the value is guaranteed to be set.
            private val mIsDataMissingLazy: Lazy<Boolean>) : AddEditTaskContract.Presenter, TasksDataSource.GetTaskCallback {

    private val mTasksRepository: TasksDataSource

    private var mAddTaskView: AddEditTaskContract.View? = null

    // Whether the data has been loaded with this presenter (or comes from a system restore)
    override var isDataMissing: Boolean = false
        private set

    private val isNewTask: Boolean
        get() = mTaskId == null

    init {
        mTasksRepository = tasksRepository
    }

    override fun saveTask(title: String, description: String) {
        if (isNewTask) {
            createTask(title, description)
        } else {
            updateTask(title, description)
        }
    }

    override fun populateTask() {
        if (isNewTask) {
            throw RuntimeException("populateTask() was called but task is new.")
        }
        mTasksRepository.getTask(mTaskId!!, this)
    }

    override fun takeView(view: AddEditTaskContract.View) {
        mAddTaskView = view
        isDataMissing = mIsDataMissingLazy.get()
        if (!isNewTask && isDataMissing) {
            populateTask()
        }
    }

    override fun dropView() {
        mAddTaskView = null
    }

    override fun onTaskLoaded(task: Task) {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView != null && mAddTaskView!!.isActive) {
            mAddTaskView!!.setTitle(task.title!!)
            mAddTaskView!!.setDescription(task.description!!)
        }
        isDataMissing = false
    }

    override fun onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView != null && mAddTaskView!!.isActive) {
            mAddTaskView!!.showEmptyTaskError()
        }
    }

    private fun createTask(title: String, description: String) {
        val newTask = Task(title, description)
        if (newTask.isEmpty) {
            if (mAddTaskView != null) {
                mAddTaskView!!.showEmptyTaskError()
            }
        } else {
            mTasksRepository.saveTask(newTask)
            if (mAddTaskView != null) {
                mAddTaskView!!.showTasksList()
            }
        }
    }

    private fun updateTask(title: String, description: String) {
        if (isNewTask) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        mTasksRepository.saveTask(Task(title, description, mTaskId!!))
        if (mAddTaskView != null) {
            mAddTaskView!!.showTasksList() // After an edit, go back to the list.
        }
    }
}
