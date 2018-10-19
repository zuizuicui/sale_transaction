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

package com.example.android.architecture.blueprints.todoapp.taskdetail

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.google.common.base.Strings

import javax.inject.Inject

/**
 * Listens to user actions from the UI ([TaskDetailFragment]), retrieves the data and updates
 * the UI as required.
 *
 *
 * By marking the constructor with `@Inject`, Dagger injects the dependencies required to
 * create an instance of the TaskDetailPresenter (if it fails, it emits a compiler error). It uses
 * [TaskDetailPresenterModule] to do so.
 *
 *
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
internal class TaskDetailPresenter
/**
 * Dagger strictly enforces that arguments not marked with `@Nullable` are not injected
 * with `@Nullable` values.
 */
@Inject
constructor(
        /**
         * Dagger strictly enforces that arguments not marked with `@Nullable` are not injected
         * with `@Nullable` values.
         */
        private val mTaskId: String?,
        private val mTasksRepository: TasksRepository) : TaskDetailContract.Presenter {
    private var mTaskDetailView: TaskDetailContract.View? = null


    private fun openTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.showMissingTask()
            }
            return
        }

        if (mTaskDetailView != null) {
            mTaskDetailView!!.setLoadingIndicator(true)
        }
        mTasksRepository.getTask(mTaskId!!, object : TasksDataSource.GetTaskCallback {
            override fun onTaskLoaded(task: Task) {
                // The view may not be able to handle UI updates anymore
                if (mTaskDetailView == null || !mTaskDetailView!!.isActive) {
                    return
                }
                mTaskDetailView!!.setLoadingIndicator(false)
                if (null == task) {
                    mTaskDetailView!!.showMissingTask()
                } else {
                    showTask(task)
                }
            }

            override fun onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTaskDetailView!!.isActive) {
                    return
                }
                mTaskDetailView!!.showMissingTask()
            }
        })
    }

    override fun editTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.showMissingTask()
            }
            return
        }
        if (mTaskDetailView != null) {
            mTaskDetailView!!.showEditTask(mTaskId!!)
        }
    }

    override fun deleteTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.showMissingTask()
            }
            return
        }
        mTasksRepository.deleteTask(mTaskId!!)
        if (mTaskDetailView != null) {
            mTaskDetailView!!.showTaskDeleted()
        }
    }

    override fun completeTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.showMissingTask()
            }
            return
        }
        mTasksRepository.completeTask(mTaskId!!)
        if (mTaskDetailView != null) {
            mTaskDetailView!!.showTaskMarkedComplete()
        }
    }

    override fun activateTask() {
        if (Strings.isNullOrEmpty(mTaskId)) {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.showMissingTask()
            }
            return
        }
        mTasksRepository.activateTask(mTaskId!!)
        if (mTaskDetailView != null) {
            mTaskDetailView!!.showTaskMarkedActive()
        }
    }

    override fun takeView(taskDetailView: TaskDetailContract.View) {
        mTaskDetailView = taskDetailView
        openTask()
    }

    override fun dropView() {
        mTaskDetailView = null
    }

    private fun showTask(task: Task) {
        val title = task.title
        val description = task.description

        if (Strings.isNullOrEmpty(title)) {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.hideTitle()
            }
        } else {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.showTitle(title!!)
            }
        }

        if (Strings.isNullOrEmpty(description)) {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.hideDescription()
            }
        } else {
            if (mTaskDetailView != null) {
                mTaskDetailView!!.showDescription(description!!)
            }
        }
        if (mTaskDetailView != null) {
            mTaskDetailView!!.showCompletionStatus(task.isCompleted)
        }
    }
}
